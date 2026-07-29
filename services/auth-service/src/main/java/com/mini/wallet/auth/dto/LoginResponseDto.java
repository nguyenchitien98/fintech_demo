package com.mini.wallet.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bản ghi phản hồi kết quả xác thực tài khoản (Login Response DTO).
 *
 * @param requires2fa Trạng thái xác thực 2 lớp (True: Yêu cầu OTP, False: Đăng nhập thành công trực tiếp).
 * @param email Địa chỉ email của người dùng.
 * @param accessToken Chuỗi JWT Access Token (chỉ trả về khi đã xác thực 2FA thành công).
 * @param refreshToken Chuỗi JWT Refresh Token (chỉ trả về khi đã xác thực 2FA thành công).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDto(
    boolean requires2fa,
    String email,
    String accessToken,
    String refreshToken
) {
    /**
     * Khởi tạo nhanh phản hồi yêu cầu xác thực 2FA.
     *
     * @param email Địa chỉ email đăng nhập của người dùng.
     * @return Đối tượng LoginResponseDto yêu cầu 2FA.
     */
    public static LoginResponseDto requires2fa(String email) {
        return new LoginResponseDto(true, email, null, null);
    }

    /**
     * Khởi tạo nhanh phản hồi đăng nhập thành công đầy đủ token.
     *
     * @param email Địa chỉ email người dùng.
     * @param accessToken Access Token đã sinh.
     * @param refreshToken Refresh Token đã sinh.
     * @return Đối tượng LoginResponseDto chứa tokens.
     */
    public static LoginResponseDto success(String email, String accessToken, String refreshToken) {
        return new LoginResponseDto(false, email, accessToken, refreshToken);
    }
}
