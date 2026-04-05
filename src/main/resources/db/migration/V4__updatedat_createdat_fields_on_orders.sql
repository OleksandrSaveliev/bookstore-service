ALTER TABLE orders
    ADD COLUMN created_at DATETIME,
    ADD COLUMN updated_at DATETIME;


UPDATE orders SET created_at = order_date, updated_at = order_date;

ALTER TABLE orders
    MODIFY COLUMN created_at DATETIME NOT NULL;