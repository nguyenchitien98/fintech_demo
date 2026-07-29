# API Gateway Service

Dịch vụ Cổng API Gateway (`gateway-service`) hoạt động tại cổng `8080`, đóng vai trò là cửa ngõ bảo mật duy nhất cho toàn bộ hệ thống ví, thực hiện định tuyến động, rà soát JWT Blacklist phi chặn (Reactive Redis) và giới hạn tốc độ truy cập Rate Limiting.

## ⚙️ Các Chức Năng Chính

1. **Routing định tuyến**: Định tuyến các yêu cầu API tương ứng:
   - `/api/v1/auth/**` ➔ `http://localhost:8081` (auth-service)
   - `/api/v1/wallets/**` ➔ `http://localhost:8082` (wallet-service)
2. **Xác thực JWT**: Bộ lọc `JwtAuthFilter` giải mã token, xác minh tính toàn vẹn và trích xuất thông tin người dùng đưa vào Header `X-User-Email` chuyển xuống cho các microservices phía sau.
3. **Chặn Blacklist Token**: Tra cứu Reactive Redis xem token có nằm trong blacklist (do người dùng đã logout) hay không. Nếu có lập tức từ chối truy cập (401 Unauthorized) mà không gây nghẽn luồng Netty.
4. **Rate Limiting (Token Bucket)**: Tích hợp Redis Rate Limiter cấu hình giới hạn tối đa 10 request/giây trên mỗi địa chỉ IP Client truy cập để bảo vệ hệ thống trước tấn công DDOS.

## 🛠️ Công Nghệ Sử Dụng
- **Spring Cloud Gateway (Reactive WebFlux)**
- **Spring Data Redis Reactive (StringRedisTemplate)**
- **Netty Server**
