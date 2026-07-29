# Common Shared Library

Thư viện dùng chung (Common Library) chứa các cấu trúc DTO phản hồi API thống nhất, cấu hình xử lý ngoại lệ toàn cục (Global Exception Handling), JWT Security Utilities và bộ máy AOP Aspect hỗ trợ cơ chế Kháng lặp giao dịch (Idempotency Aspect) sử dụng Redis.

## 📂 Các Thành Phần Chính

- **`com.mini.wallet.common.dto`**:
  - `ApiResponse`: Cấu trúc Record phản hồi thành công chuẩn cho tất cả API hệ thống.
- **`com.mini.wallet.common.exception`**:
  - `ErrorCode`: Enum định nghĩa các mã lỗi nghiệp vụ chuẩn tiếng Việt (ví dụ: `WALLET_NOT_FOUND`, `IDEMPOTENT_KEY_CONFLICT`, `TOKEN_EXPIRED`...).
  - `BusinessException`: Lớp runtime exception dùng để ném các lỗi nghiệp vụ.
  - `GlobalExceptionHandler`: Bộ điều khiển bắt và định dạng lỗi trả về chuẩn JSON `ApiErrorResponse` cho Frontend.
- **`com.mini.wallet.common.security`**:
  - `JwtUtils`: Tiện ích tạo sinh, giải mã và xác thực chữ ký token JWT.
- **`com.mini.wallet.common.idempotent`**:
  - `Idempotent`: Custom annotation đánh dấu các API cần chặn trùng lặp.
  - `IdempotentAspect`: Aspect AOP chặn bắt header `X-Idempotency-Key`, kiểm tra trạng thái lưu trữ trên Redis (`PROCESSING`, `COMPLETED`) để kháng lặp tự động trong 24 giờ.

## 📦 Cách Tích Hợp Vào Service Khác
Khai báo dependency trong tệp `pom.xml` của service con:

```xml
<dependency>
    <groupId>com.mini.wallet</groupId>
    <artifactId>common-library</artifactId>
</dependency>
```
