package com.mini.wallet.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Lớp khởi chạy dịch vụ Thông báo (NotificationServiceApplication).
 *
 * <p><strong>Tại sao cấu hình exclude = DataSourceAutoConfiguration.class:</strong>
 * Dịch vụ Thông báo lắng nghe sự kiện từ Kafka và phát SSE trực tiếp tới client,
 * không cần kết nối tới PostgreSQL database. Do đó loại bỏ cấu hình Database tự động
 * để tránh lỗi khởi động Spring context khi thiếu thuộc tính datasource.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = {
    "com.mini.wallet.notification",
    "com.mini.wallet.common.exception" // Quét Handler bắt lỗi từ common-library
})
public class NotificationServiceApplication {

    /**
     * Phương thức entrypoint chạy dịch vụ Thông báo.
     *
     * @param args Tham số dòng lệnh truyền vào.
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
