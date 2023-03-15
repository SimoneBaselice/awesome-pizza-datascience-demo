package it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaTypeRecipeDto;
import it.simonebaselice.webscience.awesomepizza.utils.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
public class PizzaType implements Identifiable<String> {

    @Id
    String pizzaId;

    String name;

    String description;

    @OneToMany
    List<PizzaIngredientQuantity> ingredients = new ArrayList<>();

    PizzaTypeRecipeDto recipe;

    Integer preparationTimeMinutes;

    String image;

    Boolean isActive;

    PizzaType(String pizzaId) {
        this.pizzaId = pizzaId;
    }

    void updatePizzaType(
        String name,
        String description,
        List<PizzaIngredientQuantity> ingredients,
        PizzaTypeRecipeDto recipe,
        Integer preparationTimeMinutes,
        String image,
        Boolean isActive
    ) {
        this.name = name;
        this.description = description;
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
        this.recipe = recipe;
        this.preparationTimeMinutes = preparationTimeMinutes;
        this.image = image;
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PizzaType entity = (PizzaType) o;
        return Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(PizzaType.class, this.getPizzaId());
    }

    @Override
    public String getId() {
        return pizzaId;
    }

}
