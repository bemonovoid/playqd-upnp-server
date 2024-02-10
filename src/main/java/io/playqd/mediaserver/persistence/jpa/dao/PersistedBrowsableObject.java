package io.playqd.mediaserver.persistence.jpa.dao;

import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;

import java.nio.file.Path;
import java.util.function.Supplier;

public record PersistedBrowsableObject(long id,
                                       Long parentId,
                                       String objectId,
                                       String dcTitle,
                                       Path location,
                                       UpnpClass upnpClass,
                                       String mimeType,
                                       Long size,
                                       Supplier<Long> childCount,
                                       long childContainerCount) {
  public boolean isRoot() {
    return parentId == null;
  }
}
