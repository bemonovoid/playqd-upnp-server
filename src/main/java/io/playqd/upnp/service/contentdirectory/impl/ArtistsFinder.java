package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.Artist;
import io.playqd.upnp.model.BrowsableObject;
import io.playqd.upnp.persistence.jpa.dao.BrowseResult;
import io.playqd.upnp.service.contentdirectory.BrowseContext;
import io.playqd.upnp.service.contentdirectory.DcTagValues;
import io.playqd.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.upnp.service.contentdirectory.ObjectIdPattern;
import io.playqd.upnp.service.contentdirectory.UpnpClass;
import io.playqd.upnp.service.contentdirectory.UpnpTagValues;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
final class ArtistsFinder implements ObjectBrowser {

    private final MediaLibraryClient mediaLibraryClient;

    ArtistsFinder(MediaLibraryClient mediaLibraryClient) {
        this.mediaLibraryClient = mediaLibraryClient;
    }

    @Override
    public BrowseResult browse(BrowseContext context) {
        var browseRequest = context.getRequest();
        var startingIndex = browseRequest.getStartingIndex();
        var requestedCount = browseRequest.getRequestedCount();

        var artists = mediaLibraryClient.artists(Pageable.ofSize(requestedCount));

        if (startingIndex > artists.getTotalElements()) {
            return BrowseResult.empty();
        }

        if (startingIndex > 0) {
            var nextPageArtists = artists.stream()
                    .map(artist -> buildBrowsableObject(context, artist))
                    .toList();
            var offset = nextPageArtists.stream().skip(startingIndex).toList();
            var result = offset.stream().limit(requestedCount).toList();
            return new BrowseResult(offset.size(), result.size(), result);
        } else {
            var firstPageArtist = artists.stream()
                    .map(artist -> buildBrowsableObject(context, artist))
                    .limit(requestedCount)
                    .toList();
            return new BrowseResult(artists.getTotalElements(), artists.getNumberOfElements(), firstPageArtist);
        }
    }

    private static BrowsableObject buildBrowsableObject(BrowseContext context, Artist artist) {
        return BrowsableObjectImpl.builder()
                .objectId(buildArtistObjectId(context, artist))
                .parentObjectId(context.getRequest().getObjectID())
                .childCount(artist.albumsCount())
                .childContainerCount(artist.albumsCount())
                .dc(buildDcTagValues(artist))
                .upnp(buildUpnpTagValues(artist))
                .build();
    }

    private static String buildArtistObjectId(BrowseContext context, Artist artist) {
        var objectIdPattern =
                context.getRequiredHeader(BrowseContext.HEADER_OBJECT_ID_PATTERN, ObjectIdPattern.class);
        return objectIdPattern.compile(artist.id());
    }

    private static DcTagValues buildDcTagValues(Artist artist) {
        return DcTagValues.builder().title(artist.name()).build();
    }

    private static UpnpTagValues buildUpnpTagValues(Artist artist) {
        return UpnpTagValues.builder().upnpClass(UpnpClass.musicArtist).build();
    }

}
