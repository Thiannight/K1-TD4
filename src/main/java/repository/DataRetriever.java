package repository;

import Model.Ingredient;
import Model.StockMovement;
import config.DatabaseConnection;

import java.sql.*;

public class DataRetriever {

    public Ingredient saveIngredient(Ingredient ingredient) {
        try (Connection conn = DatabaseConnection.getDBConnection()) {
            conn.setAutoCommit(false);

            if (ingredient.getId() == null) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO ingredient(name) VALUES (?) RETURNING id"
                );
                stmt.setString(1, ingredient.getName());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                ingredient = new Ingredient(rs.getInt(1), ingredient.getName());
            }

            for (StockMovement sm : ingredient.getStockMovementList()) {
                PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO stock_movement(id, ingredient_id, quantity, unit, movement_datetime)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (id) DO NOTHING
                """);
                stmt.setObject(1, sm.getId());
                stmt.setInt(2, ingredient.getId());
                stmt.setDouble(3, sm.getQuantity());
                stmt.setString(4, sm.getUnit().name());
                stmt.setTimestamp(5, Timestamp.from(sm.getMovementDatetime()));
                stmt.executeUpdate();
            }

            conn.commit();
            return ingredient;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
