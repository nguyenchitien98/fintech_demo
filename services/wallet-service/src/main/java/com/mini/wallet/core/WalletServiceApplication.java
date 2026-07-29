package com.mini.wallet.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lớp khởi chạy ứng dụng Spring Boot cho Wallet Core Service (Dịch vụ Ví điện tử lõi).
 *
 * <p><strong>Tại sao sử dụng @SpringBootApplication:</strong> Annotation này cấu hình tự động
 * ba thành phần cốt lõi của Spring Boot: tự động quét các bean (@ComponentScan), tự động cấu hình
 * các thư viện phụ thuộc (@EnableAutoConfiguration), và đánh dấu đây là lớp cấu hình Spring chính.
 */
@SpringBootApplication(scanBasePackages = {
    "com.mini.wallet.core",
    "com.mini.wallet.common.exception" // Quét cả GlobalExceptionHandler từ common-library
})
public class WalletServiceApplication {

    /**
     * Phương thức main để khởi chạy ứng dụng Java Spring Boot.
     *
     * @param args Các tham số dòng lệnh truyền vào khi chạy ứng dụng.
     */
    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}
