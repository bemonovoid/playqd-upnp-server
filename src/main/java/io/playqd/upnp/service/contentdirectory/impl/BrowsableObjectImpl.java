package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.upnp.model.BrowsableObject;
import io.playqd.upnp.service.contentdirectory.DcTagValues;
import io.playqd.upnp.service.contentdirectory.ResTag;
import io.playqd.upnp.service.contentdirectory.UpnpTagValues;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BrowsableObjectImpl implements BrowsableObject {

    private final long childCount;
    private final long childContainerCount;
    private final String objectId;
    private final String parentObjectId;
    private final int sortOrderId;
    private final boolean restricted = true;
    private final boolean searchable;
    private final DcTagValues dc;
    private final UpnpTagValues upnp;
    private final List<ResTag> resources;

    @Override
    public Long getParentId() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return getChildCount() > 0;
    }

    @Override
    public final Long getId() {
        return null;
    }
}