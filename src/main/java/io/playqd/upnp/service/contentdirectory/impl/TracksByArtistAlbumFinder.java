package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.Track;
import io.playqd.upnp.config.properties.PlayqdProperties;
import io.playqd.upnp.service.contentdirectory.BrowseContext;
import io.playqd.upnp.service.contentdirectory.ObjectIdPattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
final class TracksByArtistAlbumFinder extends AbstractTracksFinder {

    TracksByArtistAlbumFinder(PlayqdProperties playqdProperties, MediaLibraryClient mediaLibraryClient) {
        super(playqdProperties, mediaLibraryClient);
    }

    @Override
    protected String buildItemObjectId(Track track) {
        return ObjectIdPattern.ARTIST_ALBUM_TRACK_PATH.compile(
                track.artist().id(), track.album().id(), track.uuid());
    }

    @Override
    protected Page<Track> findAudioFiles(BrowseContext context, Pageable pageable) {
        return mediaLibraryClient.tracksByAlbumId(pageable, readAlbumId(context));
    }

    private static String readAlbumId(BrowseContext context) {
        return context.getRequiredHeader(BrowseContext.HEADER_ALBUM_ID, String.class);
    }
}