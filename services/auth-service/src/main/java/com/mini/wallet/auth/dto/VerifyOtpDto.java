package com.mini.wallet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DDTO nhận thông tin xác thực mã OTP 2FA (Verify OTP DTO).
 *
 * @param email Địa chỉ email của tài khoản đang thực hiện đăng nhập.
 * @param otp Mã xác thực OTP 6 ký tự số nhận được.
 */
public record VerifyOtpDto(
    @NotBlank(message = "Email không được phép để trống")
    @Email(message = "Email không đúng định dạng quy chuẩn")
    String email,

    @NotBlank(message = "Mã OTP không được phép để trống")
    @Size(min = 6, max = 6, message = "Mã OTP phải chứa chính xác 6 ký tự số")
    String otp
) {}
