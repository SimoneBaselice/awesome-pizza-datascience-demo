package it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.IngredientQuantityDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.IngredientQuantityQuantityDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaTypeDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaTypeRecipeDto;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

public class PizzaTypeUtils {

    public static PizzaTypeDto buildDto(PizzaType entity) {
        return new PizzaTypeDto()
            .pizzaId(entity.getPizzaId())
            .name(entity.getName())
        .description(entity.getDescription())
        .ingredients(entity.getIngredients().stream()
            .map(i -> new IngredientQuantityDto()
                .ingredientId(i.getIngredientId())
                .quantity(new IngredientQuantityQuantityDto().unit(i.getUnit()).value(i.getValue()))
            ).collect(Collectors.toList())
        ).recipe(entity.getRecipe())
        .preparationTimeMinutes(entity.getPreparationTimeMinutes())
        .image(entity.getImage())
        .isActive(entity.getIsActive());
    }

}
