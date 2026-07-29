package com.mini.wallet.auth.controller;

import com.mini.wallet.auth.dto.LoginRequestDto;
import com.mini.wallet.auth.dto.LoginResponseDto;
import com.mini.wallet.auth.dto.UserRegisterDto;
import com.mini.wallet.auth.dto.UserResponseDto;
import com.mini.wallet.auth.dto.VerifyOtpDto;
import com.mini.wallet.auth.service.AuthService;
import com.mini.wallet.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller quản lý các luồng xác thực hệ thống (AuthController).
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link RestController}: Đánh dấu lớp là REST Controller trả về JSON.</li>
 *   <li>{@link RequestMapping}: Định cấu hình endpoint gốc `/api/v1/auth`.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Khởi tạo AuthController và tiêm service xử lý nghiệp vụ bảo mật.
     *
     * @param authService Dịch vụ xác thực.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * API Đăng ký tài khoản người dùng mới (Register).
     *
     * @param registerDto Chứa email và mật khẩu thô.
     * @return ApiResponse bọc đối tượng UserResponseDto vừa đăng ký thành công.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponseDto> register(@Valid @RequestBody UserRegisterDto registerDto) {
        UserResponseDto response = authService.register(registerDto);
        return ApiResponse.success("Đăng ký tài khoản người dùng thành công", response);
    }

    /**
     * API Đăng nhập Bước 1 (Login).
     * Xác thực email/password. Nếu hợp lệ, hệ thống tự động gửi và lưu mã OTP vào Redis.
     *
     * @param loginDto DTO chứa thông tin email và mật khẩu của người dùng.
     * @return ApiResponse bọc LoginResponseDto chứa cờ yêu cầu OTP requires2fa=true.
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginDto) {
        LoginResponseDto response = authService.login(loginDto);
        return ApiResponse.success("Thông tin đăng nhập hợp lệ. Vui lòng xác thực mã 2FA OTP gửi đến.", response);
    }

    /**
     * API Đăng nhập Bước 2: Xác thực mã OTP 2FA (Verify OTP).
     * Đối soát OTP từ client gửi lên với dữ liệu trên Redis. Nếu khớp, cấp cặp JWT tokens.
     *
     * @param verifyDto DTO chứa email và OTP 6 chữ số.
     * @return ApiResponse bọc LoginResponseDto chứa Access Token và Refresh Token.
     */
    @PostMapping("/verify-2fa")
    public ApiResponse<LoginResponseDto> verify2fa(@Valid @RequestBody VerifyOtpDto verifyDto) {
        LoginResponseDto response = authService.verifyOtp(verifyDto);
        return ApiResponse.success("Xác thực OTP 2FA thành công. Đăng nhập hoàn tất.", response);
    }

    /**
     * API Đăng xuất tài khoản (Logout).
     * Đưa Access Token đang dùng vào Redis Blacklist để vô hiệu hóa ngay lập tức.
     *
     * @param authHeader Tiêu đề Authorization từ Header chứa token (Bearer ...).
     * @return ApiResponse báo trạng thái đăng xuất.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ApiResponse.success("Đăng xuất tài khoản và vô hiệu hóa phiên làm việc thành công", null);
    }

    /**
     * API lấy toàn bộ danh sách tài khoản người dùng trên hệ thống (phục vụ Admin).
     *
     * @return ApiResponse bọc danh sách UserResponseDto.
     */
    @GetMapping("/users")
    public ApiResponse<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = authService.getAllUsers();
        return ApiResponse.success("Lấy danh sách người dùng thành công", users);
    }
}
