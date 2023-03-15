package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.order;

import it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype.PizzaType;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.SchedulingModeDto;
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
@Table(name = "`Order`")
@NoArgsConstructor
public class Order implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;

    String pizzaTypeId;

    String notes;

    SchedulingModeDto schedulingMode;

    @Setter(value = AccessLevel.PACKAGE)
    OffsetDateTime OrderCreationDateTime;

    @OneToOne(mappedBy = "order")
    @Setter(value = AccessLevel.PACKAGE)
    OrderStatus orderStatus;

    void updateOrder(
        Long userId,
        String pizzaTypeId,
        String notes,
        SchedulingModeDto schedulingMode
    ) {
        this.userId = userId;
        this.pizzaTypeId = pizzaTypeId;
        this.notes = notes;
        this.schedulingMode = schedulingMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Order entity = (Order) o;
        return Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Order.class, this.getPizzaTypeId(), this.getUserId());
    }

}
