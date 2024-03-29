package io.playqd.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.client.request.Sorting;
import io.playqd.commons.data.Track;
import io.playqd.upnp.config.properties.PlayqdProperties;
import io.playqd.upnp.service.contentdirectory.BrowseContext;
import io.playqd.upnp.service.contentdirectory.ObjectIdPattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
final class TracksMostPlayedFinder extends AbstractTracksFinder {

    TracksMostPlayedFinder(PlayqdProperties playqdProperties, MediaLibraryClient mediaLibraryClient) {
        super(playqdProperties, mediaLibraryClient);
    }

    @Override
    protected String buildItemObjectId(Track track) {
        return ObjectIdPattern.ARTIST_ALBUM_TRACK_PATH
                .compile(track.artist().id(), track.album().id(), track.uuid());
    }

    @Override
    protected Page<Track> findAudioFiles(BrowseContext context, Pageable pageable) {
        var sort = Sorting.Tracks.BY_PLAYBACK_COUNT_DESC;
        var sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return mediaLibraryClient.tracksRecentlyPlayed(sortedPageable);
    }

    @Override
    protected int calculateRequestedCount(BrowseContext context) {
        var maxDisplayedCount = playqdProperties.getUpnp().getAction().getBrowse().getMaxDisplayedMostPlayed();
        return maxDisplayedCount > 0 ? maxDisplayedCount : super.calculateRequestedCount(context);
    }

    @Override
    protected String getDcTitle(BrowseContext context, Track track) {
        return String.format("(%s) %s - %s",
            track.playback().count(), track.artist().name(),super.getDcTitle(context, track));
    }
}
