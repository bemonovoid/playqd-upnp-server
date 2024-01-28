package io.playqd.mediaserver.client;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.Genre;
import io.playqd.commons.data.MusicDirectory;
import io.playqd.commons.data.Track;
import io.playqd.mediaserver.client.model.AlbumsQueryParams;
import io.playqd.mediaserver.client.model.ArtistsQueryParams;
import io.playqd.mediaserver.client.model.MediaItemFilter;
import io.playqd.mediaserver.client.model.MediaItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface MetadataClient {

  List<MusicDirectory> getMusicDirectories();

  Page<Artist> getArtists(ArtistsQueryParams params, Pageable pageable);

  Page<Album> getAlbums(AlbumsQueryParams params, Pageable pageable);

  Page<Genre> getGenres(Pageable pageable);

  Page<Track> getTracksByArtistId(String artistId);

  Page<Track> getTracksByAlbumId(String albumId);

  Page<Track> getPlayedTracks(Pageable pageable);

  Page<Track> getRecentlyAdded(Pageable pageable);

  Page<Track> getPlaylistTracks(String playlistId);

  Page<Track> getTracksByLocationIn(Set<String> locations);

  long getMediaItemsCount(MediaItemType type, MediaItemFilter filter);
}
