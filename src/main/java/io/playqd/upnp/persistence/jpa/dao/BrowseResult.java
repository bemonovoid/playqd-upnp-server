package io.playqd.upnp.persistence.jpa.dao;

import io.playqd.upnp.model.BrowsableObject;
import io.playqd.upnp.service.ActionResponse;

import java.util.Collections;
import java.util.List;

public record BrowseResult(long totalMatches, int numberReturned, List<BrowsableObject> objects)
        implements ActionResponse {

    public static BrowseResult empty() {
        return new BrowseResult(0, 0, Collections.emptyList());
    }

}
