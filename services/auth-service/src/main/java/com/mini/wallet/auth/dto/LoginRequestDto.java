package com.mini.wallet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Bản ghi nhận dữ liệu yêu cầu đăng nhập (Login Request DTO).
 *
 * @param email Địa chỉ email đăng nhập, không được để trống và đúng định dạng.
 * @param password Mật khẩu đăng nhập, không được để trống.
 */
public record LoginRequestDto(
    @NotBlank(message = "Email đăng nhập không được phép để trống")
    @Email(message = "Email đăng nhập không đúng định dạng quy chuẩn")
    String email,

    @NotBlank(message = "Mật khẩu không được phép để trống")
    String password
) {}
