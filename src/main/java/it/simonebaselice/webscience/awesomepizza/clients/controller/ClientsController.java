package it.simonebaselice.webscience.awesomepizza.clients.controller;

import it.simonebaselice.webscience.awesomepizza.generated.http.controller.ClientsApi;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaioloClientDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClientsController implements ClientsApi {

    /**
     * GET /clients/pizzaioli : Pizzaioli collection
     * Get all the temporary user
     *
     * @return OK (status code 200)
     * or  (status code 500)
     */
    @Override
    public ResponseEntity<List<PizzaioloClientDto>> getAllPizzaioli() {
        return new ResponseEntity<>(
            List.of(
                new PizzaioloClientDto().name("Luca").id(1L).email("wowpizzo@gmail.com")
            ),
            HttpStatus.OK
        );
    }

}
