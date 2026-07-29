package com.mini.wallet.auth.service;

import com.mini.wallet.auth.dto.UserRegisterDto;
import com.mini.wallet.auth.dto.UserResponseDto;
import com.mini.wallet.auth.entity.User;
import com.mini.wallet.auth.repository.UserRepository;
import com.mini.wallet.common.exception.BusinessException;
import com.mini.wallet.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lớp dịch vụ xử lý nghiệp vụ xác thực và quản lý tài khoản người dùng (Auth Service Implementation).
 *
 * <p><strong>Tại sao sử dụng @Service:</strong> Annotation này đánh dấu đây là một Spring Service Bean
 * chứa logic nghiệp vụ cốt lõi của ứng dụng, giúp Spring tự động phát hiện, quản lý vòng đời và tiêm
 * các phụ thuộc (Dependency Injection).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${app.services.wallet-url}")
    private String walletServiceUrl;

    /**
     * Khởi tạo AuthService thông qua cơ chế Inject constructor của Spring.
     *
     * @param userRepository Repository quản lý bảng users.
     * @param restTemplate RestTemplate dùng để gọi API nội bộ sang các dịch vụ khác.
     */
    public AuthService(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Thực hiện nghiệp vụ đăng ký người dùng mới và tự động mở Ví mặc định tương ứng.
     *
     * <p><strong>Tại sao sử dụng @Transactional:</strong> Annotation này bao bọc toàn bộ quá trình
     * đăng ký trong một Transaction của cơ sở dữ liệu. Nếu việc lưu thông tin tài khoản thành công
     * nhưng khâu gọi API tạo ví sang wallet-service bị thất bại (ném ngoại lệ), transaction này sẽ
     * tự động bị ROLLBACK để đảm bảo tính toàn vẹn dữ liệu giữa hai dịch vụ (tránh việc có tài khoản
     * người dùng nhưng không có ví).
     *
     * @param registerDto DTO chứa thông tin email và password.
     * @return UserResponseDto chứa thông tin người dùng đã tạo.
     * @throws BusinessException nếu email đã tồn tại hoặc gặp lỗi kết nối wallet-service.
     */
    @Transactional
    public UserResponseDto register(UserRegisterDto registerDto) {
        // 1. Kiểm tra sự tồn tại của email
        if (userRepository.existsByEmail(registerDto.email())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 2. Tạo thực thể người dùng mới và lưu vào DB (Mật khẩu ở Sprint 1 được lưu thô đơn giản)
        User user = new User();
        user.setEmail(registerDto.email());
        user.setPassword(registerDto.password()); // TODO: Sẽ tích hợp Spring Security mã hóa ở Sprint 3
        user = userRepository.save(user);

        // 3. Gọi API nội bộ sang wallet-service để tự động tạo ví mặc định
        try {
            createWalletForUser(user.getId());
        } catch (Exception ex) {
            // Ném lỗi để kích hoạt rollback transaction
            throw new BusinessException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Không thể mở ví điện tử mặc định cho tài khoản này. Đăng ký thất bại. Chi tiết: " + ex.getMessage()
            );
        }

        return new UserResponseDto(user.getId(), user.getEmail(), user.getActive(), user.getCreatedAt());
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
        
        // Gọi API POST sang wallet-service
        restTemplate.postForEntity(url, request, Object.class);
    }
}
