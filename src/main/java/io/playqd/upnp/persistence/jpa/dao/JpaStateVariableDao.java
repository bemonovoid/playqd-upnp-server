package io.playqd.upnp.persistence.jpa.dao;

import io.playqd.upnp.persistence.StateVariableDao;
import io.playqd.upnp.persistence.jpa.entity.StateVariableJpaEntity;
import io.playqd.upnp.persistence.jpa.repository.StateVariableRepository;
import io.playqd.upnp.service.StateVariable;
import io.playqd.upnp.service.StateVariableName;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class JpaStateVariableDao implements StateVariableDao {

    private final StateVariableRepository repository;

    JpaStateVariableDao(StateVariableRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<StateVariable> getAll() {
        return repository.findAll().stream()
                .map(entity -> new StateVariable(entity.getKey(), entity.getValue(), entity.getLastModifiedDate()))
                .collect(Collectors.toSet());
    }

    @Override
    public <T> Optional<T> get(StateVariableName stateVariable) {
        return repository.findFirstByKey(stateVariable)
                .map(entity -> {
                    //noinspection unchecked
                    return (T) stateVariable.getDeserializer().apply(entity.getValue());
                });
    }

    @Override
    public <T extends Serializable> void set(StateVariableName stateVariable, T value) {
        var entity = repository.findFirstByKey(stateVariable).orElseGet(() -> {
            var e = new StateVariableJpaEntity();
            e.setKey(stateVariable);
            return e;
        });
        entity.setValue(stateVariable.getSerializer().apply(value));
        repository.saveAndFlush(entity);
    }

}
