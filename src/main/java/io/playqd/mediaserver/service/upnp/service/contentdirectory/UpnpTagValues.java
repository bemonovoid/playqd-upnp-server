package io.playqd.mediaserver.service.upnp.service.contentdirectory;

import io.playqd.commons.data.Track;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@Getter
public class UpnpTagValues {

    private final String artist;
    private final String album;
    private final String genre;
    private final String author;
    private final String producer;
    private final String originalTrackNumber;
    private final UpnpClass upnpClass;
    private final String albumArtURI;
    private final int playbackCount;
    private final String lastPlaybackTime;

    public static Optional<String> formatLastPlaybackTime(Track track) {
        return Optional.ofNullable(track.playback().lastPlayedDate())
            .map(LocalDateTime::toString)
            .map(playbackTime -> playbackTime.replaceFirst("T", "'T'"));
    }
}
