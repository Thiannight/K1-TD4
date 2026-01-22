CREATE TYPE category_type AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');

CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    price numeric(10,2) NOT NULL,
    category categoty_type NOT NULL
);

CREATE TABLE stock_movement (
    id UUID PRIMARY KEY,
    ingredient_id INT NOT NULL REFERENCES ingredient(id),
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    movement_datetime TIMESTAMP NOT NULL
);

CREATE TABLE "order"(
    id SERIAL PRIMARY KEY,
    reference VARCHAR(255) NOT NULL,
    creationDatetime TIMESTAMP NOT NULL
);

CREATE TABLE dish(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dishType dish_type NOT NULL
);

CREATE TABLE dish_order (
    id SERIAL PRIMARY KEY,
    id_order INTEGER REFERENCES "order"(id),
    id_dish INTEGER REFERENCES dish(id),
    quantity INTEGER NOT NULL
);