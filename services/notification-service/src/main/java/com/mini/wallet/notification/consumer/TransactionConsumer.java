package com.mini.wallet.notification.consumer;

import com.mini.wallet.notification.controller.NotificationController;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Lớp lắng nghe và tiêu thụ sự kiện giao dịch từ Kafka Broker (TransactionConsumer).
 *
 * <p><strong>Tại sao sử dụng @Component:</strong> Đăng ký class làm Spring component bean
 * để kích hoạt quét và khởi chạy các luồng listener Kafka nền.
 */
@Component
public class TransactionConsumer {

    private final NotificationController notificationController;

    /**
     * Khởi tạo TransactionConsumer.
     *
     * @param notificationController Controller điều phối phát SSE để broadcast tin nhắn.
     */
    public TransactionConsumer(NotificationController notificationController) {
        this.notificationController = notificationController;
    }

    /**
     * Lắng nghe bất kỳ tin nhắn (Message) mới xuất hiện trong Kafka topic `transaction-events`.
     *
     * <p><strong>Tại sao sử dụng @KafkaListener:</strong> Annotation này tự động đăng ký
     * Kafka Consumer Group `notification-group` và chạy một thread lắng nghe topic `transaction-events`.
     * Khi có giao dịch hoàn tất từ wallet-service (qua outbox), tin nhắn lập tức được kéo về đây
     * để đẩy real-time xuống frontend.
     *
     * @param message Nội dung sự kiện giao dịch ở dạng JSON String.
     */
    @KafkaListener(topics = "transaction-events", groupId = "notification-group")
    public void consume(String message) {
        System.out.println("=================================================");
        System.out.println(">>> [Kafka Consumer] NHẬN SỰ KIỆN GIAO DỊCH TỪ KAFKA: ");
        System.out.println(message);
        System.out.println("=================================================");

        // Đẩy tin nhắn thông báo dạng Event Stream xuống client Next.js
        notificationController.broadcast(message);
    }
}
