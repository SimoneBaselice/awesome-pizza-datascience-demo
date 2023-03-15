package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.OrderStatusDto;
import it.simonebaselice.webscience.awesomepizza.utils.Identifiable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
public class OrderStatus implements Identifiable<Long> {

    public OrderStatus(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @Setter(value = AccessLevel.PACKAGE)
    Order order;

    @Enumerated(EnumType.STRING)
    OrderStatusType status;

    OrderStatusDto statusInfo;

    @Setter(value = AccessLevel.PACKAGE)
    OffsetDateTime statusEnteredDateTime;

    void updateOrderStatus(
        OrderStatusType status,
        OrderStatusDto statusInfo
    ) {
        this.status = status;
        this.statusInfo = statusInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OrderStatus entity = (OrderStatus) o;
        return Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(OrderStatus.class, this.getOrder().getUserId(), this.getOrder().getPizzaTypeId());
    }

}
