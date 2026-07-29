# Wallet Core Service

Dịch vụ Ví Lõi (`wallet-service`) xử lý toàn bộ nghiệp vụ tạo ví, đóng băng ví, chuyển tiền tức thì bảo toàn bởi sổ kép Ledger, kiểm soát tranh chấp tải race condition đa luồng và lưu trữ sự kiện Transactional Outbox.

## ⚙️ Các Chức Năng Chính

1. **Quản lý ví**: Tạo tài khoản ví (VND), đóng băng/mở băng tài khoản ví.
2. **Chuyển tiền sổ kép (Ledger)**: Chuyển khoản giữa các ví ghi nhận đồng thời 1 bút toán DEBIT (Nợ) ở ví gửi và 1 bút toán CREDIT (Có) ở ví nhận trong cùng một PostgreSQL transaction.
3. **Chống Deadlock & Race Condition**: Sắp xếp thứ tự ID ví khi thực hiện `SELECT ... FOR UPDATE` (Pessimistic locking) để tránh xung đột vòng lặp tài nguyên.
4. **Kháng lặp Idempotency**: Đóng dấu `@Idempotent` lên API chuyển khoản dựa trên header `X-Idempotency-Key` lưu Redis.
5. **Transactional Outbox**: Ghi sự kiện giao dịch vào bảng `outbox_events` dưới dạng PENDING.
6. **Outbox Poller (Kafka Publisher)**: Scheduler chạy nền quét mỗi 5 giây gửi sự kiện sang Kafka topic `transaction-events` và cập nhật `PROCESSED`.
7. **Simulators**: APIs giả lập đa luồng Race Simulator và tắt/bật OFFLINE Chaos Simulator.

## 🛠️ Công Nghệ Sử Dụng
- **Spring Boot 3.3.2 (Web, Scheduling, AOP)**
- **Spring Data JPA & Flyway Migration**
- **Spring Kafka & Spring Data Redis**
- **PostgreSQL / H2 Database**

## 🌐 Các API Endpoints Chính

- `POST /api/v1/wallets/transfer`: API Chuyển tiền (Yêu cầu header `X-Idempotency-Key` và xác thực JWT).
- `GET /api/v1/wallets/ledger`: Lọc danh sách bút toán sổ cái (Phân trang).
- `POST /api/v1/wallets/{id}/freeze`: Đóng băng ví.
- `POST /api/v1/wallets/{id}/unfreeze`: Mở băng ví.
- `GET /api/v1/wallets/monitors/health`: Kiểm tra sức khỏe hệ thống.
- `GET /api/v1/wallets/monitors/fraud`: Phân tích rủi ro rà soát gian lận.
- `POST /api/v1/wallets/simulator/race`: Chạy giả lập race condition đa luồng.
