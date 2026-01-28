import Model.Dish;
import Model.DishOrder;
import Model.Order;
import repository.DataRetriever;

void main() {
    DataRetriever dr = new DataRetriever();

    // 1. Setup a Dish
    Dish pizza = new Dish();
    pizza.setName("Margherita");
    pizza.setPrice(12.0);

    // 2. Prepare an Order
    Order newOrder = new Order();
    newOrder.setReference("ORD00001");
    newOrder.setCreationDatetime(Instant.now());

    DishOrder item = new DishOrder();
    item.setDish(pizza);
    item.setQuantity(2);
    newOrder.getDishOrders().add(item);

    // 3. Save with validation
    try {
        dr.saveOrder(newOrder);
        IO.println("Order saved! Total TTC: " + newOrder.getTotalAmountWithVAT());
    } catch (RuntimeException e) {
        System.err.println("Error: " + e.getMessage());
    }
}