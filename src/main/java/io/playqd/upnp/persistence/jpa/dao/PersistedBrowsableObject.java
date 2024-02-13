package io.playqd.upnp.persistence.jpa.dao;

import io.playqd.upnp.service.contentdirectory.UpnpClass;

import java.util.function.Supplier;

public record PersistedBrowsableObject(long id,
                                       Long parentId,
                                       String objectId,
                                       String dcTitle,
                                       String location,
                                       UpnpClass upnpClass,
                                       String mimeType,
                                       Long size,
                                       Supplier<Long> childCount,
                                       long childContainerCount) {
  public boolean isRoot() {
    return parentId == null;
  }
}
