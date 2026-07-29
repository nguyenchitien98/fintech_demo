package com.mini.wallet.gateway.filter;

import com.mini.wallet.common.security.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Bộ lọc xác thực JWT Token và rà soát Redis Blacklist tại API Gateway (JwtAuthFilter).
 *
 * <p><strong>Tại sao sử dụng @Component:</strong> Đăng ký filter làm Spring bean
 * giúp Spring Cloud Gateway tự động quét và áp dụng cấu hình filter tương ứng trong application.yml.
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtils jwtUtils;
    private final ReactiveStringRedisTemplate redisTemplate;

    // Danh sách các public endpoints được phép bỏ qua không cần check JWT
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/verify-2fa"
    );

    /**
     * Khởi tạo JwtAuthFilter và tiêm các tiện ích JWT và Redis.
     *
     * @param jwtUtils Đối tượng tiện ích xử lý giải mã JWT.
     * @param redisTemplate Reactive template giao tiếp với Redis không block luồng Netty.
     */
    public JwtAuthFilter(JwtUtils jwtUtils, ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Thực thi logic lọc yêu cầu HTTP đi qua Gateway.
     *
     * @param config Đối tượng cấu hình filter.
     * @return Đối tượng GatewayFilter chứa logic lọc.
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 1. Nếu thuộc danh sách whitelist thì bỏ qua kiểm tra JWT
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            // 2. Trích xuất Authorization Header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Thiếu hoặc sai định dạng token xác thực", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // 3. Xác thực tính đúng đắn và thời hạn của JWT Token
            if (!jwtUtils.validateToken(token)) {
                return onError(exchange, "Token xác thực không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED);
            }

            // 4. Tra cứu Redis Blacklist (Reactive) để kiểm tra xem token đã bị vô hiệu hóa do Logout hay chưa
            String blacklistKey = "jwt:blacklist:" + token;
            return redisTemplate.hasKey(blacklistKey)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return onError(exchange, "Phiên đăng nhập này đã bị hủy bỏ", HttpStatus.UNAUTHORIZED);
                    }
                    return chain.filter(exchange);
                });
        };
    }

    /**
     * Kiểm tra xem đường dẫn yêu cầu có nằm trong whitelist các URL công khai hay không.
     *
     * @param path Đường dẫn URL yêu cầu (ví dụ: "/api/v1/auth/login").
     * @return true nếu là công khai, ngược lại trả về false.
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * Phản hồi thông báo lỗi và mã trạng thái HTTP về client khi không vượt qua bộ lọc bảo mật.
     *
     * @param exchange Đối tượng giao dịch WebFlux.
     * @param errMessage Chuỗi thông báo lỗi chi tiết.
     * @param httpStatus Mã trạng thái lỗi HTTP (như 401).
     * @return Mono trống báo hiệu kết thúc request.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String jsonError = String.format(
            "{\"success\":false,\"message\":\"%s\",\"data\":null}", 
            errMessage
        );
        
        byte[] bytes = jsonError.getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * Lớp Config rỗng dùng để đáp ứng cấu hình tham số mặc định của AbstractGatewayFilterFactory.
     */
    public static class Config {
    }
}
