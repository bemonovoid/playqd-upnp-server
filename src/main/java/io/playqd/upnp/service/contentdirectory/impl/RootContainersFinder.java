package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.upnp.model.BrowsableObject;
import io.playqd.upnp.persistence.jpa.dao.BrowseResult;
import io.playqd.upnp.service.contentdirectory.BrowseContext;
import io.playqd.upnp.service.contentdirectory.DcTagValues;
import io.playqd.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.upnp.service.contentdirectory.SystemContainerName;
import io.playqd.upnp.service.contentdirectory.UpnpClass;
import io.playqd.upnp.service.contentdirectory.UpnpTagValues;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
final class RootContainersFinder implements ObjectBrowser {

    private final MediaLibraryClient mediaLibraryClient;

    RootContainersFinder(MediaLibraryClient mediaLibraryClient) {
        this.mediaLibraryClient = mediaLibraryClient;
    }

    @Override
    public BrowseResult browse(BrowseContext context) {
        return new BrowseResult(
                2, 2, List.of(buildFoldersRoot(), buildMusicLibraryRoot()));
    }

    private BrowsableObject buildFoldersRoot() {
        var count = mediaLibraryClient.musicDirectories().size();
        return BrowsableObjectImpl.builder()
                .objectId(SystemContainerName.ROOT_FOLDERS.getObjectId())
                .parentObjectId("-1")
                .childCount(count)
                .childContainerCount(count)
                .sortOrderId(SystemContainerName.ROOT_FOLDERS.getOrderId())
                .dc(buildDcTagValues(SystemContainerName.ROOT_FOLDERS))
                .upnp(buildUpnpTagValues())
                .build();
    }

    private BrowsableObject buildMusicLibraryRoot() {
        return BrowsableObjectImpl.builder()
                .objectId(SystemContainerName.ROOT_MUSIC_LIBRARY.getObjectId())
                .parentObjectId("-1")
                .sortOrderId(SystemContainerName.ROOT_MUSIC_LIBRARY.getOrderId())
                .childCount(SystemContainerName.getMusicLibraryChildren().size())
                .childContainerCount(SystemContainerName.getMusicLibraryChildren().size())
                .dc(buildDcTagValues(SystemContainerName.ROOT_MUSIC_LIBRARY))
                .upnp(buildUpnpTagValues())
                .build();
    }

    private static DcTagValues buildDcTagValues(SystemContainerName virtualContainer) {
        return DcTagValues.builder()
                .title(virtualContainer.getDcTitleName())
                .build();
    }

    private static UpnpTagValues buildUpnpTagValues() {
        return UpnpTagValues.builder().upnpClass(UpnpClass.storageFolder).build();
    }
}
