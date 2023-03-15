package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.ReceivedOrderStatusDto;
import it.simonebaselice.webscience.awesomepizza.utils.IdGeneratedManager;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class OrderManager extends IdGeneratedManager<Order, Long, OrderDto> {

    private final OrderRepository orderRepository;
    private final OrderStatusManager orderStatusManager;


    protected OrderManager(
        OrderRepository orderRepository,
        OrderStatusManager orderStatusManager
    ) {
        super(orderRepository);
        this.orderRepository = orderRepository;
        this.orderStatusManager = orderStatusManager;
    }

    @Override
    public Order createNew() {
        OffsetDateTime timeNow = OffsetDateTime.now();
        Order entity = super.createNew();
        entity.setOrderCreationDateTime(timeNow);
        OrderStatus orderStatus = orderStatusManager.createNewAndUpdate(
            entity.getId(),
            new ReceivedOrderStatusDto(entity.getId())
        );
        orderStatus.setOrder(entity);
        entity.setOrderStatus(orderStatus);
        return entity;
    }

    @Override
    protected Order updateEntity(
        Order currentState, OrderDto desiredState
    ) {
        currentState.updateOrder(
            desiredState.getUserId(),
            desiredState.getPizzaTypeId(),
            desiredState.getNotes(),
            desiredState.getSchedulingMode()
        );
        return currentState;
    }

    @Override
    protected Order createDefault() {
        return new Order();
    }

}
