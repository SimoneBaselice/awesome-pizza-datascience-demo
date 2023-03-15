package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.controller;

import it.simonebaselice.webscience.awesomepizza.generated.http.controller.OrdersApi;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.*;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order.*;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Transactional
public class OrdersAndProdController implements OrdersApi {

    private final OrderManager orderManager;
    private final OrderStatusManager orderStatusManager;

    public OrdersAndProdController(
        OrderManager orderManager,
        OrderStatusManager orderStatusManager
    ) {
        this.orderManager = orderManager;
        this.orderStatusManager = orderStatusManager;
    }

    /**
     * DELETE /orders/{id} :
     *
     * @param id (required)
     * @return Accepted (status code 202)
     * or  (status code 403)
     * or  (status code 404)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<Void> cancelOrder(Long id) {
        OrderStatus status = orderStatusManager.getById(id);
        if(status.getStatus().equals(OrderStatusType.DeliveredOrderStatus))
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        orderStatusManager.update(id, new CancelledOrderStatusDto(id).reason("The order has been cancelled by the user"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * PUT /orders/{id}/statusInfo :
     *
     * @param id                             (required)
     * @param changeStatusForOrderRequestDto (optional)
     * @return Accepted (status code 202)
     * or  (status code 400)
     * or  (status code 403)
     * or  (status code 404)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<OrderStatusInfoDto> changeStatusForOrder(
        Long id, @Valid ChangeStatusForOrderRequestDto changeStatusForOrderRequestDto
    ) {
        OrderStatus status = orderStatusManager.getById(id);

        Set<Long> pizzaioliBusy = orderStatusManager.getAllOrderStatusInfoByStatus(OrderStatusType.CookingOrderStatus)
            .stream().map(s -> ((CookingOrderStatusDto)s.getStatusInfo()).getAssignedPizzaioloId())
            .collect(Collectors.toSet());

        OrderStatus newEntity;

        if(
            status.getStatus().equals(OrderStatusType.ScheduledOrderStatus) &&
            changeStatusForOrderRequestDto.getNewStatus()
                .equals(ChangeStatusForOrderRequestDto.NewStatusEnum.COOKINGORDERSTATUS)
        ) {
            Long pizzaioloId = ((ScheduledOrderStatusDto)status.getStatusInfo()).getAssignedPizzaioloId();
            if(pizzaioliBusy.contains(pizzaioloId))
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            else
                newEntity = orderStatusManager.update(id, new CookingOrderStatusDto(id).assignedPizzaioloId(pizzaioloId));
        } else if (
            status.getStatus().equals(OrderStatusType.CookingOrderStatus) && changeStatusForOrderRequestDto.getNewStatus()
                .equals(ChangeStatusForOrderRequestDto.NewStatusEnum.READYORDERSTATUS)
        ) {
            newEntity = orderStatusManager.update(id, new ReadyOrderStatusDto(id));
        } else if (
            status.getStatus().equals(OrderStatusType.ReadyOrderStatus) && changeStatusForOrderRequestDto.getNewStatus()
                .equals(ChangeStatusForOrderRequestDto.NewStatusEnum.DELIVEREDORDERSTATUS)
        ) {
            newEntity = orderStatusManager.update(id, new DeliveredOrderStatusDto(id));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(OrderDtoUtils.buildOrderStatusInfoDto(newEntity), HttpStatus.ACCEPTED);
    }

    /**
     * POST /orders :
     *
     * @param orderDto (optional)
     * @return Created (status code 201)
     * or  (status code 400)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<OrderDto> createOrder(
        @Valid OrderDto orderDto
    ) {
        Order order = orderManager.createNewAndUpdate(orderDto);
        return new ResponseEntity<>(OrderDtoUtils.buildOrderDto(order), HttpStatus.CREATED);
    }

    /**
     * GET /orders : Orders collection
     *
     * @return OK (status code 200)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return new ResponseEntity<>(
            orderManager.getAll().stream().map(OrderDtoUtils::buildOrderDto).collect(Collectors.toList()),
            HttpStatus.OK
        );
    }

    /**
     * GET /orders/{id} : Order
     *
     * @param id (required)
     * @return OK (status code 200)
     * or  (status code 403)
     * or  (status code 404)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<OrderDto> getOrder(
        Long id
    ) {
        return new ResponseEntity<>(OrderDtoUtils.buildOrderDto(orderManager.getById(id)), HttpStatus.OK);
    }

    /**
     * GET /orders/statusInfo : Informations on all order status
     *
     * @return OK (status code 200)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<List<OrderStatusInfoDto>> getStatusInfoForAllOrders() {
        return new ResponseEntity<>(
            orderStatusManager.getAll().stream().map(OrderDtoUtils::buildOrderStatusInfoDto).collect(Collectors.toList()),
            HttpStatus.OK
        );
    }

    /**
     * GET /orders/{id}/statusInfo : Informations on order status
     *
     * @param id (required)
     * @return OK (status code 200)
     * or  (status code 403)
     * or  (status code 404)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<OrderStatusInfoDto> getStatusInfoForOrder(
        Long id
    ) {
        return new ResponseEntity<>(OrderDtoUtils.buildOrderStatusInfoDto(orderStatusManager.getById(id)), HttpStatus.OK);
    }

}
