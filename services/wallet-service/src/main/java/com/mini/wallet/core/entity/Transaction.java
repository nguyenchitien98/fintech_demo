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
 * Thực thể đại diện cho một Giao dịch chuyển tiền (Transaction Entity) trong cơ sở dữ liệu `mini_wallet`.
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link Entity}: Đánh dấu đây là thực thể JPA để tự động đồng bộ ánh xạ dữ liệu với DB.</li>
 *   <li>{@link Table}: Ánh xạ thực thể này với bảng `transactions` trong Postgres.</li>
 *   <li>{@link Getter}/{@link Setter}: Lombok tự động sinh code getter/setter.</li>
 * </ul>
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    /** Khóa chính tự động tăng của giao dịch. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã ví gửi tiền (from_wallet_id). */
    @Column(name = "from_wallet_id", nullable = false)
    private Long fromWalletId;

    /** Mã ví nhận tiền (to_wallet_id). */
    @Column(name = "to_wallet_id", nullable = false)
    private Long toWalletId;

    /** Số tiền giao dịch. Bắt buộc dùng BigDecimal để đảm bảo độ chính xác tuyệt đối. */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** Đơn vị tiền tệ, ví dụ: 'VND'. */
    @Column(nullable = false, length = 10)
    private String currency = "VND";

    /** Nội dung mô tả giao dịch. */
    @Column(length = 255)
    private String description;

    /** Thông tin tham chiếu/mã tham chiếu giao dịch. */
    @Column(length = 100)
    private String reference;

    /** Trạng thái giao dịch: 'PENDING', 'SUCCESS', hoặc 'FAILED'. */
    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    /** Thời điểm giao dịch được tạo ra trên hệ thống. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật trạng thái giao dịch gần nhất. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
