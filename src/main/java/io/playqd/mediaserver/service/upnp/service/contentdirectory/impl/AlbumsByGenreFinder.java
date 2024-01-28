package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.data.Album;
import io.playqd.mediaserver.api.soap.data.Browse;
import io.playqd.mediaserver.client.MetadataClient;
import io.playqd.mediaserver.client.model.AlbumsQueryParams;
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
final class AlbumsByGenreFinder implements ObjectBrowser {

    private final MetadataClient metadataClient;

    AlbumsByGenreFinder(MetadataClient metadataClient) {
        this.metadataClient = metadataClient;
    }

    @Override
    public BrowseResult browse(BrowseContext context) {
        var albumsQueryParams = AlbumsQueryParams.builder().genreId(readGenreId(context)).build();
        var genreAlbums = metadataClient.getAlbums(albumsQueryParams, Pageable.unpaged());
        var result = genreAlbums.stream()
                .map(album -> buildBrowsableObject(context.getRequest(), album))
                .toList();
        return new BrowseResult(result.size(), result.size(), result);
    }

    private static String readGenreId(BrowseContext context) {
        return context.getRequiredHeader(BrowseContext.HEADER_GENRE_ID, String.class);
    }

    private BrowsableObject buildBrowsableObject(Browse browseRequest, Album album) {
        return BrowsableObjectImpl.builder()
                .objectId(buildAlbumObjectId(album))
                .parentObjectId(browseRequest.getObjectID())
                .childCount(album.tracksCount())
                .dc(buildDcTagValues(album))
                .upnp(buildUpnpTagValues(album))
                .build();
    }

    private static String buildAlbumObjectId(Album album) {
        return ObjectIdPattern.ARTIST_ALBUM_TRACKS_PATH.compile(album.artistId(), album.id());
    }

    private static DcTagValues buildDcTagValues(Album album) {
        return DcTagValues.builder()
                .title(album.name())
                .creator(album.artistName())
                .contributor(album.artistName())
                .build();
    }

    private UpnpTagValues buildUpnpTagValues(Album album) {
        return UpnpTagValues.builder()
                .upnpClass(UpnpClass.musicAlbum)
                .artist(album.artistName())
                .author(album.artistName())
                .producer(album.artistName())
                .genre(album.genre())
                .build();
    }
}
