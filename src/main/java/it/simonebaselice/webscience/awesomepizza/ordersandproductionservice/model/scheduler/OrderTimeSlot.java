package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.scheduler;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderStatusInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
public class OrderTimeSlot implements Comparable<OrderTimeSlot> {

    private final OffsetDateTime intervalStart;
    private final OffsetDateTime intervalEnd;
    private final OrderStatusInfoDto orderInfo;


    @Override
    public int compareTo(OrderTimeSlot orderTimeSlot) {
        if(this.getIntervalStart().isBefore(orderTimeSlot.getIntervalStart()))
            return -1;
        else if (this.getIntervalStart().isEqual(orderTimeSlot.getIntervalStart()))
            return 0;
        else
            return 1;
    }

}
