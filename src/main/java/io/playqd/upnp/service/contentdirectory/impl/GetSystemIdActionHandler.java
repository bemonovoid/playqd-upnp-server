package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.upnp.service.StateVariableContextHolder;
import io.playqd.upnp.service.StateVariableName;
import io.playqd.upnp.service.UpnpActionHandler;
import io.playqd.upnp.service.contentdirectory.SimpleActionContext;
import org.springframework.stereotype.Component;

@Component
class GetSystemIdActionHandler implements UpnpActionHandler<SimpleActionContext, Integer> {

  private final StateVariableContextHolder stateVariableContextHolder;

  GetSystemIdActionHandler(StateVariableContextHolder stateVariableContextHolder) {
    this.stateVariableContextHolder = stateVariableContextHolder;
  }

  @Override
  public Integer handle(SimpleActionContext context) {
    return stateVariableContextHolder.getOrThrow(StateVariableName.UPNP_SYSTEM_UPDATE_ID);
  }
}