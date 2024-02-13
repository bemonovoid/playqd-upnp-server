package io.playqd.upnp.service.contentdirectory;

import io.playqd.upnp.persistence.jpa.dao.BrowseResult;

public interface ObjectBrowser {

    BrowseResult browse(BrowseContext context);

}
