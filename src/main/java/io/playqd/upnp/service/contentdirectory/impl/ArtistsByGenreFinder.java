package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.Artist;
import io.playqd.upnp.api.soap.data.Browse;
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
final class ArtistsByGenreFinder implements ObjectBrowser {

    private final MediaLibraryClient mediaLibraryClient;

    ArtistsByGenreFinder(MediaLibraryClient mediaLibraryClient) {
        this.mediaLibraryClient = mediaLibraryClient;
    }

    @Override
    public BrowseResult browse(BrowseContext context) {
        var browseRequest = context.getRequest();

        var requestedCount = browseRequest.getRequestedCount();

        var genreArtists = mediaLibraryClient.artistsByGenre(Pageable.ofSize(requestedCount), readGenreId(context));
        var result = genreArtists.map(artist -> buildBrowsableObject(context.getRequest(), artist)).toList();
        return new BrowseResult(result.size(), result.size(), result);
    }

    private static String readGenreId(BrowseContext context) {
        return context.getRequiredHeader(BrowseContext.HEADER_GENRE_ID, String.class);
    }

    private BrowsableObject buildBrowsableObject(Browse browseRequest, Artist artist) {
        return BrowsableObjectImpl.builder()
                .objectId(buildArtistObjectId(artist))
                .parentObjectId(browseRequest.getObjectID())
                .childCount(artist.albumsCount())
                .childContainerCount(artist.albumsCount())
                .dc(buildDcTagValues(artist))
                .upnp(buildUpnpTagValues(artist))
                .build();
    }

    private static String buildArtistObjectId(Artist artist) {
        return ObjectIdPattern.ARTIST_ALBUMS_PATH.compile(artist.id());
    }

    private static DcTagValues buildDcTagValues(Artist artist) {
        return DcTagValues.builder().title(artist.name()).build();
    }

    private UpnpTagValues buildUpnpTagValues(Artist artist) {
        return UpnpTagValues.builder().upnpClass(UpnpClass.musicArtist).build();
    }
}
