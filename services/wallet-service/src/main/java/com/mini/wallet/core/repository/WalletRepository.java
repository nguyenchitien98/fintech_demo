package com.mini.wallet.core.repository;

import com.mini.wallet.core.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện tương tác cơ sở dữ liệu cho thực thể Ví (Wallet Repository).
 *
 * <p><strong>Tại sao sử dụng @Repository:</strong> Annotation này chỉ định đây là một thành phần
 * Persistence Layer của Spring, tự động kích hoạt cơ chế dịch mã lỗi SQLException của Spring.
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

    /**
     * Truy vấn thông tin ví điện tử đồng thời khóa dòng dữ liệu dưới Database (Lock bi quan).
     *
     * <p><strong>Tại sao sử dụng @Lock(LockModeType.PESSIMISTIC_WRITE):</strong> Annotation này kích hoạt
     * câu lệnh SQL SELECT ... FOR UPDATE. Cơ sở dữ liệu sẽ khóa dòng dữ liệu của ví này lại,
     * ngăn chặn bất kỳ luồng giao dịch đồng thời nào khác đọc ghi vào ví này cho đến khi giao dịch hiện tại
     * hoàn tất (commit hoặc rollback). Nhó đó, ngăn chặn hoàn toàn lỗi Race Condition (Ví dụ:
     * người dùng rút tiền 2 lần đồng thời vượt hạn mức số dư).
     *
     * @param id Mã định danh duy nhất của tài khoản ví.
     * @return Optional chứa ví được khóa thành công.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);
}
