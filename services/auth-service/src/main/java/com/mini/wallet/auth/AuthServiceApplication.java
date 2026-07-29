package com.mini.wallet.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Lớp khởi chạy ứng dụng Spring Boot cho Authentication Service (Dịch vụ Xác thực).
 *
 * <p><strong>Tại sao sử dụng @SpringBootApplication:</strong> Annotation này cấu hình tự động
 * ba thành phần cốt lõi của Spring Boot: tự động quét các bean (@ComponentScan), tự động cấu hình
 * các thư viện phụ thuộc (@EnableAutoConfiguration), và đánh dấu đây là lớp cấu hình Spring chính.
 */
@SpringBootApplication
public class AuthServiceApplication {

    /**
     * Phương thức main để khởi chạy ứng dụng Java Spring Boot.
     *
     * @param args Các tham số dòng lệnh truyền vào khi chạy ứng dụng.
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    /**
     * Khởi tạo RestTemplate dưới dạng một Spring Bean để sử dụng gọi API REST đồng bộ
     * giữa các microservices (ở đây là gọi sang wallet-service để tự động mở ví).
     *
     * @return Một đối tượng RestTemplate cấu hình sẵn.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
