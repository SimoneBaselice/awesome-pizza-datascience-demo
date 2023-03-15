package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderStatusDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderStatusInfoDto;
import it.simonebaselice.webscience.awesomepizza.utils.IdAssignedManager;
import it.simonebaselice.webscience.awesomepizza.utils.IdGeneratedManager;
import org.cef.OS;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderStatusManager extends IdAssignedManager<OrderStatus, Long, OrderStatusDto> {

    private final OrderStatusRepository orderStatusRepository;

    protected OrderStatusManager(
        OrderStatusRepository orderStatusRepository
    ) {
        super(orderStatusRepository);
        this.orderStatusRepository = orderStatusRepository;
    }

    public Set<OrderStatusInfoDto> getAllOrderStatusInfoByStatus(OrderStatusType statusType) {
        return orderStatusRepository.findAllByStatus(statusType).stream()
            .map(OrderDtoUtils::buildOrderStatusInfoDto)
            .collect(Collectors.toSet());
    }

    @Override
    protected OrderStatus updateEntity(
        OrderStatus currentState, OrderStatusDto desiredState
    ) {
        if(
            currentState.getStatus() == null ||
                !currentState.getStatus().equals(OrderStatusType.getOrderStatusTypeForDtoClass(desiredState.getClass()))
        ) {
            OffsetDateTime now = OffsetDateTime.now();
            currentState.setStatusEnteredDateTime(now);
            OrderDtoUtils.setActivationDateTimeForOrderStatusDto(desiredState, now);
        }
        currentState.updateOrderStatus(
            OrderStatusType.getOrderStatusTypeForDtoClass(desiredState.getClass()),
            desiredState
        );
        return currentState;
    }

    @Override
    protected OrderStatus createDefault(Long key) {
        return new OrderStatus(key);
    }

}
