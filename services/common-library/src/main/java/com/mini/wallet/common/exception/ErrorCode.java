package com.mini.wallet.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Danh sách mã lỗi nghiệp vụ (Business Error Codes) chuẩn hóa cho toàn hệ thống ví điện tử.
 * Mỗi mã lỗi ánh xạ với một mã định danh chuỗi độc nhất, một thông điệp mặc định bằng tiếng Việt,
 * và một mã trạng thái HTTP Status thích hợp phục vụ cho việc trả về lỗi nhất quán ở API Gateway
 * hoặc các microservices.
 */
public enum ErrorCode {
    
    /** Lỗi dữ liệu validation đầu vào không hợp lệ */
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ hoặc thiếu thông tin bắt buộc"),

    /** Lỗi xác thực tài khoản hoặc token không hợp lệ */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Yêu cầu không được xác thực, vui lòng kiểm tra lại token"),

    /** Lỗi truy cập tài nguyên bị cấm */
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này"),

    /** Lỗi người dùng đã tồn tại trên hệ thống */
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Email người dùng này đã được đăng ký trên hệ thống"),

    /** Không tìm thấy người dùng */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng yêu cầu"),

    /** Không tìm thấy ví điện tử tương ứng */
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản ví điện tử yêu cầu"),

    /** Ví điện tử đang bị khóa hoặc đóng băng */
    WALLET_FROZEN(HttpStatus.BAD_REQUEST, "Ví điện tử này hiện đang bị đóng băng và không thể thực hiện giao dịch"),

    /** Đụng độ khóa kháng lặp giao dịch (Idempotency Key Conflict) */
    IDEMPOTENT_KEY_CONFLICT(HttpStatus.CONFLICT, "Giao dịch đang được xử lý hoặc đã hoàn tất trước đó với khóa kháng lặp này"),

    /** Lỗi máy chủ nội bộ không xác định */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống nghiêm trọng, vui lòng liên hệ ban quản trị");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    /**
     * Khởi tạo mã lỗi kèm theo mã HTTP Status và thông điệp mặc định.
     *
     * @param httpStatus Mã trạng thái HTTP trả về phía Client.
     * @param defaultMessage Thông điệp mô tả lỗi mặc định bằng tiếng Việt.
     */
    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Lấy mã trạng thái HTTP Status của lỗi.
     *
     * @return HttpStatus tương ứng của mã lỗi.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Lấy thông điệp lỗi mặc định bằng tiếng Việt.
     *
     * @return String thông điệp lỗi mặc định.
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
