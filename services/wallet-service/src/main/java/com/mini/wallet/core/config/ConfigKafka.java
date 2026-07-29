package com.mini.wallet.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Lớp cấu hình hạ tầng Apache Kafka (Kafka Configuration).
 *
 * <p><strong>Tại sao sử dụng @Configuration:</strong> Báo cho Spring container biết đây là lớp
 * cấu hình Spring beans.
 */
@Configuration
public class ConfigKafka {

    /**
     * Khai báo một topic Apache Kafka mới mang tên `transaction-events`.
     *
     * <p><strong>Tại sao khai báo Topic bean:</strong> Khi ứng dụng Spring Boot khởi động,
     * KafkaAdmin sẽ tự động liên hệ với Broker Kafka (nếu kết nối được) và tự động tạo topic này
     * nếu nó chưa tồn tại trên Broker. Cấu hình mặc định:
     * - 3 Partitions: Cho phép tăng tính song song khi tiêu thụ thông tin (scalability).
     * - 1 Replica: Thích hợp cho môi trường local/sandbox.
     *
     * @return Đối tượng NewTopic định nghĩa topic.
     */
    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name("transaction-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
