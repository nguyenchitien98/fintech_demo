package com.mini.wallet.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Lớp xử lý ngoại lệ toàn cục (Global Exception Handler) cho các Microservices.
 *
 * <p><strong>Tại sao sử dụng @RestControllerAdvice:</strong> Annotation này biến lớp trở thành
 * một khía cạnh can thiệp (AOP Interceptor) tập trung. Nó tự động bắt tất cả các ngoại lệ ném ra
 * từ các REST Controllers của ứng dụng và chuyển đổi chúng thành phản hồi JSON có cấu trúc chuẩn
 * thay vì trả về lỗi mặc định (HTML stacktrace của Tomcat/Spring) gây mất bảo mật thông tin và khó
 * parse dữ liệu ở Frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt giữ và xử lý ngoại lệ nghiệp vụ BusinessException tự định nghĩa trong hệ thống.
     *
     * <p><strong>Tại sao sử dụng @ExceptionHandler:</strong> Annotation này chỉ định phương thức
     * sẽ xử lý khi một lớp ngoại lệ cụ thể (ở đây là BusinessException) được ném ra trong luồng request.
     *
     * @param ex Ngoại lệ nghiệp vụ bắt được.
     * @return ResponseEntity chứa đối tượng ApiErrorResponse chuẩn và HTTP Status tương ứng của mã lỗi.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiErrorResponse response = ApiErrorResponse.of(
            ex.getMessage(),
            errorCode.name(),
            ex.getDetails()
        );
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * Xử lý ngoại lệ Validation đầu vào (khi các DTO được đánh dấu @Valid bị vi phạm ràng buộc).
     *
     * <p>Phương thức trích xuất chi tiết từng trường dữ liệu bị lỗi (ví dụ: email trống, mật khẩu ngắn)
     * và gom chúng vào bản đồ `errors` của JSON phản hồi để Frontend hiển thị thông báo lỗi từng ô nhập liệu.
     *
     * @param ex Ngoại lệ validation bắt được.
     * @return ResponseEntity chứa ApiErrorResponse với mã lỗi INVALID_INPUT và danh sách chi tiết lỗi validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorResponse response = ApiErrorResponse.of(
            ErrorCode.INVALID_INPUT.getDefaultMessage(),
            ErrorCode.INVALID_INPUT.name(),
            errors
        );
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT.getHttpStatus());
    }

    /**
     * Bắt giữ tất cả các ngoại lệ hệ thống không xác định khác (lỗi runtime chung, lỗi DB...).
     *
     * <p>Chốt chặn an toàn cuối cùng ngăn chặn việc rò rỉ stacktrace chi tiết của DB/Hệ thống ra bên ngoài.
     *
     * @param ex Ngoại lệ runtime không mong muốn.
     * @return ResponseEntity chứa ApiErrorResponse với mã lỗi INTERNAL_SERVER_ERROR và HTTP Status 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        ApiErrorResponse response = ApiErrorResponse.of(
            ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
            ErrorCode.INTERNAL_SERVER_ERROR.name(),
            ex.getMessage() // Có thể ẩn đi trong môi trường production thực tế để tăng tính bảo mật
        );
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
}
