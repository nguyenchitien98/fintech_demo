# Quy Ước Lập Trình Java Nâng Cao (Advanced Senior Java Conventions)

Tài liệu này tổng hợp các kỹ thuật lập trình nâng cao bắt buộc áp dụng trong dự án để giải quyết các bài toán Fintech đặc thù.

---

## 1. Kiểm Soát Bất Đồng Bộ & Khóa (Concurrency & Locking)
Để tránh hiện tượng rút tiền quá hạn mức hoặc trừ tiền trùng lặp khi có nhiều luồng cùng gọi API đồng thời (Race Condition):
1.  **Pessimistic Write Lock**: Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` trong Spring Data JPA để khóa dòng dữ liệu của Ví ngay từ mức DB.
    ```java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(Long id);
    ```
2.  **Distributed Lock (Khóa phân tán)**: Trong trường hợp kiểm tra giao dịch ở mức Gateway hoặc điều phối cụm microservices, sử dụng **Redisson** để lấy khóa phân tán. Key của khóa phân tán nên có cấu trúc: `lock:wallet:[wallet_id]`.

---

## 2. Thiết Kế Idempotency API (Kháng lặp)
Khi một yêu cầu thanh toán bị gửi lặp lại (do click đúp, rớt mạng hoặc retry từ client), hệ thống bắt buộc phải xử lý đúng 1 lần duy nhất:
*   Mọi API chuyển tiền/nạp tiền phải nhận header `X-Idempotency-Key` (UUID).
*   Sử dụng **Spring AOP** để viết khía cạnh `@Idempotent` tự động chặn request:
    *   **Bước 1**: Kiểm tra Key trên Redis. Nếu đang ở trạng thái `PROCESSING` hoặc `COMPLETED`, chặn luồng và trả về kết quả ngay lập tức.
    *   **Bước 2**: Nếu chưa có, set Key vào Redis với giá trị `PROCESSING` và TTL = 30 giây.
    *   **Bước 3**: Sau khi phương thức thực thi thành công, cập nhật trạng thái Key thành `COMPLETED` kèm payload phản hồi, nâng TTL lên 24 giờ.

---

## 3. Virtual Threads (Java 21 Project Loom)
Bật cấu hình Virtual Threads trong tất cả các ứng dụng Spring Boot 3:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```
Tận dụng Virtual Threads để xử lý các tác vụ I/O-bound (gọi HTTP API mô phỏng, truy vấn DB, bắn message sang Kafka) với hiệu năng vượt trội và chi phí tài nguyên cực thấp so với Platform Threads truyền thống.
