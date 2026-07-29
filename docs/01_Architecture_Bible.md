# Kiến Trúc Hệ Thống Ví Điện Tử (Architecture Bible)

Tài liệu này đặc tả chi tiết kiến trúc hạ tầng và sơ đồ kết nối của **Mini Digital Wallet Platform**.

---

## 1. Bản Đồ Dịch Vụ (Service Map)

Hệ thống được thiết kế theo mô hình Microservices phân tách rạch ròi bằng Spring Cloud Gateway:

```
                       ┌─────────────────────────┐
                       │  Next.js 15+ Frontend   │
                       └────────────┬────────────┘
                                    │ (REST API / SSE)
                                    ▼
                       ┌─────────────────────────┐
                       │   Spring Cloud Gateway  │
                       │     (Port 8080)         │
                       └────────────┬────────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│   Auth Service   │       │ Wallet Core Svc  │       │ Notification Svc │
│   (Port 8081)    │       │   (Port 8082)    │       │   (Port 8086)    │
└──────────────────┘       └─────────┬────────┘       └─────────▲────────┘
                                     │                          │
                                     │ Event:                   │ Consumer:
                                     │ transaction-events       │ transaction-events
                                     ▼                          │
                               ┌──────────────────┐             │
                               │   Apache Kafka   ├─────────────┘
                               └──────────────────┘
```

---

## 2. Danh Sách Các Services & Cổng Mặc Định

1.  **Gateway Service (`gateway-service` - Cổng 8080)**:
    - Điểm tiếp nhận request tập trung.
    - Thực hiện kiểm tra JWT xác thực và Rate Limiting qua Redis.
2.  **Authentication Service (`auth-service` - Cổng 8081)**:
    - Quản lý đăng ký, đăng nhập người dùng.
    - Cấp phát JWT và quản lý danh sách token bị thu hồi (Blacklist) trên Redis.
3.  **Wallet Core Service (`wallet-service` - Cổng 8082)**:
    - Xử lý Ví, Nạp tiền, Rút tiền, Chuyển tiền.
    - Lập sổ kế toán kép (Double-entry Ledger), áp dụng khóa đồng thời tránh race condition.
    - Sản xuất sự kiện (Producer) bắn tin nhắn vào Kafka Topic.
4.  **Notification Service (`notification-service` - Cổng 8086)**:
    - Tiêu thụ sự kiện giao dịch (Consumer) từ Kafka.
    - Đẩy thông báo thời gian thực về Next.js Client qua Server-Sent Events (SSE).

---

## 3. Hạ Tầng Dùng Chung (Infrastructure Stack)

- **Database**: PostgreSQL 16 (Dùng riêng cơ sở dữ liệu `mini_auth` và `mini_wallet` để thực hiện Database-per-service).
- **Caching & Lock Engine**: Redis 7.
- **Message Broker**: Apache Kafka (Topic: `transaction-events`).
