package com.mini.wallet.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp tiện ích quản lý và xử lý JWT Token (JwtUtils).
 * Chịu trách nhiệm sinh, phân tích và kiểm tra tính hợp lệ của cặp Access Token/Refresh Token.
 *
 * <p><strong>Tại sao sử dụng @Component:</strong> Đánh dấu lớp này là một Spring Component Bean
 * để có thể dễ dàng tiêm (Inject) vào bất kỳ tầng nào cần xác thực hoặc cấp phát token (như Security Config,
 * Gateway hoặc Auth Service).
 */
@Component
public class JwtUtils {

    // Khóa bí mật mặc định dùng để ký chữ ký JWT (HS256). 
    // Trong môi trường production, khóa này bắt buộc phải lấy từ biến môi trường/config an toàn.
    private static final String DEFAULT_SECRET = "9a7f3e82d5b6a1c8f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1";
    
    private final Key signingKey;

    /**
     * Khởi tạo JwtUtils và chuẩn bị khóa ký mã hóa từ cấu hình secret.
     */
    public JwtUtils() {
        this.signingKey = Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes());
    }

    /**
     * Sinh một JSON Web Token (JWT) cho tài khoản người dùng chỉ định.
     *
     * @param email Địa chỉ email của người dùng (đóng vai trò là Subject của token).
     * @param expirationMs Thời gian sống của token tính bằng mili-giây.
     * @param additionalClaims Các thông tin bổ sung muốn đính kèm vào payload của token (như roles, status).
     * @return Chuỗi mã hóa JWT hoàn chỉnh.
     */
    public String generateToken(String email, long expirationMs, Map<String, Object> additionalClaims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(additionalClaims != null ? additionalClaims : new HashMap<>())
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Sinh nhanh một Access Token cơ bản (mặc định hạn 15 phút).
     *
     * @param email Địa chỉ email người dùng.
     * @return Chuỗi JWT Access Token.
     */
    public String generateAccessToken(String email) {
        // 15 phút = 15 * 60 * 1000 = 900,000 ms
        return generateToken(email, 900000L, new HashMap<>());
    }

    /**
     * Sinh nhanh một Refresh Token cơ bản (mặc định hạn 7 ngày).
     *
     * @param email Địa chỉ email người dùng.
     * @return Chuỗi JWT Refresh Token.
     */
    public String generateRefreshToken(String email) {
        // 7 ngày = 7 * 24 * 60 * 60 * 1000 = 604,800,000 ms
        return generateToken(email, 604800000L, new HashMap<>());
    }

    /**
     * Giải mã và trích xuất thông tin payload (Claims) từ một chuỗi JWT Token.
     *
     * @param token Chuỗi mã hóa JWT.
     * @return Claims chứa toàn bộ thông tin payload.
     * @throws io.jsonwebtoken.JwtException nếu token không đúng định dạng hoặc bị sửa đổi trái phép.
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Trích xuất địa chỉ email (Subject) từ JWT Token.
     *
     * @param token Chuỗi mã hóa JWT.
     * @return String địa chỉ email chủ sở hữu token.
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Kiểm tra tính hợp lệ và thời hạn sử dụng của JWT Token.
     *
     * @param token Chuỗi mã hóa JWT.
     * @return true nếu token hợp lệ và còn hạn sử dụng, ngược lại trả về false.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
