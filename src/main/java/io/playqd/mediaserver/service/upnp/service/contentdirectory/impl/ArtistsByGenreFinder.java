package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.data.Artist;
import io.playqd.mediaserver.api.soap.data.Browse;
import io.playqd.mediaserver.client.MetadataClient;
import io.playqd.mediaserver.client.model.ArtistsQueryParams;
import io.playqd.mediaserver.model.BrowsableObject;
import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.DcTagValues;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectIdPattern;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpTagValues;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
final class ArtistsByGenreFinder implements ObjectBrowser {

    private final MetadataClient metadataClient;

    ArtistsByGenreFinder(MetadataClient metadataClient) {
        this.metadataClient = metadataClient;
    }

    @Override
    public BrowseResult browse(BrowseContext context) {
        var browseRequest = context.getRequest();

        var requestedCount = browseRequest.getRequestedCount();

        var artistsQueryParams = ArtistsQueryParams.builder().genreId(readGenreId(context)).build();
        var genreArtists = metadataClient.getArtists(artistsQueryParams, Pageable.ofSize(requestedCount));
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
