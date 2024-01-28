package io.playqd.mediaserver.service.upnp.service.contentdirectory;

import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;

public interface ObjectBrowser {

    BrowseResult browse(BrowseContext context);

}
