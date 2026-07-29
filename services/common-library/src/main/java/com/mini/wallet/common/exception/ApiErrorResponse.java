package com.mini.wallet.common.exception;

import java.time.LocalDateTime;

/**
 * Lớp Record đại diện cho cấu trúc phản hồi thất bại chuẩn (Standard Error Response)
 * trong toàn bộ hệ thống API của Mini Digital Wallet.
 * Khi hệ thống microservice gặp bất kỳ lỗi nghiệp vụ nào hoặc lỗi ngoại lệ hệ thống nào,
 * phản hồi lỗi bắt buộc phải có cấu trúc như record này để đảm bảo Frontend
 * có thể parse lỗi và hiển thị thông tin chính xác.
 *
 * @param success Trạng thái xử lý (luôn là false đối với phản hồi lỗi).
 * @param message Thông điệp mô tả lỗi tóm tắt bằng tiếng Việt để hiển thị trực tiếp lên UI.
 * @param errorCode Chuỗi định danh mã lỗi nghiệp vụ (ví dụ: INSUFFICIENT_BALANCE).
 * @param timestamp Thời điểm xảy ra lỗi hệ thống (định dạng ISO LocalDateTime).
 * @param errors Đối tượng chứa chi tiết lỗi kỹ thuật cụ thể (như lỗi Validation từng trường dữ liệu hoặc chi tiết số dư).
 */
public record ApiErrorResponse(
    boolean success,
    String message,
    String errorCode,
    LocalDateTime timestamp,
    Object errors
) {
    /**
     * Khởi tạo nhanh một phản hồi lỗi chuẩn không kèm theo thông tin chi tiết lỗi.
     *
     * @param message Thông điệp mô tả lỗi.
     * @param errorCode Chuỗi mã lỗi định danh.
     * @return Đối tượng ApiErrorResponse chuẩn.
     */
    public static ApiErrorResponse of(String message, String errorCode) {
        return new ApiErrorResponse(false, message, errorCode, LocalDateTime.now(), null);
    }

    /**
     * Khởi tạo một phản hồi lỗi chuẩn đầy đủ bao gồm cả chi tiết lỗi bổ sung.
     *
     * @param message Thông điệp mô tả lỗi.
     * @param errorCode Chuỗi mã lỗi định danh.
     * @param errors Đối tượng chứa chi tiết lỗi bổ sung.
     * @return Đối tượng ApiErrorResponse chuẩn.
     */
    public static ApiErrorResponse of(String message, String errorCode, Object errors) {
        return new ApiErrorResponse(false, message, errorCode, LocalDateTime.now(), errors);
    }
}
