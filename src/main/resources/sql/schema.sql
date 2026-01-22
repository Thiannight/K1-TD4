CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE stock_movement (
    id UUID PRIMARY KEY,
    ingredient_id INT NOT NULL REFERENCES ingredient(id),
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    movement_datetime TIMESTAMP NOT NULL
);
