package com.mini.wallet.core.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bản ghi truyền dữ liệu phản hồi sau khi thực hiện chuyển khoản thành công (Transfer Response DTO).
 *
 * @param transactionId Mã định danh duy nhất của giao dịch vừa tạo.
 * @param fromWalletId Mã định danh ví gửi.
 * @param toWalletId Mã định danh ví nhận.
 * @param amount Số tiền đã chuyển thành công.
 * @param status Trạng thái giao dịch (SUCCESS, FAILED).
 * @param description Nội dung mô tả giao dịch.
 * @param createdAt Thời điểm tạo giao dịch.
 */
public record TransferResponseDto(
    Long transactionId,
    Long fromWalletId,
    Long toWalletId,
    BigDecimal amount,
    String status,
    String description,
    LocalDateTime createdAt
) {}
