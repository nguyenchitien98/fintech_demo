package com.mini.wallet.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Lớp cấu hình giới hạn tốc độ yêu cầu (Rate Limiter Configuration).
 *
 * <p><strong>Tại sao sử dụng @Configuration:</strong> Báo cho Spring container biết đây là lớp
 * cấu hình Spring beans.
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Khởi tạo bean KeyResolver định nghĩa đối tượng phân loại dùng để giới hạn tần suất gọi API.
     *
     * <p><strong>Tại sao dùng RemoteAddress:</strong> Chúng ta giới hạn tốc độ truy cập dựa trên IP Client
     * gửi yêu cầu. Mỗi IP khách hàng sẽ được cấp phát một Token Bucket riêng trên Redis. Điều này giúp ngăn chặn
     * hiệu quả các cuộc tấn công từ chối dịch vụ (DDoS) hoặc spam gọi API hàng loạt từ một IP cố định.
     *
     * @return Đối tượng KeyResolver cung cấp IP của client.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress()
                .getHostAddress()
        );
    }
}
