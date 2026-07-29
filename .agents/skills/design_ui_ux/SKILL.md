---
name: design_ui_ux
description: Hướng dẫn thiết kế giao diện UI/UX (Frontend Next.js 15+, Tailwind CSS, Shadcn UI) cho hệ thống ví điện tử Fintech Mini Digital Wallet.
---

# Kỹ năng Thiết kế UI/UX Fintech - Mini Digital Wallet

Skill này hướng dẫn Agent cách thiết kế, phát triển và tối ưu hóa giao diện người dùng Frontend cho hệ thống ví điện tử, đảm bảo tính nhất quán thẩm mỹ với bảng vẽ thiết kế (`mini-wallet-image-screen.png`) và các đặc tả kỹ thuật backend trong hệ thống.

---

## 📁 1. Cấu Trúc Thư Mục Frontend Khuyến Nghị (Next.js 15+ App Router)

```text
frontend/
 ├── src/
 │    ├── app/                   # App router pages (layout.tsx, page.tsx)
 │    │    ├── (auth)/           # Route group cho Authentication (login, register, otp)
 │    │    ├── dashboard/        # Trang Dashboard chính
 │    │    ├── wallets/          # Quản lý ví cá nhân
 │    │    ├── transfer/         # Form chuyển tiền
 │    │    ├── transactions/     # Lịch sử giao dịch & Ledger Explorer
 │    │    ├── monitors/         # Kafka & Redis Dashboards
 │    │    └── simulator/        # Giả lập Race Condition
 │    ├── components/            # UI Components dùng chung
 │    │    ├── ui/               # Shadcn UI primitives (button, card, dialog, toast, etc.)
 │    │    ├── sidebar.tsx       # Thanh điều hướng trái
 │    │    └── timeline.tsx      # Sơ đồ tiến trình chi tiết giao dịch
 │    ├── hooks/                 # Custom React hooks (useSSE, useWallet)
 │    ├── lib/                   # Config API, định dạng tiền tệ, utils
 │    └── styles/
 │         └── globals.css       # File cấu hình CSS cốt lõi (Tailwind)
```

---

## 🎨 2. Hướng Dẫn Phát Triển Các Màn Hình Then Chốt

### 2.1. Authentication (Login & 2FA OTP)
- **Welcome Back (Login)**:
  - Form căn giữa, nền tối (`#0F172A`), thẻ card (`#1E293B`) bo góc tròn, đổ bóng tinh tế.
  - Sử dụng nút màu xanh dương (`#3B82F6`) làm nút hành động chính (Primary Call-to-Action).
  - Tích hợp thêm các nút đăng nhập nhanh qua bên thứ ba (Google, GitHub) với logo tối giản.
- **Two-Factor Authentication (2FA)**:
  - Hiển thị 6 ô nhập mã OTP (sử dụng component OTP input của Shadcn).
  - Tự động chuyển focus khi người dùng nhập số, có đếm ngược thời gian hết hạn mã (ví dụ: `Code will expire in 01:45`) và nút "Resend code" khi hết giờ.

### 2.2. Core Dashboard & Real-time Notification
- **KPI Cards**:
  - Thống kê các thông tin: Total Balance, Today's Volume, Transactions (Success, Pending, Failed).
  - Mỗi chỉ số đi kèm các chỉ báo biến động phần trăm tăng/giảm dạng badge nhỏ gọn (ví dụ: `+3.2% vs yesterday` màu xanh lá).
- **Charts (Line & Doughnut)**:
  - Dùng Recharts để vẽ biểu đồ đường (Line Chart) biểu diễn lượng giao dịch theo thời gian (`Transaction Volume`) với màu xanh dương nhạt mượt mà.
  - Vẽ biểu đồ tròn khuyết (Doughnut Chart) hiển thị tỷ lệ trạng thái giao dịch (`Success`, `Pending`, `Failed`).
- **Real-time Notifications (Server-Sent Events - SSE)**:
  - Lập trình hook `useSSE` kết nối tới Gateway/Notification Service (Cổng `8080/8086`) để lắng nghe sự kiện biến động số dư.
  - Hiển thị thông báo dạng Toast bay ra từ góc phải màn hình, hoặc đẩy vào bảng lịch sử thông báo nhanh kèm âm thanh nhỏ dịu hoặc animation rung chuông nhẹ (`🔔`).

### 2.3. Transfer Form & Idempotency Key
- **Layout 2 Cột**:
  - **Cột Trái (Form nhập liệu)**: Dropdown chọn nguồn ví (From Wallet), người nhận (To Wallet), số tiền (Amount), mô tả (Description), ghi chú tham chiếu (Reference).
  - **Kháng lặp (Idempotency Key Toggle)**:
    - Bố trí một công tắc chuyển đổi (`Switch`) ghi rõ *"Use Idempotency Key"*.
    - Khi bật, sinh mã UUID phiên bản 4 bằng thư viện `crypto.randomUUID()` hoặc frontend helper. 
    - Hiển thị UUID này trong một ô input dạng `readonly` kèm nhãn *"Idempotency Key"* màu xám nhạt để lập trình viên dễ theo dõi.
    - Đính kèm key này vào header `X-Idempotency-Key` của HTTP POST Request chuyển tiền.
  - **Cột Phải (Transfer Summary)**:
    - Hiển thị số tiền gửi định dạng rõ ràng (ví dụ: `2,000,000 VND`).
    - Tính toán trước phí (`Fee`), thuế (`Tax` 10%), tổng số tiền thực trừ (`Total`), ước tính thời gian hoàn thành (`~2 seconds`).
    - Tính toán số dư còn lại (Balance After) dựa trên số dư hiện tại của ví được chọn để cảnh báo tức thì nếu số dư bị âm trước khi người dùng gửi giao dịch.
    - Nút *"Review Transfer"* / *"Confirm Transfer"* kích thước lớn chiếm toàn bộ chiều ngang cột phải.

### 2.4. Transaction Detail & Processing Timeline
- **Chi Tiết Giao Dịch**:
  - Hiển thị đầy đủ thông tin định danh: Transaction ID, Idempotency Key, Kafka Event ID, Ledger ID (Debit/Credit).
- **Processing Timeline**:
  - Trực quan hóa tiến trình bằng một timeline dọc hoặc ngang mô phỏng hành trình của giao dịch đi qua hệ thống phân tán:
    1. **API Gateway**: Đèn chuyển xanh khi request đi qua Gateway (Port `8080`).
    2. **Wallet Service**: Kiểm tra số dư khả dụng và validate thông tin.
    3. **Redis Idempotency Check**: Chốt chặn kiểm tra key kháng lặp trên Redis.
    4. **PostgreSQL (Ledger Write)**: Ghi nhận 2 bút toán Nợ (Debit) và Có (Credit) vào cơ sở dữ liệu.
    5. **Outbox Publisher**: Lưu sự kiện vào Outbox Table để đảm bảo tính nhất quán giao dịch.
    6. **Kafka (Transaction-events)**: Sự kiện chuyển tiền được gửi vào hàng đợi Kafka Topic.
    7. **Notification Service**: Consumer đọc sự kiện từ Kafka và xử lý.
    8. **Completed (SSE)**: Đẩy thông tin giao dịch thành công về client và cập nhật số dư.
  - Mỗi bước hiển thị trạng thái bằng icon (tích xanh `✔` nếu thành công, chấm đỏ `✖` nếu lỗi, hoặc vòng xoay `⏳` khi đang xử lý) cùng với mốc thời gian chi tiết (Timestamp).

### 2.5. Double-Entry Ledger Visualizer (Ledger Explorer)
- **Nguyên tắc Sổ kép**:
  - Hiển thị bảng chi tiết các dòng Ledger Entries (`Ledger ID`, `Transaction ID`, `Account/Wallet`, `Debit`, `Credit`, `Running Balance`, `Created At`).
  - Giao dịch luôn tạo ra tối thiểu 2 dòng bút toán (Một dòng ghi Nợ, một dòng ghi Có):
    - Tài khoản nguồn (Người gửi) ghi nhận số tiền âm tại cột **Debit** (Màu đỏ `#EF4444`, ví dụ: `-2,000,000 VND`).
    - Tài khoản đích (Người nhận) ghi nhận số tiền dương tại cột **Credit** (Màu xanh lá `#10B981`, ví dụ: `+2,000,000 VND`).
  - Cung cấp cơ chế đối soát: tổng cột Debit phải luôn bằng tổng cột Credit (`Total Debit = Total Credit`) ở cuối trang hoặc bộ lọc.

### 2.6. Infrastructure & System Monitors
- **Kafka & Redis Monitor**:
  - **Kafka**: Hiển thị bảng danh sách các topic hiện tại. Các chỉ số quan trọng là `Lag` (độ trễ xử lý) và số lượng `Consumers`. Bảng màu động: Lag > 100 hiển thị màu vàng, Lag > 1000 hiển thị màu đỏ nhấp nháy cảnh báo.
  - **Redis**: Biểu đồ tốc độ xử lý yêu cầu (Operations/Second), tỷ lệ cache hit/miss, hiển thị danh sách các Idempotency Key kèm đồng hồ đếm ngược TTL (Time To Live).
- **System Health Status**:
  - Hiển thị sơ đồ dịch vụ dạng khối kèm kết nối mạng.
  - Trạng thái từng service (API Gateway, Auth Svc, Wallet Core Svc, PostgreSQL, Redis, Kafka, Notification Svc) dùng chấm đèn màu:
    - 🟢 Xanh lá: Hoạt động bình thường (Healthy).
    - 🟡 Vàng: Quá tải, lag hoặc có cảnh báo (Warning).
    - 🔴 Đỏ: Mất kết nối hoặc dịch vụ bị dừng (Critical).

### 2.7. Concurrency & Chaos Simulator
- **Race Condition Simulator**:
  - Thiết kế bảng điều khiển cho phép cấu hình tham số: `Number of Threads` (số lượng request bắn đồng thời), `Transfer Amount` (số tiền mỗi giao dịch), và nút `Start Simulation`.
  - Gọi API mô phỏng (bắn đồng loạt nhiều HTTP requests cùng 1 Idempotency Key hoặc các Idempotency Key khác nhau để test tính kháng lặp và race conditions).
  - Hiển thị kết quả bằng bảng đếm số lượng giao dịch thành công (Success) và thất bại (Failed) chạy realtime, đi kèm kiểm chứng số dư cuối cùng: `Balance Correct: YES` (màu xanh lá) để chứng tỏ hệ thống backend đã xử lý lock chính xác.
- **Chaos & Recovery Simulator**:
  - Cho phép người dùng bấm nút mô phỏng "Crash Kafka Consumer", "Stop Notification Svc" hoặc "Disconnect Redis".
  - Hiển thị trực quan tác động của lỗi lên Timeline giao dịch (giao dịch bị Pending ở khâu Kafka hoặc Notification).
  - Bấm nút "Recover" để bật lại dịch vụ và chứng kiến hệ thống tự động retry xử lý các sự kiện còn tồn đọng nhờ Outbox Pattern và cập nhật trạng thái về Completed mà không làm mất mát hay sai lệch dữ liệu.

---

## ⚡ 3. Các Hiệu Ứng Thẩm Mỹ Nâng Cao (Premium Styling)

1. **Micro-animations**:
   - Sử dụng Framer Motion hoặc CSS Keyframes để làm mượt mà chuyển động thay đổi số dư (Số tiền tăng/giảm chạy nhảy số tự động).
   - Thêm hiệu ứng phát sáng nhẹ (Glow Effect) cho các nút bấm chính và các chấm trạng thái hoạt động.
2. **Glassmorphism**:
   - Áp dụng nền mờ đục `backdrop-blur-md` kết hợp màu nền bán trong suốt `bg-slate-900/80` và viền xám siêu mảnh `border-slate-800` cho các thẻ modal và dropdown menu.
3. **Responsive Grid**:
   - Thiết kế dạng lưới tự động thích ứng. Trên màn hình Mobile, Sidebar sẽ thu gọn thành menu Hamburger, các biểu đồ sẽ chuyển thành dạng khối cuộn dọc hoặc tab ẩn/hiển thị.
