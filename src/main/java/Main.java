import Model.Order;
import Model.RestaurantTable;
import repository.DataRetriever;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        try {
            RestaurantTable table1 = dr.findTableByNumber(1);
            Order order1 = new Order();
            order1.setReference("CMD-001");
            order1.setCreationDatetime(Instant.now());
            order1.setTable(table1);
            order1.setSeatingDatetime(Instant.now());
            order1.setLeavingDatetime(Instant.now().plus(2, ChronoUnit.HOURS));

            Order saved = dr.saveOrder(order1);
            System.out.println("Commande créée: " + saved);
        } catch (Exception e) {
            System.out.println("Test 1 échoué: " + e.getMessage());
        }

        try {
            RestaurantTable table1 = dr.findTableByNumber(1);
            Order order2 = new Order();
            order2.setReference("CMD-002");
            order2.setCreationDatetime(Instant.now());
            order2.setTable(table1);
            order2.setSeatingDatetime(Instant.now());
            order2.setLeavingDatetime(Instant.now().plus(1, ChronoUnit.HOURS));

            dr.saveOrder(order2);
            System.out.println("Test 2 devrait échouer mais n'a pas échoué");
        } catch (RuntimeException e) {
            System.out.println("Test 2 réussi (échec attendu): " + e.getMessage());
        }
    }
}