CREATE DATABASE mini_dish_db2;

CREATE USER mini_dish_db_manager2 WITH PASSWORD '123456';

GRANT CONNECT ON DATABASE mini_dish_db2 TO mini_dish_db_manager2;

\c mini_dish_db2;

GRANT CREATE ON SCHEMA public TO mini_dish_db_manager2;

GRANT SELECT, INSERT, UPDATE, DELETE
    ON ALL TABLES IN SCHEMA public
    TO mini_dish_db_manager2;

GRANT USAGE, SELECT, UPDATE
    ON ALL SEQUENCES IN SCHEMA public
    TO mini_dish_db_manager2;

ALTER TABLE ingredient OWNER TO mini_dish_db_manager2;
ALTER TABLE stock_movement OWNER TO mini_dish_db_manager2;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES
    TO mini_dish_db_manager2;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES
    TO mini_dish_db_manager2;
