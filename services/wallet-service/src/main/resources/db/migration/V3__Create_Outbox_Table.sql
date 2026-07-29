-- V3__Create_Outbox_Table.sql
-- Khởi tạo bảng outbox_events để thực hiện mô hình Transactional Outbox Pattern

CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL, -- Ví dụ: 'WALLET_TRANSACTION'
    aggregate_id VARCHAR(100) NOT NULL,   -- Mã định danh transactionId
    event_type VARCHAR(100) NOT NULL,     -- Ví dụ: 'TRANSACTION_COMPLETED'
    payload TEXT NOT NULL,                -- Chứa toàn bộ nội dung sự kiện ở dạng JSON String
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL, -- PENDING, PROCESSED, FAILED
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index phục vụ quét nhanh các dòng chưa xử lý (PENDING) định kỳ
CREATE INDEX idx_outbox_events_status ON outbox_events(status);
