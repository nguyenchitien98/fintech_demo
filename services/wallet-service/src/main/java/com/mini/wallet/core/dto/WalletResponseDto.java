package com.mini.wallet.core.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bản ghi dữ liệu phản hồi thông tin ví (Wallet Response DTO).
 *
 * @param id Mã định danh duy nhất của ví.
 * @param userId Mã định danh người dùng sở hữu ví.
 * @param balance Số dư khả dụng của ví.
 * @param currency Đơn vị tiền tệ.
 * @param status Trạng thái ví (ACTIVE, FROZEN).
 * @param createdAt Thời điểm tạo ví.
 */
public record WalletResponseDto(
    Long id,
    Long userId,
    BigDecimal balance,
    String currency,
    String status,
    LocalDateTime createdAt
) {}
