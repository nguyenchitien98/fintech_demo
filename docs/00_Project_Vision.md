# Mini Digital Wallet - Tầm Nhìn Dự Án (Project Vision)

Tài liệu này định nghĩa tầm nhìn, phạm vi nghiệp vụ và mục tiêu kỹ thuật cốt lõi của hệ thống **Mini Digital Wallet Platform (Fintech Core System)**.

---

## 1. Tuyên Bố Tầm Nhìn (Vision Statement)
Xây dựng một hệ thống ví điện tử mô phỏng (Sandbox Simulation) hoàn chỉnh, có khả năng xử lý giao dịch với độ chính xác tuyệt đối (Data Integrity), kháng lặp giao dịch (Idempotency), và chịu tải cao (High Concurrency) thông qua công nghệ Java 21 Virtual Threads, Redis và Kafka.

Dự án tập trung giải quyết các bài toán hóc búa nhất của ngành Fintech/Banking:
*   **Sổ kế toán kép (Double-entry Ledger)**: Không bao giờ cập nhật số dư trực tiếp qua cột số dư, đảm bảo mỗi đồng tiền chuyển đi đều được ghi nhận Nợ/Có cân bằng hoàn hảo.
*   **Tránh rút tiền âm / Bán vượt (Race Conditions)**: Chặn đứng tình trạng trừ tiền 2 lần khi người dùng gửi 2 yêu cầu rút tiền đồng thời cùng milisecond.
*   **Độ bền bỉ & Giao dịch phân tán (Distributed Transactions & Saga)**: Đồng bộ các khâu xử lý giao dịch bất đồng bộ qua hệ thống Message Queue Kafka và cơ chế Outbox Pattern.

---

## 2. Các Trụ Cột Công Nghệ & Kỹ Thuật Cốt Lõi
1.  **Sổ kế toán kép (Double-Entry Bookkeeping)**: Lưu vết lịch sử chuyển tiền dưới dạng các dòng nhật ký (Ledger Entries). Mỗi ví là một thực thể tài sản, tổng tài sản trong hệ thống luôn bảo toàn.
2.  **Kháng lặp (Idempotency)**: Sử dụng Redis làm chốt chặn kiểm tra `X-Idempotency-Key` từ Gateway. Đảm bảo client retry 10 lần thì hệ thống cũng chỉ trừ tiền đúng 1 lần.
3.  **Khóa đồng thời (Locking mechanism)**:
    *   **Pessimistic Write Lock** trên Database phục vụ giao dịch chuyển tiền trực tiếp.
    *   **Distributed Lock (Redisson)** tại mức Gateway/Service phân phối để xử lý đồng thời quy mô cluster.
4.  **Thông báo biến động số dư Real-time**: Sử dụng Kafka kết hợp Server-Sent Events (SSE) để truyền phát thông tin số dư mới về giao diện Next.js tức thì khi giao dịch hoàn tất.
