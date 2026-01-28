package repository;

import Model.*;
import config.DatabaseConnection;
import service.StockService;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        String sql = "INSERT INTO dish (name, dish_type, price) VALUES (?, ?::dish_type, ?) RETURNING id";

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

    public Order saveOrder(Order orderToSave) {
        try (Connection conn = DatabaseConnection.getDBConnection()) {
            conn.setAutoCommit(false);

            // 1. Stock Verification
            for (DishOrder item : orderToSave.getDishOrders()) {
                Dish dish = findDishById(item.getDish().getId());
                if (dish == null) {
                    throw new RuntimeException("Dish not found: " + item.getDish().getId());
                }

                for (DishIngredient ing : dish.getIngredients()) {
                    Ingredient ingredient = findIngredientById(ing.getIngredient().getId());
                    if (ingredient == null) {
                        throw new RuntimeException("Ingredient not found: " + ing.getIngredient().getId());
                    }

                    double currentQty = stockService.getStockValueAt(ingredient, Instant.now()).getQuantity();
                    double requiredQty = ing.getQuantityRequired() * item.getQuantity();

                    if (currentQty < requiredQty) {
                        throw new RuntimeException("Insufficient stock for ingredient: " + ingredient.getName());
                    }
                }
            }

            // 2. Save Order
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO \"order\" (reference, creation_datetime) VALUES (?, ?) RETURNING id"
            );
            stmt.setString(1, orderToSave.getReference());
            stmt.setTimestamp(2, Timestamp.from(orderToSave.getCreationDatetime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) orderToSave.setId(rs.getInt(1));

            conn.commit();
            return orderToSave;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
}