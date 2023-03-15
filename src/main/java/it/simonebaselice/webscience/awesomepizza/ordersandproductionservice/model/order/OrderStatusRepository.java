package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.Set;

public interface OrderStatusRepository extends CrudRepository<OrderStatus, Long>, Repository<OrderStatus, Long> {

    Set<OrderStatus> findAllByStatus(OrderStatusType status);

}
