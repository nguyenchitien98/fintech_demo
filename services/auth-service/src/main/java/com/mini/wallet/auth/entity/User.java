package com.mini.wallet.auth.entity;

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
 * Thực thể đại diện cho tài khoản người dùng (User Entity) trong cơ sở dữ liệu `mini_auth`.
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link Entity}: Đánh dấu lớp này là một thực thể JPA để tự động ánh xạ với bảng cơ sở dữ liệu.</li>
 *   <li>{@link Table}: Chỉ định tên bảng tương ứng trong DB (bảng `users`).</li>
 *   <li>{@link Getter}/{@link Setter}: Lombok tự động sinh các hàm Getter và Setter tránh boilerplate code.</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    /** Khóa chính của bảng người dùng, tự động tăng theo cơ chế BIGSERIAL của PostgreSQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Địa chỉ email của người dùng, đóng vai trò là tên đăng nhập độc nhất. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Mật khẩu đã được mã hóa của người dùng. */
    @Column(nullable = false)
    private String password;

    /** Trạng thái kích hoạt tài khoản. True nghĩa là tài khoản đang hoạt động bình thường. */
    @Column(nullable = false)
    private Boolean active = true;

    /** Thời điểm tài khoản được khởi tạo thành công trên hệ thống. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm thông tin tài khoản được cập nhật lần gần nhất. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
