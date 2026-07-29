package com.mini.wallet.core.repository;

import com.mini.wallet.core.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Giao diện tương tác cơ sở dữ liệu cho thực thể OutboxEvent (OutboxEvent Repository).
 *
 * <p><strong>Tại sao sử dụng @Repository:</strong> Annotation này chỉ định đây là một thành phần
 * Persistence Layer của Spring, tự động kích hoạt cơ chế dịch mã lỗi SQLException của Spring.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Tìm tất cả các sự kiện outbox theo trạng thái xử lý chỉ định (ví dụ: 'PENDING').
     * Phục vụ đắc lực cho Scheduler OutboxPoller truy vấn định kỳ để xử lý gửi tin.
     *
     * @param status Trạng thái xử lý sự kiện (PENDING, PROCESSED, FAILED).
     * @return Danh sách các sự kiện được tìm thấy.
     */
    List<OutboxEvent> findByStatus(String status);
}
