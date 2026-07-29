# Authentication Service

Dịch vụ Xác thực (`auth-service`) chịu trách nhiệm đăng ký người dùng mới, xác thực thông tin đăng nhập, quản lý mã OTP 2FA và xử lý thu hồi phiên làm việc (Logout Blacklist) thông qua Redis Cache.

## ⚙️ Các Chức Năng Chính

1. **Đăng ký người dùng**: BCrypt băm mật khẩu an toàn và lưu trữ vào PostgreSQL.
2. **Đăng nhập 2 bước**: 
   - Bước 1: Xác thực Email/Password, cấp token tạm thời và tự động sinh mã OTP 6 số lưu trên Redis (TTL 120 giây).
   - Bước 2: Xác thực mã OTP 2FA, cấp phát cặp JWT Access & Refresh Token chính thức.
3. **Đăng xuất (Logout)**: Thu hồi Token bằng cách đưa token hiện tại vào danh sách Blacklist của Redis với TTL bằng thời gian sống còn lại của token.

## 🛠️ Công Nghệ Sử Dụng
- **Spring Boot 3.3.2 (Web)**
- **Spring Data JPA & Flyway Migration**
- **Spring Data Redis (Non-reactive)**
- **Spring Security Crypto (BCrypt)**
- **PostgreSQL / H2 Database**

## 🌐 Các API Endpoints Chính

- `POST /api/v1/auth/register`: Đăng ký tài khoản mới.
- `POST /api/v1/auth/login`: Xác thực Email/Password, yêu cầu 2FA OTP.
- `POST /api/v1/auth/verify-2fa`: Xác minh OTP và cấp JWT.
- `POST /api/v1/auth/logout`: Đăng xuất và blacklist token.
