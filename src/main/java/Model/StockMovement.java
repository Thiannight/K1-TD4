package Model;

import java.time.Instant;

public class StockMovement {
    private final int id;
    private final double quantity;
    private final Unit unit;
    private final Instant movementDatetime;
    private final MovementTypeEnum type;

    public StockMovement(int id, double quantity, Unit unit, Instant movementDatetime, MovementTypeEnum type) {
        this.id = id;
        this.quantity = quantity;
        this.unit = unit;
        this.movementDatetime = movementDatetime;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public double getQuantity() {
        return quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public Instant getMovementDatetime() {
        return movementDatetime;
    }
}
