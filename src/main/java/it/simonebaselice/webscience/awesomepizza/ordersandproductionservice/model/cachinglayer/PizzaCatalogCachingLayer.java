package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.cachinglayer;

import it.simonebaselice.webscience.awesomepizza.catalogservice.controller.PizzaCatalogController;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaTypeDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class PizzaCatalogCachingLayer {

    private final PizzaCatalogController pizzaCatalogController;

    private Map<String, PizzaTypeDto> cacheMap = Map.of();

    public PizzaCatalogCachingLayer(PizzaCatalogController pizzaCatalogController) {
        this.pizzaCatalogController = pizzaCatalogController;
    }

    public PizzaTypeDto getPizzaTypeFromId(String pizzaId) {
        if(!cacheMap.containsKey(pizzaId))
            refreshCache();
        PizzaTypeDto pizzaType = cacheMap.get(pizzaId);
        if(pizzaType == null)
            throw new NoSuchElementException();
        return pizzaType;
    }

    protected void refreshCache() {
        cacheMap = pizzaCatalogController.getAllPizzaTypes().getBody().stream()
            .collect(Collectors.toMap(PizzaTypeDto::getPizzaId, pizzaType -> pizzaType));
    }


}
