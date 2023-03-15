package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.scheduler;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.*;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.cachinglayer.PizzaCatalogCachingLayer;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.cachinglayer.PizzaioliCachingLayer;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order.OrderStatusManager;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order.OrderStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PizzaSchedulingService {

    private final PizzaCatalogCachingLayer pizzaCatalogCachingLayer;
    private final PizzaioliCachingLayer pizzaioliCachingLayer;
    private final OrderStatusManager orderStatusManager;
    private final Clock clock;

    public PizzaSchedulingService(
        PizzaCatalogCachingLayer pizzaCatalogCachingLayer,
        PizzaioliCachingLayer pizzaioliCachingLayer,
        OrderStatusManager orderStatusManager,
        Clock clock
    ) {
        this.pizzaCatalogCachingLayer = pizzaCatalogCachingLayer;
        this.pizzaioliCachingLayer = pizzaioliCachingLayer;
        this.orderStatusManager = orderStatusManager;
        this.clock = clock;
    }

    @Autowired
    public PizzaSchedulingService(
        PizzaCatalogCachingLayer pizzaCatalogCachingLayer,
        PizzaioliCachingLayer pizzaioliCachingLayer,
        OrderStatusManager orderStatusManager
    ) {
        this.pizzaCatalogCachingLayer = pizzaCatalogCachingLayer;
        this.pizzaioliCachingLayer = pizzaioliCachingLayer;
        this.orderStatusManager = orderStatusManager;
        this.clock = Clock.systemDefaultZone();
    }

    @Scheduled(fixedDelay = 1000)
    public void runScheduler() {
        Set<ScheduledOrderStatusDto> updatedOrders = computeNewSchedule(
            orderStatusManager.getAllOrderStatusInfoByStatus(OrderStatusType.ScheduledOrderStatus),
            orderStatusManager.getAllOrderStatusInfoByStatus(OrderStatusType.ReadyOrderStatus),
            orderStatusManager.getAllOrderStatusInfoByStatus(OrderStatusType.CookingOrderStatus),
            pizzaioliCachingLayer.getAllPizzaioliIds()
        );
        updatedOrders.forEach(s -> orderStatusManager.update(s.getOrderId(), s));
    }

    /**
     * Scheduling rules
     *
     * Order priority rules:
     *  - Orders with 'PlannedSchedulingMode' always take precedence over orders where the scheduling mode is
     *    'AsSoonAspossibleSchedulingMode'
     *  - Among orders with the same scheduling mode, the ones that have the earliest OrderCreationDateTime take the
     *    precedence
     *
     * Orders with a higher priority will be scheduled before orders with a lower one.
     * Orders already scheduled with a certain pizzaiolo will continue to be scheduled with the same pizziolo if doings
     * so would not cause a slower delivery time
     *
     * @param ordersAlreadyScheduled
     * @param newOrdersToSchedule
     * @param ordersCurrentlyInPreparation
     * @param availablePizzaioliIds
     * @return the returned set contains both the updated orders that had already been scheduled and the one that were
     * scheduled for the first time
     */
    public Set<ScheduledOrderStatusDto> computeNewSchedule(
        Set<OrderStatusInfoDto> ordersAlreadyScheduled,
        Set<OrderStatusInfoDto> newOrdersToSchedule,
        Set<OrderStatusInfoDto> ordersCurrentlyInPreparation,
        Set<Long> availablePizzaioliIds
    ) {

        Set<ScheduledOrderStatusDto> newScheduledOrder = new HashSet<>();

        Map<Long, SortedSet<OrderTimeSlot>> busyTimeSlotsByPizzaioloId = availablePizzaioliIds.stream()
            .collect(Collectors.toMap(i -> i, i -> new TreeSet<>()));

        // We need to create the busy slots for the orders currently in preparation
        ordersCurrentlyInPreparation.forEach(orderInfo -> {
            CookingOrderStatusDto cookingOrder = ((CookingOrderStatusDto) orderInfo.getStatusInfo());
            PizzaTypeDto pizzaType = pizzaCatalogCachingLayer.getPizzaTypeFromId(orderInfo.getOrder().getPizzaTypeId());
            busyTimeSlotsByPizzaioloId.get(cookingOrder.getAssignedPizzaioloId()).add(new OrderTimeSlot(
                cookingOrder.getStatusEnteredDateTime(),
                getNewEstimationForCookingEndTime(cookingOrder, Duration.ofMinutes(pizzaType.getPreparationTimeMinutes())),
                orderInfo
            ));
        });

        // We can then start scheduling the slots for the other orders based on priority
        OffsetDateTime now = OffsetDateTime.now(clock);
        List<OrderStatusInfoDto> ordersSortedByPriority = getOrdersSortedByPriority(ordersAlreadyScheduled, newOrdersToSchedule);
        ordersSortedByPriority.forEach(orderInfo -> {

            OffsetDateTime earliestStart;
            if (orderInfo.getOrder().getSchedulingMode() instanceof AsSoonAspossibleSchedulingModeDto) {
                earliestStart = now;
            } else {
                OffsetDateTime plannedDateTime =
                    ((PlannedSchedulingModeDto) orderInfo.getOrder().getSchedulingMode()).getPlannedDateTime();
                if(plannedDateTime.isBefore(now))
                    earliestStart = now;
                else
                    earliestStart = plannedDateTime;
            }

            PizzaTypeDto pizzaType = pizzaCatalogCachingLayer.getPizzaTypeFromId(orderInfo.getOrder().getPizzaTypeId());

            Map<Long, OrderTimeSlot> earliestTimeSlotByPizzaiolo = availablePizzaioliIds.stream()
                .collect(Collectors.toMap(
                    pizzaioloId -> pizzaioloId,
                    pizzaioloId -> getEarliestFreeSlot(
                        earliestStart,
                        Duration.ofMinutes(pizzaType.getPreparationTimeMinutes()),
                        orderInfo,
                        busyTimeSlotsByPizzaioloId.get(pizzaioloId)
                    )
                ));

            Long newAssignedPizzaioloId;
            //If the order was already scheduled check if it is possible to keep the same pizzaiolo that was assigned
            // before without losing more than 2 minutes from the optimal solution
            if(orderInfo.getStatusInfo() instanceof ScheduledOrderStatusDto) {
                Long lastAssignedPizzaioloId =
                    ((ScheduledOrderStatusDto) orderInfo.getStatusInfo()).getAssignedPizzaioloId();
                OffsetDateTime startDateTimeWithOldPizzaiolo =
                    earliestTimeSlotByPizzaiolo.get(lastAssignedPizzaioloId).getIntervalStart();
                if(!earliestTimeSlotByPizzaiolo.values().stream().anyMatch(slot ->
                        slot.getIntervalStart().plus(Duration.ofMinutes(2)).isBefore(startDateTimeWithOldPizzaiolo)
                ))
                    newAssignedPizzaioloId = lastAssignedPizzaioloId;
                else
                    newAssignedPizzaioloId =  earliestTimeSlotByPizzaiolo.entrySet().stream()
                        .min((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                        .orElseThrow().getKey();
            } else {
                newAssignedPizzaioloId =  earliestTimeSlotByPizzaiolo.entrySet().stream()
                    .min((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                    .orElseThrow().getKey();
            }


            OrderTimeSlot orderScheduledTimeSlot = earliestTimeSlotByPizzaiolo.get(newAssignedPizzaioloId);

            // Book the busy slot for the newly assigned pizzaiolo and create the new ScheduledOrderStatusDto
            busyTimeSlotsByPizzaioloId.get(newAssignedPizzaioloId).add(orderScheduledTimeSlot);
            newScheduledOrder.add(
                new ScheduledOrderStatusDto(orderInfo.getOrder().getOrderId())
                    .scheduledCookingStartDateTime(orderScheduledTimeSlot.getIntervalStart())
                    .assignedPizzaioloId(newAssignedPizzaioloId)
            );

        });

        return newScheduledOrder;

    }

    protected List<OrderStatusInfoDto> getOrdersSortedByPriority(
        Set<OrderStatusInfoDto> ordersAlreadyScheduled,
        Set<OrderStatusInfoDto> newOrdersToSchedule
    ) {
        return Stream.concat(ordersAlreadyScheduled.stream(), newOrdersToSchedule.stream())
            .sorted((o1, o2) -> {
                if(
                    o1.getOrder().getSchedulingMode() instanceof PlannedSchedulingModeDto &&
                    o2.getOrder().getSchedulingMode() instanceof AsSoonAspossibleSchedulingModeDto
                ) {
                    return -1;
                } else if (
                    o1.getOrder().getSchedulingMode() instanceof AsSoonAspossibleSchedulingModeDto &&
                    o2.getOrder().getSchedulingMode() instanceof PlannedSchedulingModeDto
                ) {
                    return 1;
                } else {
                    if(o1.getOrder().getOrderCreationDateTime().isBefore(o2.getOrder().getOrderCreationDateTime()))
                        return -1;
                    else
                        return 1;
                }
            }).collect(Collectors.toList());
    }

    protected OrderTimeSlot getEarliestFreeSlot(
        OffsetDateTime earliestPossibleSlotStart,
        Duration slotSize,
        OrderStatusInfoDto orderInfo,
        SortedSet<OrderTimeSlot> busySlots
    ) {

        OffsetDateTime now = OffsetDateTime.now(clock);

        //If earliestPossibleSlotStart is a time in the past we should try to schedule the order for right now
        if(earliestPossibleSlotStart.isBefore(now))
            earliestPossibleSlotStart = now;

        //If there are no busy slots we can choose the optimal slot freely
        if(busySlots.isEmpty()) {
            return new OrderTimeSlot(
                earliestPossibleSlotStart,
                earliestPossibleSlotStart.plus(slotSize),
                orderInfo
            );
        }

        //Check if there is an optimal slot before any busy slot
        OrderTimeSlot fistSlot = busySlots.first();
        if(
            earliestPossibleSlotStart.isBefore(fistSlot.getIntervalStart()) &&
            Duration.between(earliestPossibleSlotStart, fistSlot.getIntervalStart()).compareTo(slotSize) > 0
        ) {
            return new OrderTimeSlot(
                earliestPossibleSlotStart,
                earliestPossibleSlotStart.plus(slotSize),
                orderInfo
            );
        }

        //Check if earliestPossibleSlotStart is after the end of all the busy slots
        OrderTimeSlot lastSlot = busySlots.last();
        if(earliestPossibleSlotStart.isAfter(lastSlot.getIntervalEnd())) {
            return new OrderTimeSlot(
                earliestPossibleSlotStart,
                earliestPossibleSlotStart.plus(slotSize),
                orderInfo
            );
        }

        //Check if there is a free slot between two busy slots
        Iterator<OrderTimeSlot> busySlotIterator = busySlots.iterator();
        OrderTimeSlot slotBefore = busySlotIterator.next();
        OrderTimeSlot slotAfter;
        while (busySlotIterator.hasNext()) {
            slotAfter = busySlotIterator.next();

            OffsetDateTime earliestPossibleDateTimeAfterFirstSlot;
            if(earliestPossibleSlotStart.isBefore(slotBefore.getIntervalEnd()))
                earliestPossibleDateTimeAfterFirstSlot = slotBefore.getIntervalEnd();
            else
                earliestPossibleDateTimeAfterFirstSlot = earliestPossibleSlotStart;

            if(
                earliestPossibleDateTimeAfterFirstSlot.isBefore(slotAfter.getIntervalStart()) &&
                Duration.between(earliestPossibleDateTimeAfterFirstSlot, slotAfter.getIntervalStart()).compareTo(slotSize) > 0
            ) {
                return new OrderTimeSlot(
                    earliestPossibleDateTimeAfterFirstSlot,
                    earliestPossibleDateTimeAfterFirstSlot.plus(slotSize),
                    orderInfo
                );
            }

            slotBefore = slotAfter;
        }


        //At this point, we can always put a slot after the last busy one
        return new OrderTimeSlot(
            lastSlot.getIntervalEnd(),
            lastSlot.getIntervalEnd().plus(slotSize),
            orderInfo
        );

    }

    protected OffsetDateTime getNewEstimationForCookingEndTime(CookingOrderStatusDto cookingOrder, Duration initialDurationEstimation) {
        OffsetDateTime now = OffsetDateTime.now(clock);

        //If preparations has not lasted longer than our initial estimation than we consider that estimation still valid
        if(cookingOrder.getStatusEnteredDateTime().plus(initialDurationEstimation).isAfter(now)) {
            return cookingOrder.getStatusEnteredDateTime().plus(initialDurationEstimation);
        } else {

            //Otherwise we make iterative estimation adding 1/2 of the original estimation every time the cooking time
            // exceeds the current estimation
            Duration durationOverEstimation =
                Duration.between(cookingOrder.getStatusEnteredDateTime().plus(initialDurationEstimation), now);
            long iteration = (durationOverEstimation.toSeconds() / (initialDurationEstimation.toSeconds()/2)) + 1;
            return cookingOrder.getStatusEnteredDateTime().plus(initialDurationEstimation).plus(initialDurationEstimation.dividedBy(2).multipliedBy(iteration));
        }
    }
}
