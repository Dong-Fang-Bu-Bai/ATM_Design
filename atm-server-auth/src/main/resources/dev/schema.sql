DROP TABLE IF EXISTS transaction_record;
DROP TABLE IF EXISTS bank_card;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS customer;

CREATE TABLE customer (
  id BIGINT PRIMARY KEY,
  customer_name VARCHAR(50) NOT NULL,
  id_card VARCHAR(18) NOT NULL,
  phone VARCHAR(20),
  create_time TIMESTAMP NOT NULL
);

CREATE TABLE account (
  id BIGINT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  account_no VARCHAR(32) NOT NULL,
  balance DECIMAL(15, 2) NOT NULL,
  account_type INT NOT NULL
);

CREATE TABLE bank_card (
  id BIGINT PRIMARY KEY,
  card_no VARCHAR(19) NOT NULL UNIQUE,
  password VARCHAR(6) NOT NULL,
  account_id BIGINT NOT NULL
);

CREATE TABLE transaction_record (
  id BIGINT PRIMARY KEY,
  transaction_id VARCHAR(32) NOT NULL UNIQUE,
  account_id BIGINT NOT NULL,
  card_no VARCHAR(19) NOT NULL,
  transaction_type INT NOT NULL,
  amount DECIMAL(15, 2) NOT NULL,
  balance_before DECIMAL(15, 2),
  balance_after DECIMAL(15, 2),
  transaction_status INT NOT NULL,
  target_account_no VARCHAR(32),
  target_bank VARCHAR(50),
  failure_reason VARCHAR(255),
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  completed_at TIMESTAMP
);
