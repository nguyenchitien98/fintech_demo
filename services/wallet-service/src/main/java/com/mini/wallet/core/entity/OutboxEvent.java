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

import java.time.LocalDateTime;

/**
 * Thực thể lưu trữ sự kiện cần xuất bản (OutboxEvent Entity).
 * Đóng vai trò là trung tâm lưu trữ tạm thời của mô hình **Transactional Outbox Pattern**.
 * Đảm bảo tính nhất quán dữ liệu cuối cùng khi xuất bản sự kiện sang Kafka.
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link Entity}: Định nghĩa đây là một thực thể JPA.</li>
 *   <li>{@link Table}: Ánh xạ với bảng `outbox_events` lưu trữ sự kiện trong Postgres.</li>
 * </ul>
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
public class OutboxEvent {

    /** Khóa chính tự động tăng của bản ghi sự kiện. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Loại thực thể liên quan đến sự kiện (ví dụ: 'WALLET_TRANSACTION'). */
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    /** Mã định danh của thực thể liên quan đến sự kiện (ví dụ: transactionId). */
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    /** Tên của loại sự kiện phát sinh (ví dụ: 'TRANSACTION_COMPLETED'). */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /**
     * Nội dung dữ liệu sự kiện ở định dạng JSON String.
     * Chứa toàn bộ payload cần gửi sang Kafka.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    /** Trạng thái gửi tin nhắn: 'PENDING', 'PROCESSED', hoặc 'FAILED'. */
    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    /** Thời điểm sự kiện được ghi nhận vào cơ sở dữ liệu. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật trạng thái sự kiện lần cuối. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
