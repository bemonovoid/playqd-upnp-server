package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.Track;
import io.playqd.mediaserver.config.properties.PlayqdProperties;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectIdPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
final class TracksByArtistFinder extends AbstractTracksFinder {

    TracksByArtistFinder(PlayqdProperties playqdProperties, MediaLibraryClient mediaLibraryClient) {
        super(playqdProperties, mediaLibraryClient);
    }

    @Override
    protected String buildItemObjectId(Track track) {
        return ObjectIdPattern.ARTIST_TRACK_PATH.compile(track.artist().id(), track.uuid());
    }

    @Override
    protected Page<Track> findAudioFiles(BrowseContext context, Pageable pageable) {
        return mediaLibraryClient.tracksByArtistId(pageable, readArtistId(context));
    }

    private static String readArtistId(BrowseContext context) {
        return context.getRequiredHeader(BrowseContext.HEADER_ARTIST_ID, String.class);
    }
}