package com.mini.wallet.auth.repository;

import com.mini.wallet.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Giao diện tương tác cơ sở dữ liệu cho thực thể Người dùng (User Repository).
 *
 * <p><strong>Tại sao sử dụng @Repository:</strong> Annotation này chỉ định đây là một thành phần
 * Persistence Layer của Spring, tự động kích hoạt cơ chế dịch mã lỗi SQLException của Spring
 * và cho phép Spring Data JPA tự sinh mã thực thi truy vấn tự động dựa trên tên phương thức.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm kiếm thông tin người dùng dựa trên địa chỉ email.
     *
     * @param email Địa chỉ email cần tìm kiếm.
     * @return Optional chứa thực thể User nếu tìm thấy, hoặc Optional rỗng nếu không tồn tại.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra sự tồn tại của email người dùng trên hệ thống.
     *
     * @param email Địa chỉ email cần kiểm tra.
     * @return true nếu email đã tồn tại, ngược lại trả về false.
     */
    boolean existsByEmail(String email);
}
