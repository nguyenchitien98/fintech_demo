package com.mini.wallet.core.service;

import com.mini.wallet.core.entity.OutboxEvent;
import com.mini.wallet.core.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tiến trình quét chạy nền xuất bản sự kiện giao dịch (OutboxPoller Scheduler).
 * Triển khai mô hình **Transactional Outbox Pattern** để đảm bảo tính nhất quán dữ liệu cuối cùng
 * giữa PostgreSQL Ledger và Kafka Message Broker.
 *
 * <p><strong>Tại sao sử dụng @Component:</strong> Đăng ký làm Spring bean để kích hoạt quét tự động.
 */
@Component
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Khởi tạo OutboxPoller và tiêm các repositories và Kafka template.
     *
     * @param outboxEventRepository Repository quản lý tương tác bảng outbox_events.
     * @param kafkaTemplate Tiện ích gửi tin nhắn sang Apache Kafka.
     */
    public OutboxPoller(OutboxEventRepository outboxEventRepository, 
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Tiến trình chạy nền quét bảng outbox_events định kỳ mỗi 5 giây (fixedDelay = 5000).
     *
     * <p><strong>Tại sao dùng Transactional Outbox Pattern:</strong> Thay vì gửi trực tiếp lên Kafka
     * trong luồng chuyển khoản (dễ gây lỗi mất tiền nếu Broker sập hoặc mạng lag làm rollback transaction ví),
     * ta lưu sự kiện vào DB trước, sau đó dùng Poller quét độc lập gửi bất đồng bộ. Đảm bảo "At-least-once delivery".
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollOutboxEvents() {
        // 1. Quét toàn bộ sự kiện có trạng thái PENDING
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus("PENDING");
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        System.out.println(">>> [Outbox Poller] Phát hiện " + pendingEvents.size() + " sự kiện PENDING cần gửi Kafka...");

        for (OutboxEvent event : pendingEvents) {
            try {
                // 2. Gửi sự kiện sang Kafka topic 'transaction-events'
                // Sử dụng get() để chờ phản hồi đồng bộ nhằm cập nhật trạng thái DB chuẩn xác
                kafkaTemplate.send("transaction-events", event.getAggregateId(), event.getPayload()).get();
                
                // 3. Cập nhật trạng thái sự kiện thành PROCESSED nếu gửi thành công
                event.setStatus("PROCESSED");
                outboxEventRepository.save(event);
                
                System.out.println(">>> [Outbox Poller] Gửi sự kiện Kafka thành công cho Transaction ID: " + event.getAggregateId());
            } catch (Exception ex) {
                // 4. Nếu gửi lỗi (ví dụ: Kafka Broker offline), chuyển sang trạng thái FAILED
                event.setStatus("FAILED");
                outboxEventRepository.save(event);
                
                System.err.println(">>> [Outbox Poller] Lỗi gửi sự kiện Kafka: " + ex.getMessage());
            }
        }
    }
}
