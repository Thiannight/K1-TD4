CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(20) DEFAULT 'kg',
    current_stock DECIMAL(10, 2) DEFAULT 0
);

CREATE TABLE stock_movement (
    id SERIAL PRIMARY KEY,
    ingredient_id INT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    movement_type VARCHAR(10) CHECK (movement_type IN ('IN', 'OUT')),
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE
);

