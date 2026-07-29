package com.mini.wallet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Bản ghi truyền tải dữ liệu đăng ký người dùng (User Registration DTO).
 * Sử dụng Java Record giúp tối giản mã nguồn, tự động tạo các trường dữ liệu bất biến (final)
 * cùng với các hàm getter/constructor/toString/equals/hashCode chuẩn.
 *
 * @param email Địa chỉ email đăng ký của người dùng, bắt buộc phải đúng định dạng email.
 * @param password Mật khẩu đăng ký của người dùng, bắt buộc có độ dài tối thiểu 6 ký tự.
 */
public record UserRegisterDto(
    @NotBlank(message = "Email không được phép để trống")
    @Email(message = "Email không đúng định dạng quy chuẩn")
    String email,

    @NotBlank(message = "Mật khẩu không được phép để trống")
    @Size(min = 6, message = "Mật khẩu phải chứa ít nhất 6 ký tự")
    String password
) {}
