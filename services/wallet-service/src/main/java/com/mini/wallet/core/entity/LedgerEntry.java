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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể lưu vết bút toán ghi sổ cái (LedgerEntry Entity) cho từng tài khoản ví.
 * Đóng vai trò là thành phần cốt lõi của nguyên tắc **Sổ kế toán kép (Double-entry Ledger)**.
 * Số dư của ví tại một thời điểm có thể được tính toán chính xác bằng tổng giá trị Credit
 * trừ đi tổng giá trị Debit.
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link Entity}: Đánh dấu đây là thực thể JPA đại diện cho một bảng dữ liệu.</li>
 *   <li>{@link Table}: Ánh xạ trực tiếp với bảng `ledger_entries` trong PostgreSQL.</li>
 *   <li>{@link Getter}/{@link Setter}: Lombok tự động sinh code getter/setter.</li>
 * </ul>
 */
@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
public class LedgerEntry {

    /** Khóa chính tự động tăng của dòng bút toán. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã ví điện tử được ghi nhận bút toán này. */
    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    /** Liên kết logic với giao dịch (Transaction) sinh ra dòng bút toán này. */
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    /**
     * Loại bút toán: 'DEBIT' (Nợ - Ví gửi tiền đi) hoặc 'CREDIT' (Có - Ví nhận tiền về).
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * Số tiền ghi nhận.
     * Số tiền này luôn lưu trữ dưới dạng giá trị dương lớn hơn 0.
     * Chiều tăng hay giảm của số dư ví sẽ được quyết định bởi trường type (DEBIT là giảm, CREDIT là tăng).
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** Thời điểm ghi nhận dòng bút toán này vào hệ thống. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
