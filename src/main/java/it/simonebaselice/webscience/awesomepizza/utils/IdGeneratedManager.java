package it.simonebaselice.webscience.awesomepizza.utils;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class IdGeneratedManager<T extends Identifiable<K>, K, D> extends BaseManager<T, K, D>{


    protected IdGeneratedManager(CrudRepository<T, K> crudRepository) {
        super(crudRepository);
    }

    public T createNew(){
        return crudRepository.save(createDefault());
    }

    public T createNewAndUpdate(D dto){
        return updateEntity(createNew(), dto);
    }

    public T getOrCreateDefault(Supplier<Optional<T>> searchCriteria){
        return searchCriteria.get().orElseGet(() -> crudRepository.save(createDefault()));
    }
    protected abstract T createDefault();


}
