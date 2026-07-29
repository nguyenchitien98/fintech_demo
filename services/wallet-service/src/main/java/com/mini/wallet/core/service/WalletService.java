package com.mini.wallet.core.service;

import com.mini.wallet.common.exception.BusinessException;
import com.mini.wallet.common.exception.ErrorCode;
import com.mini.wallet.core.dto.WalletCreateDto;
import com.mini.wallet.core.dto.WalletResponseDto;
import com.mini.wallet.core.entity.Wallet;
import com.mini.wallet.core.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lớp xử lý nghiệp vụ quản lý ví điện tử (Wallet Core Service).
 *
 * <p><strong>Tại sao sử dụng @Service:</strong> Annotation này đánh dấu lớp là một Spring Service Bean,
 * đóng vai trò cung cấp các logic xử lý nghiệp vụ ví lõi cho ứng dụng, hỗ trợ tiêm phụ thuộc tự động.
 */
@Service
public class WalletService {

    private final WalletRepository walletRepository;

    /**
     * Khởi tạo WalletService thông qua Constructor Injection.
     *
     * @param walletRepository Repository quản lý tương tác dữ liệu bảng wallets.
     */
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
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
