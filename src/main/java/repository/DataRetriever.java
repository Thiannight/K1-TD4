package repository;

import Model.*;
import config.DatabaseConnection;
import service.StockService;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final StockService stockService = new StockService();

    public List<DishIngredient> findDishIngredients(int dishId) {
        List<DishIngredient> ingredients = new ArrayList<>();
        String sql = "SELECT di.id, di.quantity_required, di.unit, i.id as ing_id, i.name, i.price " +
                "FROM dish_ingredient di " +
                "JOIN ingredient i ON di.id_ingredient = i.id " +
                "WHERE di.id_dish = ?";

        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Ingredient ing = new Ingredient();
                ing.setId(rs.getInt("ing_id"));
                ing.setName(rs.getString("name"));
                ing.setPrice(rs.getDouble("price"));

                DishIngredient di = new DishIngredient();
                di.setId(rs.getInt("id"));
                di.setIngredient(ing);
                di.setQuantityRequired(rs.getDouble("quantity_required"));
                di.setUnit(rs.getString("unit"));

                ingredients.add(di);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching ingredients for dish " + dishId, e);
        }
        return ingredients;
    }

    public Dish saveDish(Dish d) {
        String sql = "INSERT INTO dish (name, dish_type, selling_price) VALUES (?, ?::dish_type, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, d.getName());
            stmt.setString(2, d.getDishType() != null ? d.getDishType().name() : "MAIN");
            stmt.setDouble(3, d.getPrice());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                d.setId(rs.getInt(1)); // récupère l'ID généré par la DB
            }

            return d;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du plat: " + d.getName(), e);
        }
    }


    public Ingredient saveIngredient(Ingredient toSave) {
        String sql = """
                INSERT INTO ingredient (name, price, category)
                VALUES (?, ?, ?)
                ON CONFLICT (name) DO UPDATE
                SET price = EXCLUDED.price,
                    category = EXCLUDED.category
                RETURNING id
                """;

        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            // Save or update ingredient
            pstmt.setString(1, toSave.getName());
            pstmt.setDouble(2, toSave.getPrice());
            pstmt.setString(3, toSave.getCategory().name());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                toSave.setId(rs.getInt("id"));
            }

            // Save stock movements
            saveStockMovements(toSave, conn);

            conn.commit();
            return toSave;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving ingredient: " + toSave.getName(), e);
        }
    }

    private void saveStockMovements(Ingredient ingredient, Connection conn) throws SQLException {
        String sql = "INSERT INTO stock_movement (id, id_ingredient, quantity, unit, type, creation_datetime) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO NOTHING";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (StockMovement movement : ingredient.getStockMovementList()) {
                pstmt.setObject(1, movement.getId());
                pstmt.setInt(2, ingredient.getId());
                pstmt.setDouble(3, movement.getQuantity());
                pstmt.setString(4, movement.getUnit().name());
                pstmt.setString(5, "OUT"); // Default type, should be configurable
                pstmt.setTimestamp(6, Timestamp.from(movement.getMovementDatetime()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public Ingredient findIngredientById(int id) {
        String sql = "SELECT * FROM ingredient WHERE id = ?";

        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                // Load stock movements
                loadStockMovements(ingredient, conn);

                return ingredient;
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding ingredient with id: " + id, e);
        }
    }

    private void loadStockMovements(Ingredient ingredient, Connection conn) throws SQLException {
        String sql = "SELECT * FROM stock_movement WHERE id_ingredient = ? ORDER BY creation_datetime";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ingredient.getId());
            ResultSet rs = pstmt.executeQuery();

            List<StockMovement> movements = new ArrayList<>();
            while (rs.next()) {
                StockMovement movement = new StockMovement(
                        rs.getInt("id"),
                        rs.getDouble("quantity"),
                        Unit.valueOf(rs.getString("unit")),
                        rs.getTimestamp("creation_datetime").toInstant(),
                        MovementTypeEnum.valueOf(rs.getString("movement_type"))
                );
                movements.add(movement);
            }
            ingredient.setStockMovementList(movements);
        }
    }



    public Dish findDishById(Integer id) {
        if (id == null) return null;

        String sql = "SELECT * FROM dish WHERE id = ?";

        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setPrice(rs.getDouble("price"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));

                List<DishIngredient> ingredients = findDishIngredients(dish.getId());
                dish.setIngredients(ingredients);

                return dish;
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding dish with id: " + id, e);
        }
    }

    public Order findOrderByReference(String reference) {
        try (Connection conn = DatabaseConnection.getDBConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM \"order\" WHERE reference = ?"
            );
            pstmt.setString(1, reference);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Order not found with reference: " + reference); // [cite: 53]
            }

            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setReference(rs.getString("reference"));
            return order;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order saveOrder(Order orderToSave) {
        if (orderToSave.getTable() == null || orderToSave.getTable().getId() == null) {
            throw new RuntimeException("La table doit être spécifiée pour créer une commande");
        }
        if (orderToSave.getSeatingDatetime() == null) {
            throw new RuntimeException("La date d'installation doit être spécifiée");
        }

        try (Connection conn = DatabaseConnection.getDBConnection()) {
            conn.setAutoCommit(false);

            Integer tableId = orderToSave.getTable().getId();
            Instant seating = orderToSave.getSeatingDatetime();
            Instant leaving = orderToSave.getLeavingDatetime();

            if (!isTableAvailable(conn, tableId, seating, leaving)) {
                List<Integer> availableTableIds = getAvailableTables(conn, seating, leaving);

                if (availableTableIds.isEmpty()) {
                    throw new RuntimeException("Aucune table n'est disponible pour cette période");
                } else {
                    List<String> availableTableNumbers = new ArrayList<>();
                    for (Integer id : availableTableIds) {
                        RestaurantTable t = findTableById(conn, id);
                        if (t != null) {
                            availableTableNumbers.add(String.valueOf(t.getTableNumber()));
                        }
                    }

                    String availableTablesStr = String.join(", ", availableTableNumbers);
                    throw new RuntimeException(
                            "La table " + orderToSave.getTable().getTableNumber() +
                                    " n'est pas disponible. Tables disponibles : " + availableTablesStr
                    );
                }
            }

            String orderSql = """
            INSERT INTO "order" (reference, creation_datetime, id_table, seating_datetime, leaving_datetime) 
            VALUES (?, ?, ?, ?, ?) 
            RETURNING id
            """;

            Integer orderId;
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                pstmt.setString(1, orderToSave.getReference());
                pstmt.setTimestamp(2, Timestamp.from(orderToSave.getCreationDatetime()));
                pstmt.setInt(3, tableId);
                pstmt.setTimestamp(4, Timestamp.from(seating));

                if (leaving != null) {
                    pstmt.setTimestamp(5, Timestamp.from(leaving));
                } else {
                    pstmt.setNull(5, Types.TIMESTAMP);
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    orderId = rs.getInt(1);
                    orderToSave.setId(orderId);
                } else {
                    throw new RuntimeException("Échec de l'insertion de la commande");
                }
            }

            String dishOrderSql = "INSERT INTO dish_order (id_order, id_dish, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(dishOrderSql)) {
                for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                    pstmt.setInt(1, orderId);
                    pstmt.setInt(2, dishOrder.getDish().getId());
                    pstmt.setInt(3, dishOrder.getQuantity());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
            return orderToSave;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de la commande: " + e.getMessage(), e);
        }
    }

    private RestaurantTable findTableById(Connection conn, Integer tableId) throws SQLException {
        String sql = "SELECT id, table_number FROM restaurant_table WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new RestaurantTable(rs.getInt("id"), rs.getInt("table_number"));
            }
            return null;
        }
    }

    private boolean isTableAvailable(Connection conn, Integer tableId,
                                     Instant seatingTime, Instant leavingTime) throws SQLException {
        String sql = """
        SELECT COUNT(*) FROM "order" o 
        WHERE o.id_table = ? 
        AND (
            (o.seating_datetime <= ? AND (o.leaving_datetime IS NULL OR o.leaving_datetime >= ?))
            OR (o.seating_datetime <= ? AND ? <= o.leaving_datetime)
            OR (? <= o.seating_datetime AND o.seating_datetime <= ?)
        )
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            Instant effectiveLeavingTime = leavingTime != null ? leavingTime : Instant.now();

            pstmt.setTimestamp(2, Timestamp.from(seatingTime));
            pstmt.setTimestamp(3, Timestamp.from(effectiveLeavingTime));
            pstmt.setTimestamp(4, Timestamp.from(seatingTime));
            pstmt.setTimestamp(5, Timestamp.from(effectiveLeavingTime));
            pstmt.setTimestamp(6, Timestamp.from(seatingTime));
            pstmt.setTimestamp(7, Timestamp.from(effectiveLeavingTime));

            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private List<Integer> getAvailableTables(Connection conn,
                                             Instant seatingTime, Instant leavingTime) throws SQLException {
        String allTablesSql = "SELECT id FROM restaurant_table ORDER BY table_number";
        List<Integer> allTables = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(allTablesSql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                allTables.add(rs.getInt("id"));
            }
        }

        List<Integer> availableTables = new ArrayList<>();
        for (Integer tableId : allTables) {
            if (isTableAvailable(conn, tableId, seatingTime, leavingTime)) {
                availableTables.add(tableId);
            }
        }

        return availableTables;
    }

    public RestaurantTable findTableByNumber(Integer tableNumber) {
        String sql = "SELECT id, table_number FROM restaurant_table WHERE table_number = ?";

        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tableNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new RestaurantTable(
                        rs.getInt("id"),
                        rs.getInt("table_number")
                );
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la table: " + e.getMessage(), e);
        }
    }

    private Stock getStockValueAt(Ingredient ingredient, Instant t) {
        double total = 0;
        for (StockMovement sm : ingredient.getStockMovementList()) {
            if (!sm.getMovementDatetime().isAfter(t)) {
                total += sm.getQuantity();
            }
        }
        return new Stock(total, Unit.KG);
    }
}