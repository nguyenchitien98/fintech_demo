package com.mini.wallet.core.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bản ghi dữ liệu phản hồi dòng sổ cái chi tiết (Ledger Entry Response DTO).
 *
 * @param id Mã định danh duy nhất của dòng sổ cái.
 * @param walletId Mã ví điện tử được ghi nhận.
 * @param transactionId Mã giao dịch liên đới.
 * @param type Loại bút toán (DEBIT hoặc CREDIT).
 * @param amount Số tiền giao dịch.
 * @param createdAt Thời điểm ghi sổ cái.
 */
public record LedgerResponseDto(
    Long id,
    Long walletId,
    Long transactionId,
    String type,
    BigDecimal amount,
    LocalDateTime createdAt
) {}
