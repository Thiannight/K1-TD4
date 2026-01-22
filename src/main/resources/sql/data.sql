-- Insertion des Ingredients
INSERT INTO ingredient (name, unit, current_stock) VALUES
    ('Tomate', 'kg', 50),
    ('Fromage', 'kg', 30),
    ('Pâte', 'kg', 100),
    ('Basilic', 'kg', 5),
    ('Huile d olive', 'L', 20);

-- Insertion de mouvements initiaux
INSERT INTO stock_movement (ingredient_id, quantity, movement_type, movement_date) VALUES
    (1, 50, 'IN', '2026-01-01 08:00:00'),
    (2, 30, 'IN', '2026-01-01 08:00:00'),
    (3, 100, 'IN', '2026-01-01 08:00:00'),
    (4, 5, 'IN', '2026-01-01 08:00:00'),
    (5, 20, 'IN', '2026-01-01 08:00:00');

SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM ingredient));
SELECT setval('stock_movement_id_seq', (SELECT MAX(id) FROM stock_movement));