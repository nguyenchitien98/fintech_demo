package com.mini.wallet.core.repository;

import com.mini.wallet.core.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện tương tác cơ sở dữ liệu cho thực thể Ví (Wallet Repository).
 *
 * <p><strong>Tại sao sử dụng @Repository:</strong> Annotation này chỉ định đây là một thành phần
 * Persistence Layer của Spring, tự động kích hoạt cơ chế dịch mã lỗi SQLException của Spring
 * và cho phép Spring Data JPA tự sinh mã thực thi truy vấn tự động dựa trên tên phương thức.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Tìm tất cả ví điện tử thuộc sở hữu của một người dùng.
     *
     * @param userId Mã định danh của người dùng.
     * @return Danh sách các ví điện tử được tìm thấy.
     */
    List<Wallet> findByUserId(Long userId);

    /**
     * Tìm ví điện tử đầu tiên hoặc mặc định của một người dùng.
     *
     * @param userId Mã định danh của người dùng.
     * @return Optional chứa thông tin ví nếu có.
     */
    Optional<Wallet> findFirstByUserId(Long userId);
}
