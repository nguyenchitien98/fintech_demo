# Quy Chuẩn Lập Trình (Coding Guideline)

Tài liệu này quy định các chuẩn mực về viết mã nguồn, định dạng code và các quy tắc đặc thù của dự án **Mini Digital Wallet**.

---

## 1. Nguyên Tắc Thiết Kế Domain (Fintech Domain Rules)

- **Số dư ví (Wallet Balance)**: Tuyệt đối không lưu trữ số dư dưới dạng một cột tĩnh có thể sửa đổi bằng lệnh UPDATE tùy tiện. Số dư phải được tính bằng tổng các dòng lịch sử giao dịch (Ledger Entries).
- **Kiểu dữ liệu tiền tệ**: Luôn sử dụng kiểu dữ liệu `BigDecimal` (Java) và `DECIMAL(19, 4)` (PostgreSQL) cho các trường số tiền. Tuyệt đối không dùng `double` hoặc `float` để tránh sai số làm mất tiền của khách hàng.
- **Chú thích giải thích kiến trúc (Architectural "WHY")**: Viết comment bằng tiếng Việt trong code Java giải thích lý do thiết kế (Tại sao dùng lock, tại sao dùng idempotency, tại sao dùng outbox pattern...).

---

## 2. Quy Chuẩn Đặt Tên & Cấu Trúc Gói (Package Structure)

Mỗi microservice Java được tổ chức theo cấu trúc chuẩn:

```
com.mini.wallet.[service_name]
 ├── config          # Lớp cấu hình (Security, Redis, Kafka)
 ├── controller      # Lớp tiếp nhận API (REST Controllers)
 ├── dto             # Các bản ghi truyền tải dữ liệu (Java Records)
 ├── entity          # Các thực thể JPA (JPA Entities)
 ├── repository      # Lớp truy vấn cơ sở dữ liệu (Spring Data JPA)
 ├── service         # Lớp xử lý nghiệp vụ (Interface & Impl)
 └── exception       # Lớp xử lý ngoại lệ cục bộ
```

---

## 3. Quy Chuẩn Định Dạng Code

- Sử dụng **Google Java Style** cho toàn bộ mã nguồn Java.
- Trước khi commit, bắt buộc phải chạy công cụ định dạng tự động:
  ```bash
  mvn spotless:apply
  ```
- Mã nguồn Frontend Next.js tuân thủ ESLint và Prettier chuẩn.
