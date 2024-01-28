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
final class TracksRecentlyAddedFinder extends AbstractTracksFinder {

    TracksRecentlyAddedFinder(PlayqdProperties playqdProperties, MetadataClient metadataClient) {
        super(playqdProperties, metadataClient);
    }

    @Override
    protected String buildItemObjectId(Track track) {
        return ObjectIdPattern.ARTIST_ALBUM_TRACK_PATH
                .compile(track.artist().id(), track.album().id(), track.uuid());
    }

    @Override
    protected Page<Track> findAudioFiles(BrowseContext context, Pageable pageable) {
        return metadataClient.getRecentlyAdded(pageable);
    }

    @Override
    protected int calculateRequestedCount(BrowseContext context) {
        var maxDisplayedCount = playqdProperties.getUpnp().getAction().getBrowse().getMaxDisplayedRecentlyAdded();
        return maxDisplayedCount > 0 ? maxDisplayedCount : super.calculateRequestedCount(context);
    }

    @Override
    protected String getDcTitle(BrowseContext context, Track track)  {
        return track.artist().name() + " - " + super.getDcTitle(context, track);
    }

}