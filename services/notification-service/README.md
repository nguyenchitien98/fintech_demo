# Notification Service

Dịch vụ Thông báo (`notification-service`) chạy tại cổng `8086`, đóng vai trò tiêu thụ các sự kiện biến động số dư từ Apache Kafka topic `transaction-events` và truyền phát thông báo trực tiếp tới trình duyệt Web của Client thông qua công nghệ Server-Sent Events (SSE).

## ⚙️ Các Chức Năng Chính

1. **Kafka Event Consumer**: Đăng ký Consumer Group `notification-group` lắng nghe và xử lý tin nhắn sự kiện chuyển tiền thành công từ Kafka topic `transaction-events`.
2. **Server-Sent Events (SSE)**: Cung cấp API endpoint `/api/v1/notifications/stream` để duy trì kết nối HTTP lâu dài (Long-lived connection) với client Next.js.
3. **Phát tin nhắn (Broadcast)**: Khi nhận tin nhắn từ Kafka, chuyển đổi thông tin sang dạng JSON thông báo và truyền phát tới tất cả các client Web đang duy trì kết nối SSE thời gian thực.

## 🛠️ Công Nghệ Sử Dụng
- **Spring Boot 3.3.2 (Web)**
- **Spring Kafka (Consumer)**
- **SseEmitter Web MVC Engine**

## 🌐 Các API Endpoints Chính
- `GET /api/v1/notifications/stream`: Endpoint kết nối Server-Sent Events phát stream thông báo. Bật CORS toàn cục cho phép client cổng 3000 kết nối trực tiếp.
