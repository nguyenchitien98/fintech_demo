# Kế Hoạch Triển Khai Chi Tiết Các Sprints - Mini Digital Wallet Project

Tài liệu này phân rã lộ trình phát triển hệ thống **Mini Digital Wallet Platform (Fintech Core System)** thành 4 Phase, tương ứng với 8 Sprints chi tiết. Mỗi Sprint kết hợp chặt chẽ việc phát triển API Backend cùng với xây dựng màn hình giao diện Frontend Next.js 15+ tương ứng, đảm bảo đáp ứng đầy đủ 18 màn hình và tính năng mô tả trong [plan_UI_UX.md](file:///c:/Users/Admin/Desktop/duanFintechJava/plan_UI_UX.md) và hình ảnh thiết kế.

---

## 🗺️ PHÂN RÃ CHI TIẾT CÁC PHASE & SPRINTS

### 🏁 Phase 1: Core Domain & Double-Entry Ledger (Tuần 1)

#### 🏃 Sprint 1: Database Setup, Entities JPA, CRUD Wallets & Admin Core API
*   **Mục tiêu**: Thiết lập cơ sở dữ liệu Postgres, các thực thể JPA cho người dùng, ví và viết các API quản trị ví lõi.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [x] Cấu hình JPA, Postgres cho thực thể `User` và `Wallet`.
        - [x] Viết Flyway migration tạo các bảng `users` và `wallets` trong cơ sở dữ liệu `mini_wallet` và `mini_auth`.
        - [x] Viết API Đăng ký người dùng và tự động mở Ví mặc định (Main Wallet) ở trạng thái `ACTIVE`.
        - [x] Viết các API cho ban quản trị: Lấy danh sách toàn bộ ví, danh sách người dùng phục vụ Admin Dashboard.
        - [x] Viết API Đóng băng/Mở băng ví (`/api/v1/wallets/{id}/freeze` và `/unfreeze`) phục vụ màn hình User Detail.
    - **Frontend (UI)**:
        - [x] Khởi tạo dự án Next.js 15+, cấu hình Tailwind CSS và tích hợp thư viện Shadcn UI.
        - [x] Thiết kế khung giao diện chính với Sidebar điều hướng toàn cục (Dashboard, Wallets, Transactions, Transfer, Monitors, Simulator, Admin...).
        - [x] Xây dựng form Đăng ký / Đăng nhập cơ bản.

#### 🏃 Sprint 2: Ledger Entries, Direct Money Transfer & Concurrency Control
*   **Mục tiêu**: Xây dựng nghiệp vụ chuyển tiền áp dụng Sổ kế toán kép (Double-entry Ledger) bảo toàn dữ liệu và cơ chế chống rút tiền âm.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [ ] Thiết lập thực thể `Transaction` và `LedgerEntry` (bút toán ghi Nợ/Có).
        - [ ] Lập trình logic chuyển tiền trong một Transaction Database: Khóa bản ghi ví gửi và nhận bằng Pessimistic Lock (`SELECT ... FOR UPDATE`), tạo bản ghi giao dịch `PENDING`, sau đó ghi nhận 1 dòng DEBIT cho ví gửi và 1 dòng CREDIT cho ví nhận.
        - [ ] Viết API Ledger Explorer (`/api/v1/ledger-entries`) hỗ trợ phân trang, tìm kiếm và đối soát số dư ví.
        - [ ] Viết bài Integration Test giả lập 100 Virtual Threads chuyển tiền đồng thời để kiểm chứng tài khoản không bị âm hoặc trừ tiền sai lệch (Race Condition).
    - **Frontend (UI)**:
        - [ ] Thiết kế màn hình **Ledger Explorer**: Hiển thị bảng chi tiết các bút toán Nợ/Có, trong đó cột `Debit` hiển thị số tiền âm màu Đỏ (ví dụ: `-2,000,000 VND`), cột `Credit` hiển thị số tiền dương màu Xanh lá (ví dụ: `+2,000,000 VND`).

---

### 🏁 Phase 2: Security, Redis Integration & Core Transactions (Tuần 2)

#### 🏃 Sprint 3: Authentication, Session Management & 2FA OTP
*   **Mục tiêu**: Xây dựng hệ thống đăng nhập bảo mật cấp JWT Token, quản lý logout qua Redis và bảo mật 2 lớp OTP.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [ ] Phát triển độc lập `auth-service` xử lý đăng ký, đăng nhập và cấp phát cặp token (Access/Refresh Token).
        - [ ] Tích hợp Redis làm Session Blacklist để thu hồi JWT khi người dùng đăng xuất.
        - [ ] Viết API sinh và gửi mã OTP 2FA mô phỏng, và API xác thực OTP 6 số.
    - **Frontend (UI)**:
        - [ ] Hoàn thiện màn hình **Welcome Back (Login)**: Phong cách tối, card căn giữa, hỗ trợ OAuth nhanh qua Google/GitHub.
        - [ ] Thiết kế màn hình **Two-Factor Authentication (2FA OTP)**: Nhận mã OTP 6 số, đếm ngược thời gian hết hạn mã (01:45) và nút Resend Code.

#### 🏃 Sprint 4: API Gateway, Rate Limiting & Idempotency Engine
*   **Mục tiêu**: Bảo mật Gateway, giới hạn tần suất gọi API và thiết lập bộ lọc kháng lặp giao dịch tự động.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [ ] Cấu hình Spring Cloud Gateway chặn JWT, giải mã thông tin user và đẩy vào Header `X-User-Email` cho các dịch vụ Wallet.
        - [ ] Cấu hình Rate Limiter dùng thuật toán Token Bucket lưu trên Redis tại Gateway.
        - [ ] Lập trình Custom Annotation `@Idempotent` cùng Aspect `IdempotentAspect` trong `common-library` kiểm tra và chống gửi trùng yêu cầu giao dịch trong 30 giây qua Redis dựa trên header `X-Idempotency-Key`.
    - **Frontend (UI)**:
        - [ ] Thiết kế trang **Chuyển tiền (Transfer Money)**:
            - Form nhập thông tin ví gửi, người nhận, số tiền, nội dung.
            - Nút bật tắt (Toggle Switch) cho tùy chọn *"Use Idempotency Key"*. Khi bật, tự động sinh chuỗi UUID v4 hiển thị trong ô text readonly và đính kèm vào header request.
            - Cột **Transfer Summary** hiển thị: Phí (Fee), Thuế (Tax), Tổng cộng (Total), Thời gian xử lý dự kiến và Số dư sau giao dịch (Balance After).

---

### 🏁 Phase 3: Event-Driven Kafka Pipeline & Real-Time SSE (Tuần 3)

#### 🏃 Sprint 5: Transactional Outbox Pattern, Kafka Pipeline & History UI
*   **Mục tiêu**: Tích hợp Apache Kafka để truyền phát sự kiện giao dịch bất đồng bộ, áp dụng Outbox Pattern để tăng tính bền bỉ và tạo giao diện lịch sử.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [ ] Thiết lập Apache Kafka Docker và cấu hình Kafka Producer trong `wallet-service`.
        - [ ] Áp dụng Transactional Outbox Pattern: Khi viết dữ liệu Ledger vào Postgres, đồng thời ghi sự kiện `TransactionCompletedEvent` vào bảng `outbox_events` trong cùng một transaction database.
        - [ ] Viết bộ quét Outbox (Scheduler) đọc bảng và bắn message vào Kafka Topic `transaction-events`.
    - **Frontend (UI)**:
        - [ ] Thiết kế màn hình **Lịch sử giao dịch (Transaction History)**:
            - Bảng dữ liệu chứa thông tin Date & Time, Transaction ID, Type, Amount, Status, Channel.
            - Các bộ lọc theo Status (Success, Pending, Failed), Type, khoảng thời gian và ô tìm kiếm nhanh.
            - Tích hợp nút xuất báo cáo **Export CSV**.

#### 🏃 Sprint 6: Notification Service, Real-time SSE & Detail Timeline UI
*   **Mục tiêu**: Xây dựng dịch vụ thông báo lắng nghe Kafka, đẩy thông tin về Client qua Server-Sent Events và hiển thị chi tiết tiến trình giao dịch.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [ ] Phát triển dịch vụ `notification-service` làm Kafka Consumer tiêu thụ sự kiện từ topic `transaction-events`.
        - [ ] Cung cấp API endpoint Server-Sent Events (SSE) `/api/v1/notifications/stream` để đẩy thông báo biến động số dư tức thời về client.
    - **Frontend (UI)**:
        - [ ] Cài đặt kết nối SSE tại Frontend để hiển thị popup thông báo biến động số dư thời gian thực ở góc phải màn hình.
        - [ ] Thiết kế màn hình **Chi tiết giao dịch (Transaction Detail)**:
            - Hiển thị các ID định danh sâu: Transaction ID, Idempotency Key, Kafka Event ID, Ledger ID (Debit/Credit).
            - Trực quan hóa sơ đồ cây **Processing Timeline** hiển thị luồng đi của dữ liệu qua các dịch vụ theo thời gian thực (API Gateway ➔ Wallet Service ➔ Redis Idempotency ➔ PostgreSQL Ledger ➔ Outbox Publisher ➔ Kafka Broker ➔ Notification Service ➔ Completed) với các dấu tích xanh và mốc thời gian chi tiết.

---

### 🏁 Phase 4: Monitoring, Simulation & Operational Control (Tuần 4)

#### 🏃 Sprint 7: Systems Monitoring, Fraud Detection Backend & Dashboards UI
*   **Mục tiêu**: Xây dựng các API giám sát sức khỏe hệ thống, logic phát hiện gian lận và thiết kế các bảng điều khiển trực quan.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [ ] Viết API giám sát Kafka: Lấy danh sách các topic (`transaction-events`, `notification-events`, `dead-letter-topic`), số lượng partition, Lag, Consumers hoạt động và Offset hiện tại.
        - [ ] Viết API giám sát Redis: Lấy các thông số dung lượng bộ nhớ (Memory Usage), số client kết nối, tỷ lệ Hit Rate, Ops/Sec, danh sách các khóa Idempotency đang lưu trữ (kèm TTL) và các Distributed Locks đang khóa.
        - [ ] Cấu hình Spring Boot Actuator/Health check trả về trạng thái tròn (`🟢` - Healthy, `🟡` - Warning, `🔴` - Critical) của từng dịch vụ.
        - [ ] Viết API Fraud Detection: Tính toán Risk Score dựa trên quy tắc (giao dịch vượt hạn mức, nhiều giao dịch liên tục) và trả về danh sách Alerts rủi ro.
    - **Frontend (UI)**:
        - [ ] Thiết kế trang **Kafka Monitor**: Hiển thị bảng chi tiết các topic và biểu đồ Line Chart thống kê Messages In/Out per second theo thời gian thực.
        - [ ] Thiết kế trang **Redis Dashboard**: Trực quan hóa dung lượng bộ nhớ, client, Ops/Sec và bảng danh sách các khóa Idempotency đang lưu trữ kèm TTL đếm ngược.
        - [ ] Thiết kế trang **System Health**: Hiển thị đèn trạng thái hoạt động tròn của từng dịch vụ và lịch sử sự kiện hệ thống.
        - [ ] Thiết kế trang **Fraud Detection**: Thống kê số lượng High/Medium/Low Risk, Doughnut Chart phân phối rủi ro và danh sách Alerts cảnh báo.

#### 🏃 Sprint 8: Chaos & Race Simulators, Admin Dashboard UI & Production Package
*   **Mục tiêu**: Xây dựng bộ giả lập tải/lỗi hệ thống để demo khả năng chịu tải, giao diện Admin quản trị và đóng gói Docker Compose sản xuất.
*   **Checklist kỹ thuật**:
    - **Backend (API)**:
        - [ ] Viết API giả lập Race Condition: Tiếp nhận số lượng Thread (ví dụ: 10, 50, 100) và số tiền chuyển để bắn đồng thời nhiều luồng gọi API chuyển khoản.
        - [ ] Viết API Chaos Simulator: Ngắt kết nối tạm thời Redis/Kafka/Notification để kiểm thử khả năng chịu lỗi và tự phục hồi dữ liệu qua Outbox Pattern.
    - **Backend (Operational)**:
        - [ ] Viết các Dockerfile đa tầng cho các microservices Java và ứng dụng Frontend Node.
        - [ ] Viết tệp cấu hình `docker-compose.prod.yml` chạy giới hạn cứng tài nguyên (CPU/RAM limit) cho PostgreSQL, Redis, Kafka và các microservices.
    - **Frontend (UI)**:
        - [ ] Thiết kế màn hình **Race Condition Simulator**: Cấu hình Threads, Amount, nút Start, hiển thị số giao dịch Success/Failed và đối soát tính chính xác của số dư cuối cùng.
        - [ ] Thiết kế **Chaos & Recovery Dashboard**: Mô phỏng lỗi tắt/bật Redis/Kafka/Notification, hiển thị trực quan các giao dịch bị Pending hoặc Outbox đang xếp hàng retry. Bấm nút "Recover" để bật lại dịch vụ và xem luồng giao dịch được tiếp tục và hoàn tất tự động.
        - [ ] Thiết kế **Admin Dashboard** & **User Detail**: Quản lý người dùng, ví và thực hiện hành động Đóng băng/Mở băng (Freeze/Unfreeze) ví trực tiếp trên giao diện.
