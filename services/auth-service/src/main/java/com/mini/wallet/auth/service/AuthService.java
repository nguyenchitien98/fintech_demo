package com.mini.wallet.auth.service;

import com.mini.wallet.auth.dto.LoginRequestDto;
import com.mini.wallet.auth.dto.LoginResponseDto;
import com.mini.wallet.auth.dto.UserRegisterDto;
import com.mini.wallet.auth.dto.UserResponseDto;
import com.mini.wallet.auth.dto.VerifyOtpDto;
import com.mini.wallet.auth.entity.User;
import com.mini.wallet.auth.repository.UserRepository;
import com.mini.wallet.common.exception.BusinessException;
import com.mini.wallet.common.exception.ErrorCode;
import com.mini.wallet.common.security.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Lớp dịch vụ xử lý các nghiệp vụ xác thực tài khoản, 2FA OTP, JWT và logout (Auth Service).
 *
 * <p><strong>Tại sao sử dụng @Service:</strong> Đánh dấu đây là Spring Service Component
 * quản lý toàn bộ nghiệp vụ an ninh của dự án.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;

    @Value("${app.services.wallet-url}")
    private String walletServiceUrl;

    /**
     * Khởi tạo AuthService và tiêm tất cả các dependencies bảo mật cần thiết.
     *
     * @param userRepository Repository quản lý bảng users.
     * @param restTemplate RestTemplate gọi API ví.
     * @param passwordEncoder Đối tượng băm mật khẩu.
     * @param redisTemplate Cache lưu trữ OTP/Blacklist.
     * @param jwtUtils Tiện ích sinh giải mã JWT.
     */
    public AuthService(UserRepository userRepository, 
                       RestTemplate restTemplate, 
                       PasswordEncoder passwordEncoder,
                       RedisTemplate<String, Object> redisTemplate,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Thực hiện nghiệp vụ đăng ký người dùng mới.
     *
     * <p><strong>Tại sao dùng passwordEncoder.encode:</strong> Tuyệt đối không bao giờ được lưu mật khẩu
     * thô của người dùng dưới DB để tránh rò rỉ thông tin khi database bị tấn công. BCrypt được sử dụng
     * để băm mật khẩu một chiều đi kèm salt ngẫu nhiên.
     */
    @Transactional
    public UserResponseDto register(UserRegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.email())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setEmail(registerDto.email());
        user.setPassword(passwordEncoder.encode(registerDto.password())); // Mã hóa BCrypt mật khẩu
        user = userRepository.save(user);

        try {
            createWalletForUser(user.getId());
        } catch (Exception ex) {
            throw new BusinessException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Không thể mở ví điện tử mặc định cho tài khoản này. Đăng ký thất bại. Chi tiết: " + ex.getMessage()
            );
        }

        return new UserResponseDto(user.getId(), user.getEmail(), user.getActive(), user.getCreatedAt());
    }

    /**
     * Thực hiện nghiệp vụ đăng nhập Bước 1: Đối sánh mật khẩu và sinh mã OTP 2FA.
     *
     * <p><strong>Tại sao lưu OTP vào Redis:</strong> Redis có cấu trúc dạng in-memory cache cực nhanh
     * và hỗ trợ thuộc tính TTL (Time To Live). Mã OTP được đặt thời hạn tự động huỷ sau 120 giây (2 phút)
     * giúp tăng cường bảo mật và giải phóng bộ nhớ tự động mà không cần quét bảng DB thủ công.
     *
     * @param loginDto DTO chứa email và mật khẩu của người dùng.
     * @return LoginResponseDto đánh dấu requires2fa=true để bắt chuyển hướng sang trang nhập OTP.
     */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto loginDto) {
        User user = userRepository.findByEmail(loginDto.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác"));

        if (!user.getActive()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Tài khoản của bạn đã bị khóa");
        }

        // Kiểm tra đối khớp mật khẩu băm BCrypt
        if (!passwordEncoder.matches(loginDto.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
        }

        // Sinh ngẫu nhiên mã OTP 2FA gồm 6 số
        String otp = String.format("%06d", new Random().nextInt(1000000));

        // Lưu mã OTP vào Redis với thời gian sống 2 phút
        String redisKey = "otp:user:" + user.getEmail();
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofSeconds(120));

        // In mã OTP ra log console của hệ thống để mô phỏng (dễ dàng lấy test)
        System.out.println("=================================================");
        System.out.println(">>> [2FA OTP] MÃ XÁC THỰC CỦA " + user.getEmail() + " LÀ: " + otp);
        System.out.println("=================================================");

        return LoginResponseDto.requires2fa(user.getEmail());
    }

    /**
     * Thực hiện nghiệp vụ đăng nhập Bước 2: Xác thực mã OTP 2FA và cấp phát JWT.
     *
     * @param verifyDto DTO chứa email và mã OTP người dùng gửi lên.
     * @return LoginResponseDto chứa Access Token và Refresh Token.
     */
    public LoginResponseDto verifyOtp(VerifyOtpDto verifyDto) {
        String email = verifyDto.email();
        String otp = verifyDto.otp();

        String redisKey = "otp:user:" + email;
        String savedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedOtp == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Mã OTP đã hết hạn hoặc không tồn tại, vui lòng đăng nhập lại");
        }

        if (!savedOtp.equals(otp)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Mã OTP không chính xác");
        }

        // Xác thực thành công -> Xóa mã OTP trên Redis
        redisTemplate.delete(redisKey);

        // Sinh cặp JWT tokens
        String accessToken = jwtUtils.generateAccessToken(email);
        String refreshToken = jwtUtils.generateRefreshToken(email);

        return LoginResponseDto.success(email, accessToken, refreshToken);
    }

    /**
     * Thực hiện đăng xuất người dùng: Thu hồi Access Token hiện tại.
     *
     * <p><strong>Tại sao dùng Redis Blacklist:</strong> Vì JWT mang tính chất stateless (không lưu trạng thái ở server),
     * không thể vô hiệu hóa trực tiếp token trước hạn. Do đó khi đăng xuất, ta lấy token đưa vào Redis Blacklist
     * với TTL đúng bằng thời gian sống còn lại của token. Gateway sẽ kiểm tra blacklist này để chặn đứng truy cập.
     *
     * @param authorizationHeader Header Authorization chứa chuỗi JWT dạng Bearer.
     */
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            if (jwtUtils.validateToken(token)) {
                Claims claims = jwtUtils.extractClaims(token);
                long expirationTime = claims.getExpiration().getTime();
                long remainingMs = expirationTime - System.currentTimeMillis();

                // Đưa token vào Blacklist trên Redis nếu nó chưa hết hạn thực tế
                if (remainingMs > 0) {
                    String blacklistKey = "jwt:blacklist:" + token;
                    redisTemplate.opsForValue().set(blacklistKey, "revoked", Duration.ofMillis(remainingMs));
                }
            }
        } catch (Exception e) {
            // Bỏ qua nếu token không đúng định dạng khi parse
        }
    }

    /**
     * Lấy danh sách toàn bộ người dùng đã đăng ký trên hệ thống (phục vụ giao diện Admin).
     *
     * @return Danh sách các UserResponseDto.
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Gửi yêu cầu HTTP POST sang wallet-service để yêu cầu tạo ví mới cho người dùng.
     *
     * @param userId Mã định danh của người dùng vừa được tạo.
     */
    private void createWalletForUser(Long userId) {
        String url = walletServiceUrl + "/api/v1/wallets";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", userId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        restTemplate.postForEntity(url, request, Object.class);
    }
}
