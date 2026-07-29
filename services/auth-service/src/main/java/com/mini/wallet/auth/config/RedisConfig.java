package com.mini.wallet.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Lớp cấu hình hạ tầng kết nối Redis (Redis Configuration).
 *
 * <p><strong>Tại sao sử dụng @Configuration:</strong> Annotation này báo cho Spring container biết
 * đây là lớp định nghĩa cấu hình hệ thống, chứa các phương thức khởi tạo Spring Beans (@Bean)
 * để đăng ký vào Context và sẵn sàng tiêm (Inject) cho các service khác sử dụng.
 */
@Configuration
public class RedisConfig {

    /**
     * Khởi tạo cấu hình đối tượng RedisTemplate giúp giao tiếp và thực thi các lệnh đọc/ghi dữ liệu trên Redis.
     *
     * <p><strong>Tại sao cấu hình Serializer:</strong> Mặc định Spring sử dụng JdkSerializationRedisSerializer
     * khiến dữ liệu lưu trên Redis ở dạng nhị phân thô không đọc được bằng mắt thường. Chúng ta cấu hình lại:
     * - Key Serializer: Sử dụng String để lưu trữ các khóa dạng text trực quan (ví dụ: "otp:user:john@example.com").
     * - Value Serializer: Sử dụng GenericJackson2JsonRedisSerializer để tự động chuyển các Object Java thành JSON
     * giúp tiết kiệm dung lượng lưu trữ và dễ dàng đối soát khi dùng redis-cli.
     *
     * @param connectionFactory Nhà máy quản lý kết nối Redis (được Spring tự động cấu hình dựa trên datasource trong yml).
     * @return Đối tượng RedisTemplate cấu hình sẵn.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Cấu hình Serializer cho Key dạng String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Cấu hình Serializer cho Value dạng JSON
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
