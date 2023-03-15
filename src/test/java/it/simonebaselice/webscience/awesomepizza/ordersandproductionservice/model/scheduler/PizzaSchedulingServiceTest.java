package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.scheduler;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.*;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.cachinglayer.PizzaCatalogCachingLayer;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.cachinglayer.PizzaioliCachingLayer;
import it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order.OrderStatusManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


class PizzaSchedulingServiceTest {

    OffsetDateTime baseTime0 = OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
    OffsetDateTime baseTime1 = baseTime0.plusMinutes(5);
    OffsetDateTime baseTime2 = baseTime1.plusMinutes(5);
    OffsetDateTime baseTime3Current = baseTime2.plusMinutes(5);

    PizzaSchedulingService pizzaSchedulingService;

    Clock clock;
    PizzaCatalogCachingLayer pizzaCatalogCachingLayer;
    PizzaioliCachingLayer pizzaioliCachingLayer;

    @Mock
    OrderStatusManager orderStatusManager;

    @BeforeEach
    void setUp() {
        pizzaCatalogCachingLayer = mock(PizzaCatalogCachingLayer.class);
        pizzaioliCachingLayer = mock(PizzaioliCachingLayer.class);

        clock = mock(Clock.class);
        Clock fixedClock = Clock.fixed(baseTime3Current.toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        pizzaSchedulingService = new PizzaSchedulingService(pizzaCatalogCachingLayer,
            pizzaioliCachingLayer,
            orderStatusManager, clock);
    }

    @Test
    void updateSchedule() {
    }

    @Test
    void order_with_PlannedSchedulingMode_should_have_higher_priority_than_AsSoonAsPossibleSchedulingMode() {

        OrderStatusInfoDto orderLowPrio1 = new OrderStatusInfoDto(
            new OrderDto(1L, "P1", new AsSoonAspossibleSchedulingModeDto())
                .orderCreationDateTime(baseTime1),
            new ReadyOrderStatusDto(1L).statusEnteredDateTime(baseTime1)
        );
        OrderStatusInfoDto orderLowPrio2 = new OrderStatusInfoDto(
            new OrderDto(2L, "P1", new AsSoonAspossibleSchedulingModeDto())
                .orderCreationDateTime(baseTime0),
            new ReadyOrderStatusDto(2L).statusEnteredDateTime(baseTime0)
        );
        OrderStatusInfoDto orderLowPrio3 = new OrderStatusInfoDto(
            new OrderDto(4L, "P2", new AsSoonAspossibleSchedulingModeDto())
                .orderCreationDateTime(baseTime1),
            new ScheduledOrderStatusDto(4L)
                .scheduledCookingStartDateTime(baseTime2)
                .assignedPizzaioloId(2L)
                .statusEnteredDateTime(baseTime1)
        );
        OrderStatusInfoDto orderHighPrio1 = new OrderStatusInfoDto(
            new OrderDto(3L, "P1", new PlannedSchedulingModeDto(OffsetDateTime.now()))
                .orderCreationDateTime(baseTime1),
            new ReadyOrderStatusDto(3L).statusEnteredDateTime(baseTime1)
        );

        List<OrderStatusInfoDto> ordersByPriority = pizzaSchedulingService.getOrdersSortedByPriority(
            Set.of(orderLowPrio3),
            Set.of(orderLowPrio1, orderHighPrio1, orderLowPrio2)
        );

        assertEquals(4, ordersByPriority.size());
        assertEquals(orderHighPrio1, ordersByPriority.get(0));
    }

    void priority_for_order_with_the_same_scheduling_mode_should_be_decided_based_on_order_creation() {

        OrderStatusInfoDto orderPrioLow = new OrderStatusInfoDto(
            new OrderDto(1L, "P1", new AsSoonAspossibleSchedulingModeDto())
                .orderCreationDateTime(baseTime2),
            new ReadyOrderStatusDto(1L).statusEnteredDateTime(baseTime2)
        );
        OrderStatusInfoDto orderPrioMedium = new OrderStatusInfoDto(
            new OrderDto(2L, "P1", new AsSoonAspossibleSchedulingModeDto())
                .orderCreationDateTime(baseTime1),
            new ReadyOrderStatusDto(2L).statusEnteredDateTime(baseTime1)
        );
        OrderStatusInfoDto orderPrioHigh = new OrderStatusInfoDto(
            new OrderDto(2L, "P1", new AsSoonAspossibleSchedulingModeDto())
                .orderCreationDateTime(baseTime0),
            new ReadyOrderStatusDto(2L).statusEnteredDateTime(baseTime0)
        );

        List<OrderStatusInfoDto> ordersByPriority = pizzaSchedulingService.getOrdersSortedByPriority(
            Set.of(),
            Set.of(orderPrioMedium, orderPrioHigh, orderPrioLow)
        );

        assertEquals(3, ordersByPriority.size());
        assertEquals(orderPrioHigh, ordersByPriority.get(0));
        assertEquals(orderPrioMedium, ordersByPriority.get(1));
        assertEquals(orderPrioLow, ordersByPriority.get(2));

    }

    @Test
    void orders_planned_for_the_past_should_be_scheduled_as_soon_as_possible() {

        OrderStatusInfoDto orderInfo = mock(OrderStatusInfoDto.class);
        SortedSet<OrderTimeSlot> busySlots = new TreeSet<>();

        OrderTimeSlot orderSlotFound = pizzaSchedulingService.getEarliestFreeSlot(
            baseTime0,
            Duration.ofMinutes(10),
            orderInfo,
            busySlots
        );

        assertEquals(orderInfo, orderSlotFound.getOrderInfo());
        assertTrue(baseTime3Current.isEqual(orderSlotFound.getIntervalStart()));
        assertTrue(baseTime3Current.plusMinutes(10).isEqual(orderSlotFound.getIntervalEnd()));
    }

    @Test
    void orders_planned_for_the_future_should_be_scheduled_at_that_time_when_possible() {

        OrderStatusInfoDto orderInfo = mock(OrderStatusInfoDto.class);
        SortedSet<OrderTimeSlot> busySlots = new TreeSet<>();

        Clock fixedClock = Clock.fixed(baseTime0.toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();

        OrderTimeSlot orderSlotFound = pizzaSchedulingService.getEarliestFreeSlot(
            baseTime2,
            Duration.ofMinutes(15),
            orderInfo,
            busySlots
        );

        assertTrue(baseTime2.isEqual(orderSlotFound.getIntervalStart()));
        assertTrue(baseTime2.plusMinutes(15).isEqual(orderSlotFound.getIntervalEnd()));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1})
    void orders_planned_for_the_future_should_never_have_overlapping_slots(int offset) {

        OrderStatusInfoDto orderInfo = mock(OrderStatusInfoDto.class);
        SortedSet<OrderTimeSlot> busySlots = new TreeSet<>();
        busySlots.add(new OrderTimeSlot(baseTime1, baseTime1.plusMinutes(7), mock(OrderStatusInfoDto.class)));

        Clock fixedClock = Clock.fixed(baseTime0.toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();

        OrderTimeSlot orderSlotFound = pizzaSchedulingService.getEarliestFreeSlot(
            baseTime1.plusMinutes(offset),
            Duration.ofMinutes(4),
            orderInfo,
            busySlots
        );

        assertTrue(baseTime1.plusMinutes(7).isEqual(orderSlotFound.getIntervalStart()));
        assertTrue(baseTime1.plusMinutes(7).plusMinutes(4).isEqual(orderSlotFound.getIntervalEnd()));
    }

    @Test
    void orders_planned_for_now_should_be_scheduled_before_orders_with_AsSoonAsPossibleSchedulingMode() {

        Clock fixedClock = Clock.fixed(baseTime1.minusMinutes(1).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();

        doReturn(new PizzaTypeDto().pizzaId("P1").preparationTimeMinutes(2))
            .when(pizzaCatalogCachingLayer).getPizzaTypeFromId("P1");

        OrderStatusInfoDto orderHighPrio = new OrderStatusInfoDto(
            new OrderDto(10L, "P1", new PlannedSchedulingModeDto(baseTime1))
                .orderCreationDateTime(baseTime1).orderId(1L),
            new ReadyOrderStatusDto(1L).statusEnteredDateTime(baseTime1)
        );
        OrderStatusInfoDto orderLowPrio = new OrderStatusInfoDto(
            new OrderDto(10L, "P1", new AsSoonAspossibleSchedulingModeDto())
                .orderCreationDateTime(baseTime0).orderId(2L),
            new ReadyOrderStatusDto(2L).statusEnteredDateTime(baseTime0)
        );

        Set<ScheduledOrderStatusDto> scheduledOrders = pizzaSchedulingService.computeNewSchedule(
            Set.of(),
            Set.of(orderLowPrio, orderHighPrio),
            Set.of(),
            Set.of(1L)
        );

        assertTrue(scheduledOrders.stream().allMatch(o -> o.getAssignedPizzaioloId() == 1L));
        assertTrue(scheduledOrders.stream().anyMatch(o ->
            o.getOrderId() == 1L && o.getScheduledCookingStartDateTime().isEqual(baseTime1))
        );
        assertTrue(scheduledOrders.stream().anyMatch(o ->
            o.getOrderId() == 2L && o.getScheduledCookingStartDateTime().isEqual(baseTime1.plusMinutes(2)))
        );

    }



    @Test
    void getNewEstimationForCookingEndTime() {
    }

}