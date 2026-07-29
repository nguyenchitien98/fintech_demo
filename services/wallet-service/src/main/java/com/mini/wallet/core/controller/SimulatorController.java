package com.mini.wallet.core.controller;

import com.mini.wallet.common.dto.ApiResponse;
import com.mini.wallet.core.dto.TransferRequestDto;
import com.mini.wallet.core.entity.Wallet;
import com.mini.wallet.core.repository.WalletRepository;
import com.mini.wallet.core.service.OutboxPoller;
import com.mini.wallet.core.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * REST Controller điều phối các hoạt động mô phỏng tải và lỗi hệ thống (SimulatorController).
 * Cung cấp APIs giả lập giao dịch tranh chấp luồng Race Condition và lỗi sập Broker Chaos Simulator.
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link RestController}: Khai báo đây là một REST API Controller trả về dữ liệu JSON trực tiếp.</li>
 *   <li>{@link RequestMapping}: Cấu hình endpoint gốc `/api/v1/wallets/simulator`.</li>
 *   <li>{@link CrossOrigin}: Mở CORS cho phép Frontend gọi trực tiếp không qua gateway để kiểm thử.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/wallets/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    private final WalletService walletService;
    private final WalletRepository walletRepository;

    /**
     * Khởi tạo SimulatorController.
     *
     * @param walletService Service xử lý chuyển khoản để chạy đa luồng.
     * @param walletRepository Repository ví để lấy số dư đối soát sau mô phỏng.
     */
    public SimulatorController(WalletService walletService, WalletRepository walletRepository) {
        this.walletService = walletService;
        this.walletRepository = walletRepository;
    }

    /**
     * Record đại diện cho request body giả lập Race Condition.
     */
    public record RaceRequest(
        Long fromWalletId,
        Long toWalletId,
        BigDecimal amount,
        int threads
    ) {}

    /**
     * API Giả lập Race Condition (Chuyển khoản song song nhiều luồng đồng thời).
     * Sử dụng Virtual Threads (Java 21) để bắn đồng thời nhiều luồng gọi API chuyển khoản.
     *
     * @param request DTO cấu hình tham số giả lập.
     * @return ResponseEntity chứa kết quả đếm thành công/thất bại và số dư đối soát.
     */
    @PostMapping("/race")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateRaceCondition(@RequestBody RaceRequest request) {
        int threadCount = request.threads();
        BigDecimal amount = request.amount();
        Long fromId = request.fromWalletId();
        Long toId = request.toWalletId();

        // 1. Tạo Thread Pool sử dụng Virtual Threads (Java 21) để đạt hiệu suất tối đa
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        // 2. Kích hoạt song song threadCount tác vụ chuyển khoản
        for (int i = 0; i < threadCount; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    walletService.transferMoney(new TransferRequestDto(
                        fromId,
                        toId,
                        amount,
                        "Giả lập Race Condition luồng song song",
                        "RACE-SIM"
                    ));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failedCount.incrementAndGet();
                }
            }, executor));
        }

        // 3. Chờ tất cả các Task đa luồng hoàn tất
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // 4. Lấy số dư thực tế sau giao dịch của 2 ví để đối soát
        Wallet fromWallet = walletRepository.findById(fromId).orElse(null);
        Wallet toWallet = walletRepository.findById(toId).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount.get());
        result.put("failedCount", failedCount.get());
        result.put("fromWalletBalance", fromWallet != null ? fromWallet.getBalance() : BigDecimal.ZERO);
        result.put("toWalletBalance", toWallet != null ? toWallet.getBalance() : BigDecimal.ZERO);
        result.put("fromWalletId", fromId);
        result.put("toWalletId", toId);

        return ResponseEntity.ok(ApiResponse.success("Giả lập Race Condition hoàn tất", result));
    }

    /**
     * API Giả lập Bật/Tắt trạng thái Offline của Broker Kafka (Chaos Simulator).
     *
     * @return ResponseEntity chứa trạng thái OFFLINE/ONLINE hiện tại của Kafka.
     */
    @PostMapping("/chaos/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleKafkaChaos() {
        OutboxPoller.isKafkaOffline = !OutboxPoller.isKafkaOffline;
        
        Map<String, Object> result = new HashMap<>();
        result.put("isKafkaOffline", OutboxPoller.isKafkaOffline);
        result.put("status", OutboxPoller.isKafkaOffline ? "OFFLINE (SIMULATED)" : "ONLINE");

        return ResponseEntity.ok(ApiResponse.success("Đã thay đổi trạng thái Chaos Simulator", result));
    }

    /**
     * API Khôi phục hệ thống (Recover) tắt giả lập lỗi sập Kafka.
     * Đánh thức OutboxPoller tiếp tục gửi bù các message PENDING lên Broker.
     *
     * @return ResponseEntity chứa thông báo kết quả.
     */
    @PostMapping("/chaos/recover")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recoverFromChaos() {
        OutboxPoller.isKafkaOffline = false;
        
        Map<String, Object> result = new HashMap<>();
        result.put("isKafkaOffline", false);
        result.put("status", "ONLINE");

        return ResponseEntity.ok(ApiResponse.success("Hệ thống đã phục hồi. Kafka ONLINE.", result));
    }
}
