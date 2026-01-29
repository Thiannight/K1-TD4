DROP TABLE IF EXISTS dish_order CASCADE;
DROP TABLE IF EXISTS dish_ingredient CASCADE;
DROP TABLE IF EXISTS stock_movement CASCADE;
DROP TABLE IF EXISTS "order" CASCADE;
DROP TABLE IF EXISTS dish CASCADE;
DROP TABLE IF EXISTS ingredient CASCADE;
DROP TYPE IF EXISTS category_type CASCADE;
DROP TYPE IF EXISTS dish_type CASCADE;

CREATE TYPE category_type AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    category category_type NOT NULL
);

CREATE TABLE dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dish_type dish_type NOT NULL,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0
);

CREATE TABLE dish_ingredient (
    id SERIAL PRIMARY KEY,
    id_dish INTEGER REFERENCES dish(id) ON DELETE CASCADE,
    id_ingredient INTEGER REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity_required DECIMAL(10, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'KG',
    UNIQUE(id_dish, id_ingredient)
);

CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(255) NOT NULL UNIQUE,
    creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dish_order (
    id SERIAL PRIMARY KEY,
    id_order INTEGER REFERENCES "order"(id) ON DELETE CASCADE,
    id_dish INTEGER REFERENCES dish(id),
    quantity INTEGER NOT NULL
);

CREATE TABLE stock_movement (
    id SERIAL PRIMARY KEY,
    id_ingredient INTEGER REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity DECIMAL(10, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'KG',
    type movement_type NOT NULL,
    creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE restaurant_table (
                                  id SERIAL PRIMARY KEY,
                                  table_number INTEGER NOT NULL UNIQUE
);

ALTER TABLE "order"
    ADD COLUMN id_table INTEGER REFERENCES restaurant_table(id),
    ADD COLUMN seating_datetime TIMESTAMP,
    ADD COLUMN leaving_datetime TIMESTAMP;