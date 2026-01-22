package Model;

import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private final Integer id;
    private final String name;
    private final List<StockMovement> stockMovementList = new ArrayList<>();

    public Ingredient(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addStockMovement(StockMovement sm) {
        stockMovementList.add(sm);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }
}

