-- 1. Create the orders table first
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        client_id BIGINT,
                        order_date DATETIME(6),
                        price DECIMAL(19, 2),
                        status VARCHAR(20) NOT NULL,
                        CONSTRAINT fk_orders_client FOREIGN KEY (client_id) REFERENCES client_profiles(id)
);

-- 2. Create the book_items table (The Join Table)
CREATE TABLE book_items (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            order_id BIGINT,
                            book_id BIGINT,
                            quantity INT,
                            CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                            CONSTRAINT fk_items_book FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 3. Add indexes for faster searching in the Employee Cabinet
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_book_items_order ON book_items(order_id);