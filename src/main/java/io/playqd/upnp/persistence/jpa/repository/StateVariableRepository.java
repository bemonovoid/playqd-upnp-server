package io.playqd.upnp.persistence.jpa.repository;

import io.playqd.upnp.persistence.jpa.entity.StateVariableJpaEntity;
import io.playqd.upnp.service.StateVariableName;

import java.util.Optional;

public interface StateVariableRepository extends IdentityJpaRepository<StateVariableJpaEntity> {

    Optional<StateVariableJpaEntity> findFirstByKey(StateVariableName stateVariable);

}
