package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.*;

import java.util.Arrays;

public enum OrderStatusType {
    ReceivedOrderStatus(ReceivedOrderStatusDto.class),
    CancelledOrderStatus(CancelledOrderStatusDto.class),
    ScheduledOrderStatus(ScheduledOrderStatusDto.class),
    CookingOrderStatus(CookingOrderStatusDto.class),
    ReadyOrderStatus(ReadyOrderStatusDto.class),
    DeliveredOrderStatus(DeliveredOrderStatusDto.class);

    Class<? extends OrderStatusDto> dtoInstanceClass;

    OrderStatusType(Class<? extends OrderStatusDto> dtoInstanceClass) {
        this.dtoInstanceClass = dtoInstanceClass;
    }

    public Class<? extends OrderStatusDto> getDtoInstanceClass() {
        return dtoInstanceClass;
    }

    static OrderStatusType getOrderStatusTypeForDtoClass(Class<? extends OrderStatusDto> dtoClass) {
        return Arrays.stream(OrderStatusType.values())
            .filter(e -> e.getDtoInstanceClass().equals(dtoClass))
            .findFirst().orElseThrow();
    }

}
