package com.mini.wallet.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Bản ghi truyền dữ liệu yêu cầu chuyển khoản (Transfer Request DTO).
 *
 * @param fromWalletId Mã định danh ví gửi tiền, không được null.
 * @param toWalletId Mã định danh ví nhận tiền, không được null.
 * @param amount Số tiền chuyển khoản, bắt buộc phải lớn hơn 0.
 * @param description Nội dung chuyển khoản.
 * @param reference Tham chiếu giao dịch (nếu có).
 */
public record TransferRequestDto(
    @NotNull(message = "Mã ví gửi không được phép để trống")
    Long fromWalletId,

    @NotNull(message = "Mã ví nhận không được phép để trống")
    Long toWalletId,

    @NotNull(message = "Số tiền chuyển không được phép để trống")
    @DecimalMin(value = "0.01", message = "Số tiền chuyển tối thiểu phải là 0.01")
    BigDecimal amount,

    String description,
    
    String reference
) {}
