package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.upnp.api.soap.data.Browse;
import io.playqd.upnp.model.BrowsableObject;
import io.playqd.upnp.persistence.jpa.dao.BrowseResult;
import io.playqd.upnp.service.contentdirectory.BrowseContext;
import io.playqd.upnp.service.contentdirectory.DcTagValues;
import io.playqd.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.upnp.service.contentdirectory.SystemContainerName;
import io.playqd.upnp.service.contentdirectory.UpnpClass;
import io.playqd.upnp.service.contentdirectory.UpnpTagValues;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
final class MusicLibraryChildrenFinder implements ObjectBrowser {

    private final MediaLibraryClient mediaLibraryClient;

    MusicLibraryChildrenFinder(MediaLibraryClient mediaLibraryClient) {
        this.mediaLibraryClient = mediaLibraryClient;
    }

    @Override
    public BrowseResult browse(BrowseContext context) {
        var result = SystemContainerName.getMusicLibraryChildren().stream()
                .map(container -> buildContainer(context.getRequest(), container))
                .sorted(Comparator.comparing(BrowsableObject::getSortOrderId))
                .toList();
        return new BrowseResult(result.size(), result.size(), result);
    }

    private BrowsableObject buildContainer(Browse browseRequest, SystemContainerName container) {
        var count = countChildren(container);
        return BrowsableObjectImpl.builder()
                .objectId(container.getObjectId())
                .parentObjectId(browseRequest.getObjectID())
                .childCount(count)
                .childContainerCount(countChildContainers(count, container))
                .sortOrderId(container.getOrderId())
                .dc(buildDcTagValues(container))
                .upnp(buildUpnpTagValues(container))
                .build();
    }

    private long countChildren(SystemContainerName container) {
        switch (container) {
            case ARTIST_ALBUM, ARTIST_TRACK -> {
                return mediaLibraryClient.artistsCount().count();
            }
            case GENRE_ARTIST, GENRE_ALBUM -> {
                return mediaLibraryClient.genresCount().count();
            }
            case TRACKS_MOST_PLAYED, TRACKS_RECENTLY_PLAYED -> {
                return mediaLibraryClient.tracksPlayedCount().count();
            }
            case TRACKS_RECENTLY_ADDED -> {
                return mediaLibraryClient.tracksLastRecentlyAddedCount().count();
            }
            default -> {
                return 0;
            }
        }
    }

    private static long countChildContainers(long childCount, SystemContainerName container) {
        switch (container) {
            case ARTIST_ALBUM, ARTIST_TRACK, GENRE_ARTIST, GENRE_ALBUM -> {
                return childCount;
            }
            default -> {
                return 0;
            }
        }
    }

    private static DcTagValues buildDcTagValues(SystemContainerName container) {
        return DcTagValues.builder()
                .title(container.getDcTitleName())
                .build();
    }

    private static UpnpTagValues buildUpnpTagValues(SystemContainerName container) {
        return UpnpTagValues.builder().upnpClass(UpnpClass.storageFolder).build();
    }
}
