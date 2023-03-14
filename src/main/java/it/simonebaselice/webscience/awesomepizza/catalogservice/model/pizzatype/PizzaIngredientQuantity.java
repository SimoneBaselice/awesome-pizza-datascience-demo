package it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.IngredientUnitDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PizzaIngredientQuantity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String ingredientId;

    Float value;

    IngredientUnitDto unit;

    void updatePizzaIngredientQuantity(
        String ingredientId,
        Float value,
        IngredientUnitDto unit
    ) {
        this.ingredientId = ingredientId;
        this.value = value;
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PizzaIngredientQuantity entity = (PizzaIngredientQuantity) o;
        return Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(PizzaIngredientQuantity.class);
    }

}
