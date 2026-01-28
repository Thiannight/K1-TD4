import Model.Dish;
import Model.DishOrder;
import Model.Order;
import repository.DataRetriever;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        Dish pizza = new Dish();
        pizza.setName("Margherita");
        pizza.setPrice(12.0);

        Order newOrder = new Order();
        newOrder.setReference("ORD00001");
        newOrder.setCreationDatetime(Instant.now());

        DishOrder item = new DishOrder();
        item.setDish(pizza);
        item.setQuantity(2);
        newOrder.getDishOrders().add(item);

        dr.saveOrder(newOrder);
        System.out.println("Order saved! Total TTC: " + newOrder.getTotalAmountWithVAT());
    }
}
