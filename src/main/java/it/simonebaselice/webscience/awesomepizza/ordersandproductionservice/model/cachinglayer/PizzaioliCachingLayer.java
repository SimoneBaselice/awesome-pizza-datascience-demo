package it.simonebaselice.webscience.awesomepizza.ordersandproductionservice.model.cachinglayer;

import it.simonebaselice.webscience.awesomepizza.clients.controller.ClientsController;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaTypeDto;
import it.simonebaselice.webscience.awesomepizza.generated.http.model.PizzaioloClientDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PizzaioliCachingLayer {

    private final ClientsController clientsController;

    private List<PizzaioloClientDto> pizzaioli;

    public PizzaioliCachingLayer(ClientsController clientsController) {
        this.clientsController = clientsController;
        refreshCache();
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void refreshCache() {
        pizzaioli = new ArrayList<>(clientsController.getAllPizzaioli().getBody());
    }

    public Set<PizzaioloClientDto> getAllPizzaioli() {
        return new HashSet<>(pizzaioli);
    }

    public Set<Long> getAllPizzaioliIds() {
        return pizzaioli.stream().map(PizzaioloClientDto::getId).collect(Collectors.toSet());
    }


}
