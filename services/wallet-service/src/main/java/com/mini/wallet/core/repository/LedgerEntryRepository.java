package com.mini.wallet.core.repository;

import com.mini.wallet.core.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Giao diện tương tác cơ sở dữ liệu cho thực thể Bút toán ghi sổ (LedgerEntry Repository).
 *
 * <p><strong>Tại sao sử dụng @Repository:</strong> Annotation này chỉ định đây là một thành phần
 * Persistence Layer của Spring, tự động kích hoạt cơ chế dịch mã lỗi SQLException của Spring.
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    /**
     * Tìm kiếm và phân trang tất cả các dòng bút toán ghi sổ cái liên quan đến một tài khoản ví cụ thể.
     * Phục vụ đắc lực cho màn hình Ledger Explorer lọc thông tin chi tiết của ví.
     *
     * @param walletId Mã định danh tài khoản ví điện tử cần tra cứu.
     * @param pageable Tham số phân trang (trang hiện tại, kích thước trang, sắp xếp).
     * @return Page chứa danh sách các dòng LedgerEntry kết quả.
     */
    Page<LedgerEntry> findByWalletId(Long walletId, Pageable pageable);
}
