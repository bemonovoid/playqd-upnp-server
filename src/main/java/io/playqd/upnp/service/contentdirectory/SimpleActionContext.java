package io.playqd.upnp.service.contentdirectory;

import io.playqd.upnp.service.ActionContext;

public class SimpleActionContext extends ActionContext<Void> {

  public SimpleActionContext() {
    super();
  }

  @Override
  public Void getRequest() {
    return null;
  }

}
