-- V1__Create_Wallets_Table.sql
-- Khởi tạo bảng wallets lưu trữ thông tin ví điện tử của người dùng

CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance DECIMAL(19, 4) DEFAULT 0.0000 NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND' NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);
