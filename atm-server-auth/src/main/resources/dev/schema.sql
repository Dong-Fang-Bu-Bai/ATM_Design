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
