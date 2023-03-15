package it.simonebaselice.webscience.awesomepizza.catalogservice.controller;

import it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype.PizzaType;
import it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype.PizzaTypeManager;
import it.simonebaselice.webscience.awesomepizza.catalogservice.model.pizzatype.PizzaTypeDtoUtils;
import it.simonebaselice.webscience.awesomepizza.generated.http.controller.PizzaTypesApi;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaTypeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PizzaCatalogController implements PizzaTypesApi {

    private final PizzaTypeManager pizzaTypeManager;

    public PizzaCatalogController(PizzaTypeManager pizzaTypeManager) {
        this.pizzaTypeManager = pizzaTypeManager;
    }

    /**
     * POST /pizza-types :
     *
     * @param pizzaTypeDto (optional)
     * @return Created (status code 201)
     * or  (status code 400)
     * or  (status code 404)
     * or  (status code 500)
     */
    @Override
    @Transactional
    public ResponseEntity<PizzaTypeDto> createPizzaType(
        @Valid PizzaTypeDto pizzaTypeDto
    ) {
        PizzaType entity = pizzaTypeManager.createNewAndUpdate(pizzaTypeDto.getPizzaId(), pizzaTypeDto);
        return new ResponseEntity<>(PizzaTypeDtoUtils.buildPizzaTypeDto(entity), HttpStatus.CREATED);
    }

    /**
     * PUT /pizza-types/{id} : /pizza-types/{id}
     *
     * @param id           (required)
     * @param pizzaTypeDto (optional)
     * @return Accepted (status code 202)
     * or  (status code 400)
     * or  (status code 403)
     * or  (status code 404)
     * or  (status code 500)
     */
    @Override
    @Transactional
    public ResponseEntity<PizzaTypeDto> editPizzaType(
        String id, @Valid PizzaTypeDto pizzaTypeDto
    ) {
        PizzaType entity = pizzaTypeManager.update(pizzaTypeDto.getPizzaId(), pizzaTypeDto);
        return new ResponseEntity<>(PizzaTypeDtoUtils.buildPizzaTypeDto(entity), HttpStatus.ACCEPTED);
    }

    /**
     * GET /pizza-types : Pizza types
     *
     * @return OK (status code 200)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<List<PizzaTypeDto>> getAllPizzaTypes() {
        return new ResponseEntity<>(
            pizzaTypeManager.getAll().stream().map(PizzaTypeDtoUtils::buildPizzaTypeDto).collect(Collectors.toList()),
            HttpStatus.OK
        );

    }

    /**
     * GET /pizza-types/{id} : Pizza type
     *
     * @param id (required)
     * @return OK (status code 200)
     * or  (status code 403)
     * or  (status code 404)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<PizzaTypeDto> getPizzaType(
        String id
    ) {
        return new ResponseEntity<>(PizzaTypeDtoUtils.buildPizzaTypeDto(pizzaTypeManager.getById(id)), HttpStatus.OK);
    }

}
