package com.mini.wallet.auth.controller;

import com.mini.wallet.auth.dto.UserRegisterDto;
import com.mini.wallet.auth.dto.UserResponseDto;
import com.mini.wallet.auth.service.AuthService;
import com.mini.wallet.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller tiếp nhận các yêu cầu xác thực và đăng ký tài khoản (AuthController).
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link RestController}: Đánh dấu lớp này là một Spring MVC REST Controller, tự động tuần tự hóa
 *   các đối tượng trả về từ phương thức thành định dạng JSON trong HTTP Response Body.</li>
 *   <li>{@link RequestMapping}: Định nghĩa tiền tố đường dẫn URI dùng chung cho toàn bộ các API trong controller.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Khởi tạo AuthController và tiêm dependencies cần thiết.
     *
     * @param authService Service xử lý nghiệp vụ tài khoản.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * API tiếp nhận đăng ký tài khoản người dùng mới.
     *
     * <p>Sau khi đăng ký thành công tài khoản, hệ thống sẽ tự động gọi sang wallet-service
     * để mở ví mặc định và trả về cấu trúc ApiResponse chuẩn.
     *
     * @param registerDto DTO thông tin tài khoản đăng ký (email, password) đã qua validation.
     * @return ApiResponse chứa thông tin người dùng được khởi tạo thành công.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponseDto> registerUser(@Valid @RequestBody UserRegisterDto registerDto) {
        UserResponseDto response = authService.register(registerDto);
        return ApiResponse.success("Đăng ký tài khoản người dùng thành công", response);
    }

    /**
     * API quản trị lấy toàn bộ danh sách người dùng đã đăng ký trên hệ thống.
     *
     * @return ApiResponse chứa danh sách toàn bộ người dùng.
     */
    @GetMapping("/users")
    public ApiResponse<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = authService.getAllUsers();
        return ApiResponse.success("Lấy danh sách người dùng thành công", users);
    }
}
