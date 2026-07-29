# Quy Chuẩn Phản Hồi API & Xử Lý Ngoại Lệ (API Response & Exception Standard)

Tất cả các API được phơi bày từ các Microservices phải tuân thủ nghiêm ngặt quy chuẩn định dạng JSON dưới đây.

---

## 1. Định Dạng Phản Hồi Thành Công (Standard Success Response)
Mọi phản hồi thành công (HTTP Status 2xx) bắt buộc phải bọc trong một Record chung `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Giao dịch chuyển tiền thành công",
  "data": {
    "transactionId": "d8a1f810-728f-4cb1-80a5-b1a9f1a28a3f",
    "amount": 50000.00,
    "status": "SUCCESS"
  }
}
```

---

## 2. Định Dạng Phản Hồi Thất Bại (Standard Error Response)
Khi xảy ra lỗi (HTTP Status 4xx, 5xx), hệ thống không được trả về mã stacktrace raw mà phải phản hồi cấu trúc `ApiErrorResponse` chuẩn:

```json
{
  "success": false,
  "message": "Số dư tài khoản không đủ để thực hiện giao dịch",
  "errorCode": "INSUFFICIENT_BALANCE",
  "timestamp": "2026-07-29T06:00:00Z",
  "errors": {
    "walletId": "Số dư khả dụng là 20,000 VND, yêu cầu chuyển 50,000 VND"
  }
}
```

---

## 3. Quản Lý Ngoại Lệ Toàn Cục (Global Exception Handler)
Mỗi microservice Java phải tích hợp một Class `@RestControllerAdvice` kế thừa từ `common-library` để tự động chặn các ngoại lệ:
*   `BusinessException`: Chứa mã lỗi nghiệp vụ tùy chỉnh (ví dụ: `INSUFFICIENT_BALANCE`, `WALLET_FROZEN`, `IDEMPOTENT_KEY_CONFLICT`).
*   `MethodArgumentNotValidException`: Tự động trích xuất các lỗi validation đầu vào (`@NotNull`, `@Min`, `@Pattern`) và đưa vào trường `errors` của JSON phản hồi lỗi.
