package Model;

import java.time.Instant;
import java.util.UUID;

public class StockMovement {
    private final UUID id;
    private final double quantity;
    private final Unit unit;
    private final Instant movementDatetime;

    public StockMovement(UUID id, double quantity, Unit unit, Instant movementDatetime) {
        this.id = id;
        this.quantity = quantity;
        this.unit = unit;
        this.movementDatetime = movementDatetime;
    }

    public UUID getId() {
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
