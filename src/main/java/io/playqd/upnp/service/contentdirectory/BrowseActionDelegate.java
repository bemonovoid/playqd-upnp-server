package io.playqd.upnp.service.contentdirectory;

import io.playqd.upnp.persistence.jpa.dao.BrowseResult;

public interface BrowseActionDelegate {

    BrowseResult browseRoots(BrowseContext browseActionContext);

    BrowseResult browseChildren(BrowseContext browseContext);

}
