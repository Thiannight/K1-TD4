-- Suppression des tables existantes
DROP TABLE IF EXISTS dish_order CASCADE;
DROP TABLE IF EXISTS dish_ingredient CASCADE;
DROP TABLE IF EXISTS stock_movement CASCADE;
DROP TABLE IF EXISTS "order" CASCADE;
DROP TABLE IF EXISTS restaurant_table CASCADE;
DROP TABLE IF EXISTS dish CASCADE;
DROP TABLE IF EXISTS ingredient CASCADE;
DROP TYPE IF EXISTS dish_type CASCADE;
DROP TYPE IF EXISTS ingredient_category CASCADE;
DROP TYPE IF EXISTS movement_type CASCADE;
DROP TYPE IF EXISTS unit CASCADE;

CREATE TYPE dish_type AS ENUM ('STARTER', 'MAIN', 'DESSERT');
CREATE TYPE ingredient_category AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE movement_type AS ENUM ('IN', 'OUT');
CREATE TYPE unit AS ENUM ('PCS', 'KG', 'L');

CREATE TABLE restaurant_table (
    id SERIAL PRIMARY KEY,
    table_number INTEGER NOT NULL UNIQUE
);

CREATE TABLE dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    dish_type dish_type,
    selling_price NUMERIC(10, 2)
);

CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    price NUMERIC(10, 2),
    category ingredient_category
);

CREATE TABLE dish_ingredient (
    id SERIAL PRIMARY KEY,
    id_dish INTEGER REFERENCES dish(id),
    id_ingredient INTEGER REFERENCES ingredient(id),
    required_quantity NUMERIC(10, 2),
    unit unit
);

CREATE TABLE stock_movement (
    id SERIAL PRIMARY KEY,
    id_ingredient INTEGER REFERENCES ingredient(id),
    quantity NUMERIC(10, 2),
    type movement_type,
    unit unit,
    creation_datetime TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(255),
    creation_datetime TIMESTAMP WITHOUT TIME ZONE,
    id_table INTEGER REFERENCES restaurant_table(id),
    seating_datetime TIMESTAMP WITHOUT TIME ZONE,
    leaving_datetime TIMESTAMP WITHOUT TIME ZONE

CREATE TABLE dish_order (
    id SERIAL PRIMARY KEY,
    id_order INTEGER REFERENCES "order"(id),
    id_dish INTEGER REFERENCES dish(id),
    quantity INTEGER
);