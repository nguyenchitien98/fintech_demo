package com.mini.wallet.common.exception;

/**
 * Lớp Ngoại lệ Nghiệp vụ (Business Exception) dùng chung cho toàn bộ hệ sinh thái ví điện tử.
 * Mọi lỗi nghiệp vụ phát sinh trong quá trình xử lý logic (ví dụ: Số dư không đủ, Ví bị khóa,
 * Trùng lặp Idempotency key...) đều phải ném ra ngoại lệ này hoặc các lớp kế thừa từ nó.
 * Ngoại lệ này sẽ được tự động bắt giữ bởi GlobalExceptionHandler để trả về định dạng JSON lỗi chuẩn.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object details;

    /**
     * Khởi tạo ngoại lệ nghiệp vụ với một mã lỗi có sẵn và thông điệp mặc định của mã lỗi đó.
     *
     * @param errorCode Enum ErrorCode định nghĩa mã lỗi và HTTP Status.
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Khởi tạo ngoại lệ nghiệp vụ với một mã lỗi và thông điệp lỗi động tùy biến.
     *
     * @param errorCode Enum ErrorCode định nghĩa mã lỗi.
     * @param customMessage Thông điệp mô tả lỗi chi tiết cho trường hợp phát sinh cụ thể.
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Khởi tạo ngoại lệ nghiệp vụ kèm thông điệp động và chi tiết lỗi bổ sung (ví dụ: danh sách validation errors).
     *
     * @param errorCode Enum ErrorCode định nghĩa mã lỗi.
     * @param customMessage Thông điệp mô tả lỗi tùy biến.
     * @param details Đối tượng chứa thông tin kỹ thuật/Validation chi tiết để phản hồi về client.
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Object details) {
        super(customMessage);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Lấy mã lỗi ErrorCode đính kèm trong ngoại lệ.
     *
     * @return ErrorCode của lỗi.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Lấy chi tiết lỗi bổ sung đính kèm (nếu có).
     *
     * @return Object chứa chi tiết lỗi bổ sung hoặc null.
     */
    public Object getDetails() {
        return details;
    }
}
