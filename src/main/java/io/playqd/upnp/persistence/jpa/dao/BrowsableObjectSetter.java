package io.playqd.upnp.persistence.jpa.dao;

import io.playqd.upnp.service.contentdirectory.UpnpClass;

public interface BrowsableObjectSetter {

    void setLocation(String location);

    void setChildCount(long count);

    void setChildContainerCount(long count);

    void setParentId(Long parentId);

    void setDcTitle(String dcTitle);

    void setUpnpClass(UpnpClass upnpClass);

    void setMimeType(String mimeType);

    void setSize(Long size);
}
