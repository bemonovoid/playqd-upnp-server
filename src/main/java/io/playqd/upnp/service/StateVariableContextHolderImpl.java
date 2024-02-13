package io.playqd.upnp.service;

import io.playqd.upnp.exception.PlayqdException;
import io.playqd.upnp.persistence.StateVariableDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@Component
class StateVariableContextHolderImpl implements StateVariableContextHolder {

    private final StateVariableDao stateVariableDao;

    StateVariableContextHolderImpl(StateVariableDao stateVariableDao) {
        this.stateVariableDao = stateVariableDao;
    }

    @Override
    public Set<StateVariable> getAll() {
        return stateVariableDao.getAll();
    }

    @Override
    public <T> T getOrThrow(StateVariableName stateVariable) {
        Optional<T> mayBeStateVariable = get(stateVariable);
        if (mayBeStateVariable.isEmpty()) {
            throw new PlayqdException(stateVariable.getVariableName() + " was not found");
        }
        return mayBeStateVariable.get();
    }

    @Override
    public <T> Optional<T> get(StateVariableName stateVariable) {
        return stateVariableDao.get(stateVariable);
    }

    @Override
    public <T extends Serializable> T getOrUpdate(StateVariableName stateVariable, Supplier<T> newValue) {
        Optional<T> mayBeStateVariable = get(stateVariable);
        if (mayBeStateVariable.isEmpty()) {
            set(stateVariable, newValue.get());
        }
        return mayBeStateVariable.orElse(newValue.get());
    }

    @Override
    public <T extends Serializable> void set(StateVariableName stateVariable, T newValue) {
        stateVariableDao.set(stateVariable, newValue);
    }

    private void updateSystemUpdateId() {
        Integer oldSystemUpdateId = getOrThrow(StateVariableName.UPNP_SYSTEM_UPDATE_ID);
        var newSystemUpdateId = oldSystemUpdateId + 1;
        set(StateVariableName.UPNP_SYSTEM_UPDATE_ID, newSystemUpdateId);
        log.info("'SystemUpdateID' was updated from '{}' to '{}'", oldSystemUpdateId, newSystemUpdateId);
    }

}
