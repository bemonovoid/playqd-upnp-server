package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseActionDelegate;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectIdAntMatchers;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectIdMatcherResult;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectIdPattern;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.SystemContainerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class BrowseActionDelegateImpl implements BrowseActionDelegate {

    private final BeanFactory beanFactory;

    BrowseActionDelegateImpl(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public BrowseResult browseRoots(BrowseContext browseContext) {
        return getResponse(RootContainersFinder.class, browseContext);
    }

    @Override
    public BrowseResult browseChildren(BrowseContext browseContext) {
        if (SystemContainerName.ROOT_FOLDERS.getObjectId().equals(browseContext.getObjectId())) {
            return browseMediaSources(browseContext);
        } else if (SystemContainerName.ROOT_MUSIC_LIBRARY.getObjectId().equals(browseContext.getObjectId())) {
            return browseMusicLibraryContainers(browseContext);
        } else if (SystemContainerName.isMusicLibraryChildObjectId(browseContext.getObjectId())) {
            return browseMusicLibraryChildContainer(browseContext);
        } else if (browseContext.getObjectId().contains("/")) {
            var matcherResult = ObjectIdAntMatchers.match(browseContext.getObjectId());
            if (matcherResult.isMatched()) {
                return browseAntMatchingContainers(browseContext, matcherResult);
            }
            log.warn("Unable to browse request object id was potentially of expected Playqd ObjecId pattern");
        }
        return browseMediaSourceContent(browseContext);
    }

    private BrowseResult browseMediaSources(BrowseContext context) {
        return getResponse(MusicDirectoriesBrowser.class, context);
    }

    private BrowseResult browseMusicLibraryContainers(BrowseContext context) {
        return getResponse(MusicLibraryChildrenFinder.class, context);
    }

    private BrowseResult browseMusicLibraryChildContainer(BrowseContext context) {
        var systemContainerName = SystemContainerName.getFromObjectId(context.getObjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("%s is not supported browsable virtual container", context.getObjectId())));
        switch (systemContainerName) {
            case ARTIST_ALBUM -> {
                context.addHeader(BrowseContext.HEADER_OBJECT_ID_PATTERN, ObjectIdPattern.ARTIST_ALBUMS_PATH);
                return getResponse(ArtistsFinder.class, context);
            }
            case ARTIST_TRACK -> {
                context.addHeader(BrowseContext.HEADER_OBJECT_ID_PATTERN, ObjectIdPattern.ARTIST_TRACKS_PATH);
                return getResponse(ArtistsFinder.class, context);
            }
            case GENRE_ARTIST -> {
                context.addHeader(BrowseContext.HEADER_OBJECT_ID_PATTERN, ObjectIdPattern.GENRE_ARTISTS_PATH);
                return getResponse(GenresFinder.class, context);
            }
            case GENRE_ALBUM -> {
                context.addHeader(BrowseContext.HEADER_OBJECT_ID_PATTERN, ObjectIdPattern.GENRE_ALBUMS_PATH);
                return getResponse(GenresFinder.class, context);
            }
            case TRACKS_MOST_PLAYED -> {
                return getResponse(TracksMostPlayedFinder.class, context);
            }
            case TRACKS_RECENTLY_ADDED -> {
                return getResponse(TracksRecentlyAddedFinder.class, context); // TODO may be have a folder of 'Since {date}' or for example 3 recent dates/folders
            }
            case TRACKS_RECENTLY_PLAYED -> {
                return getResponse(TracksRecentlyPlayedFinder.class, context);
            }
            case PLAYLISTS -> {
                return getResponse(PlaylistFilesFinder.class, context);
            }
            default -> throw new IllegalStateException(String.format("%s music library query not yet supported.",
                    systemContainerName.getDcTitleName()));
        }
    }

    private BrowseResult browseAntMatchingContainers(BrowseContext context,
                                                     ObjectIdMatcherResult matcherResult) {
        var extractedTemplateVariables = matcherResult.getExtractedTemplateVariables();
        if (matcherResult.getPattern().getRequiredArgs() != extractedTemplateVariables.size()) {
            throw new IllegalStateException("Ant matcher is in invalid state.");
        }
        context.addHeaders(matcherResult.getExtractedTemplateVariables());
        switch (matcherResult.getPattern()) {
            case ARTIST_ALBUMS_PATH -> {
                return getResponse(AlbumsByArtistFinder.class, context);
            }
            case ARTIST_TRACKS_PATH -> {
                return getResponse(TracksByArtistFinder.class, context);
            }
            case ARTIST_ALBUM_TRACKS_PATH -> {
                return getResponse(TracksByArtistAlbumFinder.class, context);
            }
            case GENRE_ARTISTS_PATH -> {
                return getResponse(ArtistsByGenreFinder.class, context);
            }
            case GENRE_ALBUMS_PATH -> {
                return getResponse(AlbumsByGenreFinder.class, context);
            }
            case PLAYLIST_PATH -> {
                return getResponse(TracksByPlaylistFinder.class, context);
            }
            default -> {
                log.warn("Not yet implemented");
                return BrowseResult.empty();
            }
        }
    }

    private BrowseResult browseMediaSourceContent(BrowseContext context) {
        return getResponse(DirectoryBrowser.class, context);
    }

    private BrowseResult getResponse(Class<? extends ObjectBrowser> objectFinderClass, BrowseContext context) {
        return beanFactory.getBean(objectFinderClass).browse(context);
    }
}