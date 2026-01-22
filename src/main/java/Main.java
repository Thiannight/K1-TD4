import Model.Ingredient;
import Model.Stock;
import Model.StockMovement;
import Model.Unit;
import repository.DataRetriever;
import service.StockService;

import java.time.Instant;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        Ingredient rice = new Ingredient(null, "Rice");

        rice.addStockMovement(
                new StockMovement(UUID.randomUUID(), 10, Unit.KG, Instant.parse("2024-01-01T10:00:00Z"))
        );
        rice.addStockMovement(
                new StockMovement(UUID.randomUUID(), -3, Unit.KG, Instant.parse("2024-01-05T10:00:00Z"))
        );

        DataRetriever dr = new DataRetriever();
        dr.saveIngredient(rice);

        StockService service = new StockService();
        Stock stock = service.getStockValueAt(
                rice,
                Instant.parse("2024-01-06T12:00:00Z")
        );

        System.out.println(stock.getQuantity()); // 7.0
    }
}
