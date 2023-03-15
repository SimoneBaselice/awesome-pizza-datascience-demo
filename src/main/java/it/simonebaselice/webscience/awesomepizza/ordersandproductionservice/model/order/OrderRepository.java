package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype.PizzaType;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {

}
