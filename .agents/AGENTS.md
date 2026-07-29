# Quy tắc phát triển (Development Rules) - Mini Digital Wallet Monorepo

## 📝 Quy chuẩn tài liệu mã nguồn (Javadoc Standard)

Mọi mã nguồn Java được viết mới hoặc cập nhật trong dự án này phải tuân thủ nghiêm ngặt quy chuẩn Javadoc sau:

1. **Class & Interface Javadoc**:
   - Mỗi Class, Interface, Record hoặc Enum đều phải có mô tả Javadoc ở trên cùng, nêu rõ vai trò nghiệp vụ của nó.
   - **Đặc biệt**: Nếu Class/Interface/Record/Enum có sử dụng bất kỳ Annotation nào (ở mức Class-level như `@RestController`, `@Service`, `@Configuration`, `@Aspect`...), Javadoc bắt buộc phải có phần giải thích lý do tại sao sử dụng annotation đó và vai trò của nó đối với class.
   
2. **Method & Function Javadoc**:
   - Tất cả các phương thức public, protected, package-private (bao gồm cả các phương thức trong REST Controller, Service Layer, Repository Layer) phải có Javadoc mô tả rõ:
     - Chức năng của phương thức.
     - Tham số `@param` (mô tả ý nghĩa của từng tham số truyền vào).
     - Kết quả trả về `@return` (mô tả ý nghĩa của dữ liệu phản hồi).
     - Các ngoại lệ `@throws` (nếu phương thức có ném ngoại lệ nghiệp vụ cụ thể).
   
3. **Mã hóa tiếng Việt**:
   - Viết Javadoc bằng tiếng Việt chuẩn, rõ ràng, dễ hiểu để phục vụ quá trình học tập và giải thích mã nguồn khi phỏng vấn.

4. **Kiểm toán & Tính Kháng lặp (Idempotency)**:
   - Các API thay đổi số dư hoặc thực hiện chuyển tiền bắt buộc phải sử dụng annotation `@Idempotent` để kiểm tra khóa kháng lặp (Idempotency Key) trên Redis nhằm ngăn chặn giao dịch trùng lặp.
   - Luôn sử dụng Sổ kế toán kép (Double-entry Ledger) để ghi nhận biến động số dư. Không được phép chỉnh sửa số dư trực tiếp qua câu lệnh UPDATE mà phải thông qua ghi nhận nợ/có (Debit/Credit).

## 🎨 Quy chuẩn Thiết kế UI/UX (UI/UX Design Standards)

Để đảm bảo hệ thống có một giao diện Frontend (Next.js 15+, Tailwind CSS, Shadcn UI) đồng bộ, hiện đại và gây ấn tượng mạnh mẽ cho người dùng và nhà tuyển dụng (Fintech Core Dashboard), mọi giao diện xây dựng mới hoặc cập nhật bắt buộc phải tuân thủ các quy tắc sau:

1. **Chủ đề & Bảng màu (Theme & Palette)**:
   - Hệ thống sử dụng chế độ tối (**Dark Mode**) làm chủ đạo để mang lại cảm giác cao cấp (Stripe, Revolut style).
   - **Bảng màu chính thức**:
     - Nền ứng dụng (Background): `#0F172A` (Slate 900)
     - Khối/Thẻ chứa nội dung (Card): `#1E293B` (Slate 800)
     - Màu thương hiệu/Chính (Primary): `#3B82F6` (Blue 500)
     - Màu thành công (Success): `#10B981` (Emerald 500) - Dùng cho Credit, giao dịch SUCCESS, trạng thái Healthy.
     - Màu cảnh báo (Warning): `#F59E0B` (Amber 500) - Dùng cho giao dịch PENDING, trạng thái Warning.
     - Màu lỗi/Nguy hiểm (Error): `#EF4444` (Red 500) - Dùng cho Debit, giao dịch FAILED, trạng thái Critical/Fraud.
   - **Font chữ**: Sử dụng các font chữ sans-serif hiện đại như `Inter`, `Outfit` hoặc `Roboto`.

2. **Giao diện Giao dịch & Kiểm toán (Transaction & Audit UI)**:
   - **Form chuyển tiền (Transfer Form)**:
     - Bắt buộc có nút chuyển đổi (**Toggle Switch**) cho tùy chọn *"Use Idempotency Key"*.
     - Khi bật toggle này, giao diện phải hiển thị một trường text readonly chứa chuỗi UUID được tạo tự động (`Idempotency Key`). Key này sẽ được đính kèm vào header `X-Idempotency-Key` khi gọi API.
     - Phải có bảng tóm tắt giao dịch (**Transfer Summary**) ở bên phải hoặc ngay dưới form để hiển thị: Phí (Fee), Thuế (Tax), Tổng tiền (Total), Thời gian xử lý dự kiến và Số dư sau giao dịch (Balance After).
   - **Sổ kế toán Ledger Explorer**:
     - Bảng ghi chép nợ có phải hiển thị rõ hai cột `Debit` và `Credit`.
     - Số tiền trong cột `Debit` phải hiển thị màu Đỏ với dấu trừ (ví dụ: `-50,000 VND`).
     - Số tiền trong cột `Credit` phải hiển thị màu Xanh lá với dấu cộng (ví dụ: `+50,000 VND`).
     - Hỗ trợ bộ lọc theo tài khoản ví, khoảng thời gian và tìm kiếm nhanh.
   - **Chi tiết giao dịch & Processing Timeline**:
     - Khi người dùng nhấp vào một giao dịch, giao diện phải hiển thị bảng thông tin chi tiết (Transaction ID, Idempotency Key, Kafka Event ID, Ledger ID, Sender, Receiver).
     - Phải hiển thị một sơ đồ tiến trình dạng cây (**Processing Timeline**) trực quan hóa luồng đi của dữ liệu qua các dịch vụ thời gian thực: `API Gateway` -> `Wallet Service (Validation)` -> `Redis Idempotency Check` -> `PostgreSQL (Ledger Write)` -> `Outbox Publisher` -> `Kafka (Transaction-events)` -> `Notification Service` -> `Completed`.

3. **Hệ thống Giám sát & Bảng điều khiển (Monitors & Dashboards)**:
   - **Kafka Monitor**: Hiển thị danh sách topics (`transaction-events`, `notification-events`, `dead-letter-topic`), số lượng partition, Lag, Consumers hoạt động, Offset hiện tại và trạng thái (Healthy/Warning). Biểu đồ Line Chart hiển thị Messages In/Out per second theo thời gian thực.
   - **Redis Dashboard**: Hiển thị trực quan dung lượng bộ nhớ (Memory Usage), số lượng client kết nối, tỷ lệ Hit Rate, Ops/Sec, danh sách các khóa Idempotency đang lưu trữ (kèm TTL) và các Distributed Locks đang bị khóa.
   - **System Health**: Hiển thị các đèn trạng thái tròn (`🟢` - Healthy, `🟡` - Warning, `🔴` - Critical) cho từng thành phần (API Gateway, Auth Service, Wallet Service, PostgreSQL, Redis, Kafka, Notification Service) cùng lịch sử sự kiện hệ thống (Incident History) bên dưới.
   - **Fraud Detection**: Dashboard thống kê các giao dịch đáng ngờ dựa trên Risk Score (High, Medium, Low), hiển thị phân phối rủi ro dạng Doughnut Chart và danh sách cảnh báo (Recent Alerts).

4. **Tương tác Real-time & Giả lập (Real-time SSE & Simulator)**:
   - **Thông báo Real-time**: Khi có biến động số dư, thông báo phải hiển thị dạng Toast hoặc một bảng thông báo trượt từ góc phải màn hình thông qua kết nối Server-Sent Events (SSE) đến cổng `8086` của `notification-service`.
   - **Race Condition Simulator**: Giao diện cho phép người dùng cấu hình số lượng Thread chạy đồng thời (ví dụ: 10, 50, 100) và số tiền chuyển để thực thi kiểm thử độ chịu tải và tính kháng lặp của API. Kết quả hiển thị thống kê tức thì số giao dịch thành công (Success), thất bại (Failed) và xác nhận số dư ví cuối cùng có chính xác hay không.
