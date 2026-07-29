package com.mini.wallet.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mini.wallet.core.dto.TransferRequestDto;
import com.mini.wallet.core.dto.WalletCreateDto;
import com.mini.wallet.core.dto.WalletResponseDto;
import com.mini.wallet.core.entity.Wallet;
import com.mini.wallet.core.repository.LedgerEntryRepository;
import com.mini.wallet.core.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bài kiểm thử tích hợp đồng thời (Concurrency Integration Test) cho WalletService.
 * Giả lập 100 Virtual Threads chuyển tiền song song từ một ví sang một ví khác
 * để kiểm chứng tính an toàn của cơ chế Khóa bi quan (Pessimistic Locking)
 * và tính bảo toàn số dư của Sổ kế toán kép.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
public class WalletServiceConcurrencyTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    private Long fromWalletId;
    private Long toWalletId;

    /**
     * Thiết lập dữ liệu ban đầu trước mỗi ca kiểm thử.
     * Tạo 2 ví:
     * - Ví gửi (fromWallet) có số dư ban đầu là 10,000 VND.
     * - Ví nhận (toWallet) có số dư ban đầu là 0 VND.
     */
    @BeforeEach
    public void setUp() {
        walletRepository.deleteAll();
        ledgerEntryRepository.deleteAll();

        // Tạo ví gửi cho user 1
        WalletResponseDto w1 = walletService.createWallet(new WalletCreateDto(1L));
        fromWalletId = w1.id();
        
        // Tạo ví nhận cho user 2
        WalletResponseDto w2 = walletService.createWallet(new WalletCreateDto(2L));
        toWalletId = w2.id();

        // Nạp tiền ban đầu vào ví gửi (10,000.00 VND)
        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow();
        fromWallet.setBalance(new BigDecimal("10000.0000"));
        walletRepository.save(fromWallet);
    }

    /**
     * Ca kiểm thử giả lập 100 luồng chuyển tiền đồng thời, mỗi luồng chuyển 100 VND.
     * Tổng số tiền cần chuyển là 100 * 100 = 10,000 VND (vừa vặn hết số dư ví gửi).
     *
     * <p>Kết quả mong đợi sau khi tất cả các luồng hoàn thành:
     * - Ví gửi sạch tiền (số dư bằng 0.0000 VND).
     * - Ví nhận có chính xác 10,000.0000 VND.
     * - Tổng số dòng ghi nhận bút toán trong ledger_entries phải là 200 dòng (100 DEBIT và 100 CREDIT).
     */
    @Test
    public void testConcurrentMoneyTransfer() throws InterruptedException {
        int numberOfThreads = 100;
        BigDecimal transferAmount = new BigDecimal("100.0000");

        // Sử dụng Virtual Threads Executor để tận dụng tính năng Java 21 Loom
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // Đợi tín hiệu phát súng khởi động để tất cả các luồng bắn request cùng lúc
                    startLatch.await();
                    walletService.transferMoney(new TransferRequestDto(
                        fromWalletId,
                        toWalletId,
                        transferAmount,
                        "Concurrent transfer test",
                        "REF-CONCURRENT"
                    ));
                } catch (Exception e) {
                    System.err.println("Giao dịch đồng thời gặp lỗi: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Phát súng khởi động cho 100 luồng chạy song song đồng thời
        startLatch.countDown();
        
        // Đợi tất cả 100 luồng hoàn thành xử lý
        finishLatch.await();
        executorService.shutdown();

        // 3. Đọc lại trạng thái ví sau khi kiểm thử
        Wallet finalFromWallet = walletRepository.findById(fromWalletId).orElseThrow();
        Wallet finalToWallet = walletRepository.findById(toWalletId).orElseThrow();

        // Đối soát số dư
        assertEquals(0, new BigDecimal("0.0000").compareTo(finalFromWallet.getBalance()), 
            "Ví gửi phải bị trừ sạch tiền về 0 VND");
        assertEquals(0, new BigDecimal("10000.0000").compareTo(finalToWallet.getBalance()), 
            "Ví nhận phải nhận đủ chính xác 10,000 VND");

        // Đối soát số dòng Ledger
        long ledgerCount = ledgerEntryRepository.count();
        assertEquals(200, ledgerCount, "Tổng số dòng bút toán ghi nhận trong Ledger phải là 200 dòng");
    }
}
