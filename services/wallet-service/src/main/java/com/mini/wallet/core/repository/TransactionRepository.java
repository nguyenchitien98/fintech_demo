package com.mini.wallet.core.repository;

import com.mini.wallet.core.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Giao diện quản lý truy xuất dữ liệu cho thực thể Giao dịch (Transaction Repository).
 *
 * <p><strong>Tại sao sử dụng @Repository:</strong> Annotation này chỉ định đây là một thành phần
 * Persistence Layer của Spring, tự động kích hoạt cơ chế dịch mã lỗi SQLException của Spring
 * và cho phép Spring Data JPA tự sinh mã thực thi truy vấn tự động dựa trên tên phương thức.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
