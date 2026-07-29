package com.mini.wallet.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.wallet.common.exception.BusinessException;
import com.mini.wallet.common.exception.ErrorCode;
import com.mini.wallet.core.dto.LedgerResponseDto;
import com.mini.wallet.core.dto.TransferRequestDto;
import com.mini.wallet.core.dto.TransferResponseDto;
import com.mini.wallet.core.dto.WalletCreateDto;
import com.mini.wallet.core.dto.WalletResponseDto;
import com.mini.wallet.core.entity.LedgerEntry;
import com.mini.wallet.core.entity.OutboxEvent;
import com.mini.wallet.core.entity.Transaction;
import com.mini.wallet.core.entity.Wallet;
import com.mini.wallet.core.repository.LedgerEntryRepository;
import com.mini.wallet.core.repository.OutboxEventRepository;
import com.mini.wallet.core.repository.TransactionRepository;
import com.mini.wallet.core.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lớp xử lý nghiệp vụ quản lý ví điện tử, sổ cái Ledger và giao dịch chuyển tiền (Wallet Core Service).
 * Tích hợp lưu trữ sự kiện outbox để đồng bộ Kafka bất đồng bộ bền bỉ.
 *
 * <p><strong>Tại sao sử dụng @Service:</strong> Annotation này đánh dấu lớp là một Spring Service Bean,
 * đóng vai trò cung cấp các logic xử lý nghiệp vụ ví lõi cho ứng dụng, hỗ trợ tiêm phụ thuộc tự động.
 */
@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OutboxEventRepository outboxEventRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Khởi tạo WalletService thông qua Constructor Injection để tiêm các repositories.
     *
     * @param walletRepository Repository quản lý tương tác dữ liệu bảng wallets.
     * @param transactionRepository Repository quản lý bảng transactions.
     * @param ledgerEntryRepository Repository quản lý bảng ledger_entries.
     * @param outboxEventRepository Repository quản lý bảng outbox_events.
     */
    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository,
                         LedgerEntryRepository ledgerEntryRepository,
                         OutboxEventRepository outboxEventRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    /**
     * Tạo tài khoản ví điện tử mặc định cho người dùng mới.
     *
     * @param createDto DTO chứa mã định danh người dùng.
     * @return WalletResponseDto chứa thông tin ví vừa tạo thành công.
     */
    @Transactional
    public WalletResponseDto createWallet(WalletCreateDto createDto) {
        Wallet wallet = new Wallet();
        wallet.setUserId(createDto.userId());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("VND");
        wallet.setStatus("ACTIVE");
        
        wallet = walletRepository.save(wallet);
        return mapToResponseDto(wallet);
    }

    /**
     * Lấy danh sách toàn bộ các tài khoản ví đang tồn tại trong hệ thống (phục vụ Admin Dashboard).
     *
     * @return Danh sách các ví điện tử được bọc trong WalletResponseDto.
     */
    @Transactional(readOnly = true)
    public List<WalletResponseDto> getAllWallets() {
        return walletRepository.findAll().stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Đóng băng tài khoản ví điện tử chỉ định.
     * Ví sau khi bị đóng băng sẽ không thể tham gia vào bất kỳ giao dịch chuyển nhận tiền nào.
     *
     * @param id Mã định danh duy nhất của tài khoản ví cần đóng băng.
     * @return WalletResponseDto chứa thông tin ví với trạng thái FROZEN mới.
     * @throws BusinessException nếu không tìm thấy ví tương ứng với ID cung cấp.
     */
    @Transactional
    public WalletResponseDto freezeWallet(Long id) {
        Wallet wallet = walletRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setStatus("FROZEN");
        wallet = walletRepository.save(wallet);
        return mapToResponseDto(wallet);
    }

    /**
     * Mở băng tài khoản ví điện tử đang bị đóng băng.
     * Ví sau khi mở băng sẽ chuyển về trạng thái ACTIVE và thực hiện các giao dịch bình thường.
     *
     * @param id Mã định danh duy nhất của tài khoản ví cần mở băng.
     * @return WalletResponseDto chứa thông tin ví với trạng thái ACTIVE mới.
     * @throws BusinessException nếu không tìm thấy ví tương ứng với ID cung cấp.
     */
    @Transactional
    public WalletResponseDto unfreezeWallet(Long id) {
        Wallet wallet = walletRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setStatus("ACTIVE");
        wallet = walletRepository.save(wallet);
        return mapToResponseDto(wallet);
    }

    /**
     * Thực hiện nghiệp vụ chuyển tiền giữa 2 tài khoản ví áp dụng Sổ kế toán kép (Double-entry Ledger)
     * và khóa bi quan để phòng race condition và deadlock.
     *
     * <p><strong>Tại sao sử dụng @Transactional:</strong> Annotation này đảm bảo toàn bộ luồng xử lý
     * (Select ví, trừ tiền ví gửi, cộng tiền ví nhận, tạo transaction, tạo ledger entries, ghi outbox) đều nằm
     * trong một Database Transaction duy nhất. Nếu xảy ra bất kỳ lỗi runtime nào, toàn bộ dữ liệu sẽ
     * được Rollback hoàn hảo, ngăn chặn lỗi thất thoát tiền tệ.
     *
     * @param request DTO chứa thông tin ví gửi, ví nhận, số tiền và nội dung chuyển.
     * @return TransferResponseDto chứa thông tin giao dịch thành công.
     * @throws BusinessException khi ví gửi không đủ tiền, ví bị đóng băng, hoặc không tìm thấy ví.
     */
    @Transactional
    public TransferResponseDto transferMoney(TransferRequestDto request) {
        Long fromId = request.fromWalletId();
        Long toId = request.toWalletId();
        BigDecimal amount = request.amount();

        if (fromId.equals(toId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Không thể chuyển khoản đến cùng một ví gửi");
        }

        // 1. Áp dụng thuật toán sắp xếp thứ tự khóa để tránh deadlock
        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);

        // Thực hiện SELECT ... FOR UPDATE theo thứ tự ID tăng dần
        Wallet firstWallet = walletRepository.findByIdForUpdate(firstId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND, "Không tìm thấy ví có ID: " + firstId));
        Wallet secondWallet = walletRepository.findByIdForUpdate(secondId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND, "Không tìm thấy ví có ID: " + secondId));

        Wallet fromWallet = firstWallet.getId().equals(fromId) ? firstWallet : secondWallet;
        Wallet toWallet = firstWallet.getId().equals(toId) ? firstWallet : secondWallet;

        // 2. Kiểm tra trạng thái hoạt động của ví (Ví không bị đóng băng)
        if ("FROZEN".equals(fromWallet.getStatus())) {
            throw new BusinessException(ErrorCode.WALLET_FROZEN, "Ví gửi đang bị đóng băng, không thể thực hiện giao dịch");
        }
        if ("FROZEN".equals(toWallet.getStatus())) {
            throw new BusinessException(ErrorCode.WALLET_FROZEN, "Ví nhận đang bị đóng băng, không thể nhận tiền");
        }

        // 3. Kiểm tra số dư ví gửi
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 4. Cập nhật số dư hai ví
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // 5. Ghi nhận giao dịch tổng quát
        Transaction tx = new Transaction();
        tx.setFromWalletId(fromId);
        tx.setToWalletId(toId);
        tx.setAmount(amount);
        tx.setDescription(request.description());
        tx.setReference(request.reference());
        tx.setStatus("SUCCESS");
        tx = transactionRepository.save(tx);

        // 6. Ghi nhận bút toán Nợ (DEBIT) cho ví gửi
        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setWalletId(fromId);
        debitEntry.setTransactionId(tx.getId());
        debitEntry.setType("DEBIT");
        debitEntry.setAmount(amount);
        ledgerEntryRepository.save(debitEntry);

        // 7. Ghi nhận bút toán Có (CREDIT) cho ví nhận
        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setWalletId(toId);
        creditEntry.setTransactionId(tx.getId());
        creditEntry.setType("CREDIT");
        creditEntry.setAmount(amount);
        ledgerEntryRepository.save(creditEntry);

        // 8. Mô hình Transactional Outbox Pattern: Tạo và lưu sự kiện chuyển tiền vào cơ sở dữ liệu
        try {
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("transactionId", tx.getId());
            eventPayload.put("fromWalletId", fromId);
            eventPayload.put("toWalletId", toId);
            eventPayload.put("amount", amount);
            eventPayload.put("status", tx.getStatus());
            eventPayload.put("description", tx.getDescription());
            eventPayload.put("reference", tx.getReference());
            eventPayload.put("createdAt", tx.getCreatedAt().toString());

            String payloadJson = objectMapper.writeValueAsString(eventPayload);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType("WALLET_TRANSACTION");
            outboxEvent.setAggregateId(tx.getId().toString());
            outboxEvent.setEventType("TRANSACTION_COMPLETED");
            outboxEvent.setPayload(payloadJson);
            outboxEvent.setStatus("PENDING");

            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            // Không rollback transaction ví nếu gặp lỗi convert JSON (để đảm bảo tính sẵn sàng tối đa)
            System.err.println(">>> [Outbox Error] Không thể lưu sự kiện Outbox: " + e.getMessage());
        }

        return new TransferResponseDto(
            tx.getId(),
            tx.getFromWalletId(),
            tx.getToWalletId(),
            tx.getAmount(),
            tx.getStatus(),
            tx.getDescription(),
            tx.getCreatedAt()
        );
    }

    /**
     * Truy vấn danh sách dòng sổ cái Ledger (phân trang) phục vụ cho màn hình Ledger Explorer.
     * Hỗ trợ bộ lọc theo tài khoản ví cụ thể.
     *
     * @param walletId Mã ví điện tử để lọc (truyền null nếu lấy toàn bộ hệ thống).
     * @param pageable Ràng buộc phân trang và sắp xếp.
     * @return Trang Page chứa danh sách các DTO LedgerResponseDto.
     */
    @Transactional(readOnly = true)
    public Page<LedgerResponseDto> getLedgerEntries(Long walletId, Pageable pageable) {
        Page<LedgerEntry> entries;
        if (walletId != null) {
            entries = ledgerEntryRepository.findByWalletId(walletId, pageable);
        } else {
            entries = ledgerEntryRepository.findAll(pageable);
        }
        
        return entries.map(entry -> new LedgerResponseDto(
            entry.getId(),
            entry.getWalletId(),
            entry.getTransactionId(),
            entry.getType(),
            entry.getAmount(),
            entry.getCreatedAt()
        ));
    }

    /**
     * Ánh xạ thông tin thực thể Wallet sang DTO WalletResponseDto.
     *
     * @param wallet Thực thể Wallet cần ánh xạ.
     * @return WalletResponseDto tương ứng.
     */
    private WalletResponseDto mapToResponseDto(Wallet wallet) {
        return new WalletResponseDto(
            wallet.getId(),
            wallet.getUserId(),
            wallet.getBalance(),
            wallet.getCurrency(),
            wallet.getStatus(),
            wallet.getCreatedAt()
        );
    }
}
