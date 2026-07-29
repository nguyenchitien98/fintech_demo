package com.mini.wallet.common.dto;

/**
 * Lớp Record đại diện cho cấu trúc phản hồi thành công chuẩn (Standard Success Response)
 * trong toàn bộ hệ thống API của Mini Digital Wallet.
 * Tất cả các API khi thực thi thành công (HTTP Status 2xx) bắt buộc phải bọc dữ liệu trả về
 * trong đối tượng này để đảm bảo tính nhất quán định dạng phản hồi cho Frontend.
 *
 * @param <T> Kiểu dữ liệu của payload trả về trong trường data.
 * @param success Trạng thái thành công của yêu cầu (luôn là true đối với phản hồi này).
 * @param message Thông điệp mô tả kết quả xử lý giao dịch hoặc yêu cầu API bằng tiếng Việt.
 * @param data Đối tượng chứa dữ liệu phản hồi chi tiết từ nghiệp vụ.
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data
) {
    /**
     * Phương thức tiện ích để khởi tạo nhanh một phản hồi thành công kèm dữ liệu.
     *
     * @param <T> Kiểu dữ liệu của payload.
     * @param message Thông điệp mô tả kết quả.
     * @param data Dữ liệu nghiệp vụ đi kèm.
     * @return Đối tượng ApiResponse chuẩn chứa thông điệp và dữ liệu nghiệp vụ.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Phương thức tiện ích để khởi tạo phản hồi thành công không kèm dữ liệu nghiệp vụ.
     *
     * @param <T> Kiểu dữ liệu (sẽ mang giá trị Void hoặc null).
     * @param message Thông điệp mô tả kết quả.
     * @return Đối tượng ApiResponse chuẩn chứa thông điệp và data bằng null.
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
}
