package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.data.Track;
import io.playqd.mediaserver.client.MetadataClient;
import io.playqd.mediaserver.config.properties.PlayqdProperties;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectIdPattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class TracksByPlaylistFinder extends AbstractTracksFinder {

    TracksByPlaylistFinder(PlayqdProperties playqdProperties, MetadataClient metadataClient) {
        super(playqdProperties, metadataClient);
    }

    @Override
    protected String buildItemObjectId(Track track) {
        return ObjectIdPattern.ARTIST_ALBUM_TRACK_PATH
                .compile(track.artist().id(), track.album().id(), track.uuid());
    }

    @Override
    protected Page<Track> findAudioFiles(BrowseContext context, Pageable pageable) {
        return metadataClient.getPlaylistTracks(readPlaylistId(context));
    }

    @Override
    protected String getDcTitle(BrowseContext context, Track track) {
        return track.artist().name() + " - " + super.getDcTitle(context, track);
    }

    private static String readPlaylistId(BrowseContext context) {
        return context.getRequiredHeader(BrowseContext.HEADER_PLAYLIST_ID);
    }
}
