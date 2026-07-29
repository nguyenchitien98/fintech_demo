package com.mini.wallet.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể đại diện cho tài khoản Ví điện tử (Wallet Entity) trong cơ sở dữ liệu `mini_wallet`.
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link Entity}: Đánh dấu lớp này là một thực thể JPA để JPA quản lý và ánh xạ vào database.</li>
 *   <li>{@link Table}: Định rõ ánh xạ thực thể này với bảng `wallets` trong PostgreSQL.</li>
 *   <li>{@link Getter}/{@link Setter}: Lombok tự động sinh code getter/setter lúc compile, giữ mã nguồn gọn gàng.</li>
 * </ul>
 */
@Entity
@Table(name = "wallets")
@Getter
@Setter
public class Wallet {

    /** Khóa chính tự động tăng của tài khoản Ví. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã định danh của người dùng sở hữu ví này (Liên kết logic với bảng users của auth-service). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Số dư tài khoản ví.
     * <p><strong>Quy chuẩn Fintech:</strong> Bắt buộc dùng BigDecimal để tránh sai số dấu phẩy động
     * (float/double) và được lưu trữ với độ chính xác cao (scale = 4) dưới DB.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    /** Đơn vị tiền tệ của ví (ví dụ: 'VND', 'USD'). Mặc định là 'VND'. */
    @Column(nullable = false, length = 10)
    private String currency = "VND";

    /**
     * Trạng thái hoạt động của ví.
     * Nhận giá trị 'ACTIVE' (đang hoạt động) hoặc 'FROZEN' (đang đóng băng).
     */
    @Column(nullable = false, length = 50)
    private String status = "ACTIVE";

    /** Thời điểm khởi tạo ví. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm ví được cập nhật thông tin gần nhất. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
