package com.mini.wallet.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Lớp khởi chạy API Gateway Application (GatewayApplication).
 *
 * <p><strong>Tại sao cấu hình exclude = DataSourceAutoConfiguration.class:</strong>
 * Lớp API Gateway này sử dụng WebFlux và Spring Cloud Gateway đóng vai trò định tuyến mạng,
 * không cần tương tác trực tiếp hay kết nối tới bất kỳ cơ sở dữ liệu JDBC quan hệ nào (như PostgreSQL).
 * Do đó ta cần cấu hình loại trừ cấu hình cơ sở dữ liệu mặc định để tránh lỗi khởi chạy Spring context
 * khi thiếu DataSource properties.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GatewayApplication {

    /**
     * Phương thức entrypoint chạy ứng dụng API Gateway.
     *
     * @param args Tham số dòng lệnh truyền vào (nếu có).
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
