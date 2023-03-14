package it.simonebaselice.webscience.awesomepizza.utils;

import org.springframework.data.repository.CrudRepository;

public abstract class IdAssignedManager<T extends Identifiable<K>, K, D> extends BaseManager<T, K, D>{
    protected IdAssignedManager(CrudRepository<T, K> crudRepository) {
        super(crudRepository);
    }

    public T createNew(K key){
        if(crudRepository.existsById(key))
            throw new IllegalArgumentException(String.format(
                    "Cannot create entity with id %s because there is another persisted instance with the same id",
                    key
            ));
        return crudRepository.save(createDefault(key));
    }
    public T createNewAndUpdate(K key, D dto){
        return updateEntity(createNew(key), dto);
    }

    public T getOrCreateNewAndUpdate(K key, D dto){
        return crudRepository.findById(key)
                .map(entity -> updateEntity(entity, dto))
                .orElseGet(() -> updateEntity(createNew(key), dto));
    }
    protected abstract T createDefault(K key);
}
