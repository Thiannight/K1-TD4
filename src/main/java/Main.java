import Model.*;
import repository.DataRetriever;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        Dish pizza = new Dish();
        pizza.setName("Margherita");
        pizza.setPrice(12.0);
        pizza.setDishType(DishTypeEnum.MAIN);
        pizza = dr.saveDish(pizza);

        RestaurantTable table = dr.findTableByNumber(1);
        if (table == null) {
            System.out.println("Table non trouvée");
            return;
        }

        Order newOrder = new Order();
        newOrder.setReference("ORD00001");
        newOrder.setCreationDatetime(Instant.now());
        newOrder.setTable(table);
        newOrder.setSeatingDatetime(Instant.now());
        newOrder.setLeavingDatetime(Instant.now().plus(2, ChronoUnit.HOURS));

        DishOrder item = new DishOrder();
        item.setDish(pizza);
        item.setQuantity(2);
        newOrder.getDishOrders().add(item);

        try {
            dr.saveOrder(newOrder);
            System.out.println("Commande sauvegardée ! Total TTC: " + newOrder.getTotalAmountWithVAT());
            System.out.println("Table: " + table.getTableNumber());
        } catch (RuntimeException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        Order anotherOrder = new Order();
        anotherOrder.setReference("ORD00002");
        anotherOrder.setCreationDatetime(Instant.now());
        anotherOrder.setTable(table);
        anotherOrder.setSeatingDatetime(Instant.now());
        anotherOrder.setLeavingDatetime(Instant.now().plus(2, ChronoUnit.HOURS));

        DishOrder item2 = new DishOrder();
        item2.setDish(pizza);
        item2.setQuantity(1);
        anotherOrder.getDishOrders().add(item2);

        try {
            dr.saveOrder(anotherOrder);
        } catch (RuntimeException e) {
            System.out.println("Test de non-disponibilité: " + e.getMessage());
        }
    }
}