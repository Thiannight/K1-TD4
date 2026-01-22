package service;

import Model.Ingredient;
import Model.Stock;
import Model.StockMovement;
import Model.Unit;

import java.time.Instant;

public class StockService {

    public Stock getStockValueAt(Ingredient ingredient, Instant t) {
        double total = 0;

        for (StockMovement sm : ingredient.getStockMovementList()) {
            if (!sm.getMovementDatetime().isAfter(t)) {
                total += sm.getQuantity();
            }
        }

        return new Stock(total, Unit.KG);
    }
}
