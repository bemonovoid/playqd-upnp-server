package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.Track;
import io.playqd.mediaserver.config.properties.PlayqdProperties;
import io.playqd.mediaserver.model.BrowsableObject;
import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowsableObjectValidations;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.DcTagValues;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ResTag;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpTagValues;
import io.playqd.mediaserver.util.DlnaUtils;
import io.playqd.mediaserver.util.UpnpResourceUriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jupnp.support.model.ProtocolInfo;
import org.jupnp.util.MimeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
abstract class AbstractTracksFinder extends BrowsableObjectBuilder implements ObjectBrowser {

    protected final MediaLibraryClient mediaLibraryClient;
    protected final PlayqdProperties playqdProperties;

    AbstractTracksFinder(PlayqdProperties playqdProperties, MediaLibraryClient mediaLibraryClient) {
        this.mediaLibraryClient = mediaLibraryClient;
        this.playqdProperties = playqdProperties;
    }

    @Override
    public final BrowseResult browse(BrowseContext context) {
        var browseRequest = context.getRequest();
        var requestedStartIdx = browseRequest.getStartingIndex();
        if (requestedStartIdx > 0) {
            return findInRange(context);
        }
        var requestedCount = calculateRequestedCount(context);
        log.info("Requesting first {} audio files from starting index: 0", requestedCount);
        var audioFiles = findAudioFiles(context, Pageable.ofSize(requestedCount));
        var result = audioFiles.stream()
                .map(audioFile -> buildItemObject(context, audioFile))
                .toList();
        return new BrowseResult(audioFiles.getTotalElements(), result.size(), result);
    }

    protected abstract String buildItemObjectId(Track track);

    protected abstract Page<Track> findAudioFiles(BrowseContext context, Pageable pageable);

    protected int calculateRequestedCount(BrowseContext context) {
        return context.getRequest().getRequestedCount();
    }

    protected String getDcTitle(BrowseContext context, Track track) {
        var dcTitle = track.title();
        var validateXml = context.getHeader(BrowseContext.HEADER_INVALID_XML_CHAR_VALIDATION_ENABLED, false);
        if (validateXml) {
            dcTitle = getValidatedDcTitle(track);
        }
        return dcTitle;
    }

    private BrowseResult findInRange(BrowseContext context) {
//        long totalCount = countTotal(context);
//        var requestedStartIdx = context.getRequest().getStartingIndex();
//        if (requestedStartIdx > totalCount) {
//            log.warn("Requested starting index ({}) is greater then available tracks.", requestedStartIdx);
//            return BrowseResult.empty();
//        }
        var requestedCount = calculateRequestedCount(context);
        var requestedStartIdx = context.getRequest().getStartingIndex();

        log.info("Requesting first {} audio files from starting index: {}.", requestedCount, requestedStartIdx);

        var pageNumber = calculatePageNumber(requestedStartIdx, requestedCount);

        var audioFiles = findAudioFiles(context, PageRequest.of(pageNumber, requestedCount));

        var result = audioFiles.getContent().stream()
//                .skip(requestedStartIdx)
                .map(track -> buildItemObject(context, track))
//                .limit(requestedCount)
                .toList();
        return new BrowseResult(audioFiles.getTotalElements(), result.size(), result);
    }

    private int calculatePageNumber(int requestedStartIdx, int requestedCount) {
        return requestedStartIdx / requestedCount;
    }

    private BrowsableObject buildItemObject(BrowseContext context, Track track) {
        var objectId = buildItemObjectId(track);
        return BrowsableObjectImpl.builder()
                .objectId(objectId)
                .parentObjectId(context.getObjectId())
                .dc(buildDcTagValues(context, track))
                .upnp(buildUpnpTagValues(track))
                .resources(buildResources(track))
                .build();
    }

    private DcTagValues buildDcTagValues(BrowseContext context, Track track) {
        return DcTagValues.builder()
                .title(getDcTitle(context, track))
                .creator(track.artist().name())
                .contributor(track.artist().name())
                .build();
    }

    private String getValidatedDcTitle(Track track) {
        var possibleDcTitles = new LinkedList<Supplier<String>>();
        possibleDcTitles.add(track::title);
        possibleDcTitles.add(() -> track.fileAttributes().name());
        return BrowsableObjectValidations.getFirstValidValue(possibleDcTitles);
    }

    private UpnpTagValues buildUpnpTagValues(Track track) {
        return UpnpTagValues.builder()
                .artist(track.artist().name())
                .album(track.album().name())
                .author(track.artist().name())
                .producer(track.artist().name())
                .genre(track.album().genre())
                .originalTrackNumber(track.number())
                .upnpClass(UpnpClass.musicTrack)
                .playbackCount(track.playback().count())
                .lastPlaybackTime(UpnpTagValues.formatLastPlaybackTime(track).orElse(null))
                .albumArtURI(track.album().artwork() != null ? track.album().artwork().uri() : null)
                .build();
    }

    private List<ResTag> buildResources(Track track) {
        var audioFileResource = buildAudioFileResource(track);
        var albumArtResources = buildAlbumArtRes(track);
        var result = new ArrayList<ResTag>(albumArtResources.size() + 1);
        result.add(audioFileResource);
        result.addAll(albumArtResources);
        return result;
    }

    private ResTag buildAudioFileResource(Track track) {
        return ResTag.builder()
                .id(Long.toString(track.id()))
                .uri(UpnpResourceUriBuilder.forTrackAudioStreamObject(playqdProperties.getMetadataServerBaseUrl(), track.id()))
                .protocolInfo(new ProtocolInfo(MimeType.valueOf(track.audioFormat().mimeType())).toString())
                .bitsPerSample(Integer.toString(track.audioFormat().bitsPerSample()))
                .bitRate(track.audioFormat().bitRate())
                .sampleFrequency(track.audioFormat().sampleRate())
                .size(track.fileAttributes().size())
                .duration(DlnaUtils.formatLength(track.length().precise()))
                .build();
    }

    private List<ResTag> buildAlbumArtRes(Track track) {
        if (track.album().artwork() == null) {
            return Collections.emptyList();
        }
        var artwork = track.album().artwork();
        try {
            var mimeType = MimeType.valueOf(artwork.mimeType());
            return List.of(buildResTag(artwork));
        } catch (IllegalArgumentException e) {
            log.error("Unexpected error when reading the mime type of album art with id: {}", artwork.albumId(), e);
            return Collections.emptyList();
        }
    }

    private static ResTag buildResTag(Track.Artwork artwork) {
        return ResTag.builder()
                .id(artwork.albumId())
                .uri(artwork.uri())
                .protocolInfo(DlnaUtils.buildImageProtocolInfo(MimeType.valueOf(artwork.mimeType())))
                .size(String.valueOf(artwork.size()))
                .image(true)
                .build();
    }

}
