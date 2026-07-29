package com.mini.wallet.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Lớp cấu hình bảo mật mật khẩu người dùng (Security Configuration).
 *
 * <p><strong>Tại sao sử dụng @Configuration:</strong> Báo cho Spring container biết đây là lớp
 * định nghĩa cấu hình hệ thống chứa định nghĩa Spring Beans.
 */
@Configuration
public class SecurityConfig {

    /**
     * Định nghĩa bean PasswordEncoder để mã hóa mật khẩu người dùng.
     *
     * <p><strong>Tại sao dùng BCryptPasswordEncoder:</strong> BCrypt là thuật toán băm mật khẩu một chiều
     * cực kỳ an toàn, tự động tích hợp chuỗi muối ngẫu nhiên (Salt) và thuật toán lặp chống lại các cuộc tấn công
     * dò mật khẩu (Brute force hoặc Rainbow table).
     *
     * @return Một đối tượng PasswordEncoder cấu hình băm BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
