package com.mini.wallet.core.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Bản ghi dữ liệu yêu cầu tạo ví mới (Wallet Create DTO).
 *
 * @param userId Mã định danh người dùng sở hữu ví, không được null.
 */
public record WalletCreateDto(
    @NotNull(message = "Mã định danh người dùng không được phép để trống")
    Long userId
) {}
