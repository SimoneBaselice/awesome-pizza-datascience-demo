package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import com.fasterxml.jackson.databind.util.BeanUtil;
import it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype.PizzaType;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderStatusDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderStatusInfoDto;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

public class OrderDtoUtils {

    public static OrderDto buildOrderDto(Order entity) {
        return new OrderDto(
            entity.getUserId(),
            entity.getPizzaTypeId(),
            entity.getSchedulingMode()
        ).orderId(entity.getId())
        .notes(entity.getNotes())
        .orderCreationDateTime(entity.getOrderCreationDateTime());
    }

    public static OrderStatusDto buildOrderStatusDto(OrderStatus entity) {
        return entity.getStatusInfo();
    }

    public static OrderStatusInfoDto buildOrderStatusInfoDto(OrderStatus entity) {
        return new OrderStatusInfoDto(buildOrderDto(entity.getOrder()), buildOrderStatusDto(entity));
    }

    public static void setActivationDateTimeForOrderStatusDto(OrderStatusDto dto, OffsetDateTime dateTime) {
        try {
            Field dateTimeField = dto.getClass().getDeclaredField("statusEnteredDateTime");
            dateTimeField.setAccessible(true);
            dateTimeField.set(dto, dateTime);
            dateTimeField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException("All classes implementing OrderStatusDto should have the 'statusEnteredDateTime' field and it should be of type OffsetDateTime");
        }
    }
}
