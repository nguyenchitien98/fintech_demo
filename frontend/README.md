# Next.js 15+ Fintech Core Dashboard

Ứng dụng Frontend Dashboard Web được xây dựng bằng Next.js 15+, Tailwind CSS và Lucide Icons để tương tác và giám sát toàn bộ hoạt động của hệ thống ví điện tử lõi.

## ⚙️ Các Trang Chức Năng

1. **Trang đăng nhập & Đăng ký**: Hỗ trợ băm đăng nhập và chuyển trang OTP 2FA có countdown đếm ngược 2 phút tự nhảy focus 6 ô nhập.
2. **Tổng quan (Dashboard Home)**: Hiển thị tổng quan tài khoản ví, giao dịch gần đây và biểu đồ biến động số dư.
3. **Chuyển tiền (/transfer)**: Biểu mẫu chuyển tiền tích hợp Toggle Switch cho *"Use Idempotency Key"*, tự động sinh UUID v4 đính kèm header request và bảng tóm tắt chi phí giao dịch (phí chuyển, thuế VAT 0.1%).
4. **Lịch sử GD (/transactions)**: Phân tách 2 Tabs:
   - *Tab Lịch sử*: Xem nhật ký chuyển tiền. Nhấp vào ID để xem Modal **Processing Timeline** dạng cây dọc thể hiện luồng đi của dữ liệu qua các microservices thời gian thực.
   - *Tab Kiểm toán Sổ cái*: Xem đối soát bút toán Nợ (Debit - Đỏ) / Có (Credit - Xanh) theo nguyên tắc Sổ kế toán kép.
5. **Kafka Monitor (/monitors/kafka)**: Bảng thống kê các topic, phân vùng partition, lag và biểu đồ Line Chart SVG động.
6. **Redis Dashboard (/monitors/redis)**: Theo dõi Client kết nối, Hit Rate, Ops/Sec và danh sách khóa Idempotency Key kèm TTL đếm ngược.
7. **Sức khỏe hệ thống (/monitors/health)**: Giám sát trạng thái hoạt động tròn (Healthy, Warning, Critical) của từng dịch vụ và lịch sử sự kiện hệ thống.
8. **Phát hiện gian lận (/monitors/fraud)**: Bảng Alerts cảnh báo giao dịch đáng ngờ, tính toán Risk Score và biểu đồ Doughnut Chart SVG rủi ro.
9. **Quản trị Admin (/admin)**: Quản lý người dùng, ví (Freeze/Unfreeze) và tích hợp các bộ giả lập Race Simulator, Chaos Simulator kèm hướng dẫn kiểm thử trực tiếp trên giao diện UI.

## 🛠️ Công Nghệ Sử Dụng
- **Next.js 15 (App Router)**
- **React 19 & Tailwind CSS**
- **Lucide Icons**
- **HTML5 EventSource (SSE client)**

## 🚀 Khởi chạy cục bộ
```bash
npm install
npm run dev
```
Truy cập giao diện tại: `http://localhost:3000`.
