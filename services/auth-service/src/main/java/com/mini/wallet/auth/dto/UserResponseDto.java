package com.mini.wallet.auth.dto;

import java.time.LocalDateTime;

/**
 * Bản ghi dữ liệu phản hồi thông tin người dùng (User Response DTO).
 *
 * @param id Mã định danh duy nhất của người dùng.
 * @param email Địa chỉ email của người dùng.
 * @param active Trạng thái hoạt động của tài khoản.
 * @param createdAt Thời điểm tài khoản được tạo.
 */
public record UserResponseDto(
    Long id,
    String email,
    Boolean active,
    LocalDateTime createdAt
) {}
