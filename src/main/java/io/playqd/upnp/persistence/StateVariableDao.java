package io.playqd.upnp.persistence;

import io.playqd.upnp.service.StateVariable;
import io.playqd.upnp.service.StateVariableName;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public interface StateVariableDao {

    Set<StateVariable> getAll();

    <T> Optional<T> get(StateVariableName stateVariable);

    <T extends Serializable> void set(StateVariableName stateVariable, T value);

}
