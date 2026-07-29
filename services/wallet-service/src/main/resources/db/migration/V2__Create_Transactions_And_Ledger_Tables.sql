-- V2__Create_Transactions_And_Ledger_Tables.sql
-- Khởi tạo bảng transactions và bảng ledger_entries để ghi chép nhật ký sổ kế toán kép

-- 1. Tạo bảng Transactions lưu trữ thông tin giao dịch tổng quát
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    from_wallet_id BIGINT NOT NULL,
    to_wallet_id BIGINT NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND' NOT NULL,
    description VARCHAR(255),
    reference VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index cho bảng transactions phục vụ tra cứu nhanh
CREATE INDEX idx_transactions_from_wallet ON transactions(from_wallet_id);
CREATE INDEX idx_transactions_to_wallet ON transactions(to_wallet_id);

-- 2. Tạo bảng Ledger Entries lưu trữ bút toán chi tiết của từng ví (Sổ cái)
CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL, -- DEBIT hoặc CREDIT
    amount DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index cho bảng ledger_entries phục vụ đối soát số dư nhanh và phân trang
CREATE INDEX idx_ledger_wallet_id ON ledger_entries(wallet_id);
CREATE INDEX idx_ledger_transaction_id ON ledger_entries(transaction_id);
