package service;

import Model.Ingredient;
import Model.Stock;
import Model.StockMovement;
import Model.Unit;

import java.time.Instant;
import java.util.List;

public class StockService {

    public Stock getStockValueAt(Ingredient ingredient, Instant t) {
        double initialStock = getInitialStock(ingredient.getName());
        double total = initialStock;

        List<StockMovement> movements = ingredient.getStockMovementList();
        if (movements == null || movements.isEmpty()) {
            return new Stock(initialStock, Unit.KG);
        }

        for (StockMovement sm : movements) {
            if (!sm.getMovementDatetime().isAfter(t)) {
                total += sm.getQuantity();
            }
        }

        return new Stock(total, Unit.KG);
    }

    private double getInitialStock(String ingredientName) {
        return switch (ingredientName.toLowerCase()) {
            case "laitue" -> 5.0;
            case "tomate" -> 4.0;
            case "poulet" -> 10.0;
            case "chocolat" -> 3.0;
            case "beurre" -> 2.5;
            default -> 0.0;
        };
    }
}