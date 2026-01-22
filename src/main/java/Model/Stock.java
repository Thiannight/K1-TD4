package Model;

public class Stock {
    private final double quantity;
    private final Unit unit;

    public Stock(double quantity, Unit unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public Unit getUnit() {
        return unit;
    }
}
