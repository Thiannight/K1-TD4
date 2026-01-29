INSERT INTO ingredient (id, name, price, category, id_dish)
VALUES (1, 'Laitue', 800.00, 'VEGETABLE', 1),
       (2, 'Tomate', 600.00, 'VEGETABLE', 1),
       (3, 'Poulet', 4500.00, 'ANIMAL', 2),
       (4, 'Chocolat', 3000.00, 'OTHER', 4),
       (5, 'Beurre', 2500.00, 'DAIRY', 4);

INSERT INTO dish (id, name, dish_type)
VALUES (1, 'Salade Fraiche', 'START'),
       (2, 'Poulet grille', 'MAIN'),
       (3, 'Riz au legumes', 'MAIN'),
       (4, 'Gateau au chocolat', 'DESSERT'),
       (5, 'Salade de fruit', 'DESSERT');

insert into dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit)
values (1, 1, 1, 0.20, 'KG'),
       (2, 1, 2, 0.15, 'KG'),
       (3, 2, 3, 1.00, 'KG'),
       (4, 4, 4, 0.30, 'KG'),
       (5, 4, 5, 0.20, 'KG');

insert into stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
values (1, 1, 5.0, 'IN', 'KG', '2024-01-05 08:00'),
       (2, 1, 0.2, 'OUT', 'KG', '2024-01-06 12:00'),
       (3, 2, 4.0, 'IN', 'KG', '2024-01-05 08:00'),
       (4, 2, 0.15, 'OUT', 'KG', '2024-01-06 12:00'),
       (5, 3, 10.0, 'IN', 'KG', '2024-01-04 09:00'),
       (6, 3, 1.0, 'OUT', 'KG', '2024-01-06 13:00'),
       (7, 4, 3.0, 'IN', 'KG', '2024-01-05 10:00'),
       (8, 4, 0.3, 'OUT', 'KG', '2024-01-06 14:00'),
       (9, 5, 2.5, 'IN', 'KG', '2024-01-05 10:00'),
       (10, 5, 0.2, 'OUT', 'KG', '2024-01-06 14:00');

-- Reset sequences to avoid conflicts
SELECT setval('ingredient_id_seq', COALESCE((SELECT MAX(id) FROM ingredient), 1));
SELECT setval('dish_id_seq', COALESCE((SELECT MAX(id) FROM dish), 1));
SELECT setval('dish_ingredient_id_seq', COALESCE((SELECT MAX(id) FROM dish_ingredient), 1));
SELECT setval('order_id_seq', COALESCE((SELECT MAX(id) FROM "order"), 1));
SELECT setval('dish_order_id_seq', COALESCE((SELECT MAX(id) FROM dish_order), 1));

INSERT INTO restaurant_table (id, table_number) VALUES
    (1, 1),
    (2, 2),
    (3, 3);

SELECT setval('restaurant_table_id_seq', COALESCE((SELECT MAX(id) FROM restaurant_table), 1));