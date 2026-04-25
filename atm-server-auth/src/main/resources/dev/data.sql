INSERT INTO customer (id, customer_name, id_card, phone, create_time)
VALUES (1, '张三', '110101199001011234', '13800000000', CURRENT_TIMESTAMP);

INSERT INTO account (id, customer_id, account_no, balance, account_type)
VALUES (10001, 1, 'ACC10001', 5000.00, 1);

INSERT INTO bank_card (id, card_no, password, account_id)
VALUES (1, '6222020000000001', '123456', 10001);
