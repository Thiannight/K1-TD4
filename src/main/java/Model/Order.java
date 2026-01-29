package Model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders = new ArrayList<>();
    private RestaurantTable table;
    private Instant seatingDatetime;
    private Instant leavingDatetime;

    // Getters et setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }
    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }
    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public RestaurantTable getTable() {
        return table;
    }
    public void setTable(RestaurantTable table) {
        this.table = table;
    }

    public Instant getSeatingDatetime() {
        return seatingDatetime;
    }
    public void setSeatingDatetime(Instant seatingDatetime) {
        this.seatingDatetime = seatingDatetime;
    }

    public Instant getLeavingDatetime() {
        return leavingDatetime;
    }
    public void setLeavingDatetime(Instant leavingDatetime) { this.leavingDatetime = leavingDatetime; }

    public Double getTotalAmountWithoutVat() {
        return dishOrders.stream()
                .mapToDouble(item -> {
                    Dish dish = item.getDish();
                    return (dish != null && dish.getPrice() != null)
                            ? dish.getPrice() * item.getQuantity()
                            : 0.0;
                })
                .sum();
    }

    public Double getTotalAmountWithVat() {
        Double total = getTotalAmountWithoutVat();
        return total != null ? total * 1.20 : null;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", table=" + (table != null ? table.getTableNumber() : "null") +
                ", seating=" + seatingDatetime +
                ", leaving=" + leavingDatetime +
                ", dishCount=" + dishOrders.size() +
                '}';
    }
}