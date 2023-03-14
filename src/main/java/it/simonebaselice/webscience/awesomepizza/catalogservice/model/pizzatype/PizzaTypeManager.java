package it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype;

import it.simonebaselice.webscience.awesomepizza.generated.http.model.IngredientQuantityDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaTypeDto;
import it.simonebaselice.webscience.awesomepizza.utils.IdAssignedManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PizzaTypeManager extends IdAssignedManager<PizzaType, String, PizzaTypeDto> {

    private final PizzaTypeRepository pizzaTypeRepository;
    private final PizzaIngredientQuantityRepository pizzaIngredientQuantityRepository;


    protected PizzaTypeManager(
        PizzaTypeRepository pizzaTypeRepository,
        PizzaIngredientQuantityRepository pizzaIngredientQuantityRepository
    ) {
        super(pizzaTypeRepository);
        this.pizzaTypeRepository = pizzaTypeRepository;
        this.pizzaIngredientQuantityRepository = pizzaIngredientQuantityRepository;
    }


    @Override
    protected PizzaType updateEntity(
        PizzaType currentState, PizzaTypeDto desiredState
    ) {

        // we need to remove old ingredients that are no longer present, update the already existing ingredients and
        // add the new ones

        Map<String, IngredientQuantityDto> desiredIngredientsMap = desiredState.getIngredients().stream()
            .collect(Collectors.toMap(IngredientQuantityDto::getIngredientId, i -> i));

        List<PizzaIngredientQuantity> ingredientsToKeep = currentState.getIngredients().stream()
            .filter(ingredient -> desiredIngredientsMap.containsKey(ingredient.getIngredientId()))
            .collect(Collectors.toList());

        //update old ingredients to keep
        ingredientsToKeep.forEach(i -> {
            IngredientQuantityDto desiredIngredient = desiredIngredientsMap.get(i.getIngredientId());
            i.updatePizzaIngredientQuantity(
                desiredIngredient.getIngredientId(),
                desiredIngredient.getQuantity().getValue(),
                desiredIngredient.getQuantity().getUnit()
            );
        });

        Set<String> ingredientsToKeepId = ingredientsToKeep.stream()
            .map(PizzaIngredientQuantity::getIngredientId)
            .collect(Collectors.toSet());

        // remove old ingredients that are no longer present
        Set<PizzaIngredientQuantity> oldIngredients = new HashSet<>(currentState.ingredients);
        currentState.ingredients.clear();
        oldIngredients.stream()
            .filter(i -> !ingredientsToKeepId.contains(i.getIngredientId()))
            .forEach(pizzaIngredientQuantityRepository::delete);

        // create new ingredients
        List<PizzaIngredientQuantity> newIngredients = desiredState.getIngredients().stream()
            .filter(i -> !ingredientsToKeepId.contains(i.getIngredientId()))
            .map(i -> new PizzaIngredientQuantity(
                null,
                i.getIngredientId(),
                i.getQuantity().getValue(),
                i.getQuantity().getUnit()
            ))
            .map(pizzaIngredientQuantityRepository::save)
            .collect(Collectors.toList());

        List<PizzaIngredientQuantity> finalIngredients = new ArrayList<>(ingredientsToKeep);
        finalIngredients.addAll(newIngredients);

        currentState.updatePizzaType(
            desiredState.getName(),
            desiredState.getDescription(),
            finalIngredients,
            desiredState.getRecipe(),
            desiredState.getPreparationTimeMinutes(),
            desiredState.getImage(),
            desiredState.getIsActive()
        );

        return currentState;

    }

    @Override
    protected PizzaType createDefault(String key) {
        return new PizzaType(key);
    }

}
