# Quy Chuẩn Tái Sử Dụng & Thư Viện Dùng Chung (Reusability & Shared Modules)

Tài liệu này đặc tả cấu trúc của thư viện dùng chung **Common Shared Library (`common-library`)**.

---

## 1. Vai Trò Nghiệp Vụ Của `common-library`

Để tránh viết lặp lại (DRY) các đoạn code hạ tầng và tiện ích, toàn bộ logic không thuộc domain lõi của từng service sẽ được đưa vào `common-library`. Các microservices con (`auth-service`, `wallet-service`, `notification-service`) chỉ cần thêm dependency `common-library` vào `pom.xml` để tái sử dụng.

---

## 2. Các Thành Phần Trong `common-library`

1.  **Cấu hình Bảo mật & JWT (`com.mini.wallet.common.security`)**:
    - `JwtUtils.java`: Giải mã, kiểm tra chữ ký và trích xuất thông tin người dùng từ JWT Token.
2.  **Quản lý Ngoại lệ Toàn cục (`com.mini.wallet.common.exception`)**:
    - `BusinessException.java`: Lớp Exception nghiệp vụ dùng chung.
    - `ErrorCode.java`: Enum định nghĩa các mã lỗi hệ thống và nghiệp vụ (ví dụ: `UNAUTHORIZED`, `INVALID_INPUT`, `TRANSACTION_FAILED`).
    - `GlobalExceptionHandler.java`: `@RestControllerAdvice` xử lý lỗi tập trung.
3.  **Kháng lặp & AOP (`com.mini.wallet.common.idempotency`)**:
    - `@Idempotent`: Custom Annotation đánh dấu phương thức cần xử lý kháng lặp.
    - `IdempotentAspect.java`: Khía cạnh can thiệp tự động check/set khóa trên Redis.
4.  **Sự kiện Dùng Chung (`com.mini.wallet.common.event`)**:
    - Định nghĩa các Class Record sự kiện để serialize/deserialize khi trao đổi thông tin qua Kafka (ví dụ: `TransactionCompletedEvent.java`).
