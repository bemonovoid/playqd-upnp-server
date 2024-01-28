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
import io.playqd.mediaserver.client.model.MediaItemsCount;
import io.playqd.mediaserver.config.properties.PlayqdProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class PlayqdMetadataClient implements MetadataClient {

  private final String ARTISTS_API = "/api/v1/metadata/artists";
  private final String ALBUMS_API = "/api/v1/metadata/albums";
  private final String GENRES_API = "/api/v1/metadata/genres";
  private final String TRACKS_API = "/api/v1/metadata/tracks";

  private final String COUNTS_API = "/metadata/counts/";

  private final RestTemplate restTemplate;
  private final PlayqdProperties playqdProperties;

  public PlayqdMetadataClient(PlayqdProperties playqdProperties) {
    this.playqdProperties = playqdProperties;
    this.restTemplate = new RestTemplateBuilder()
        .rootUri(playqdProperties.getMetadataServerBaseUrl() + "/api/v1")
        .build();
  }

  @Override
  public Page<Artist> getArtists(ArtistsQueryParams params, Pageable pageable) {
    var urlTemplate = UriComponentsBuilder.fromHttpUrl(playqdProperties.getMetadataServerBaseUrl() + ARTISTS_API)
        .queryParams(new LinkedMultiValueMap<>(pageableToQueryParams(pageable)))
        .queryParam("genreId", params.genreId())
        .encode()
        .toUriString();
    ResponseEntity<PageableResponse<Artist>> responseEntity = restTemplate.exchange(
        urlTemplate,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {}
    );
    return toPage(responseEntity);
  }

  @Override
  public Page<Album> getAlbums(AlbumsQueryParams params, Pageable pageable) {
    var urlTemplate = UriComponentsBuilder.fromHttpUrl(playqdProperties.getMetadataServerBaseUrl() + ALBUMS_API)
        .queryParam("artistId", params.artistId())
        .queryParam("genreId", params.genreId())
        .encode()
        .toUriString();

    ResponseEntity<PageableResponse<Album>> responseEntity = restTemplate.exchange(
        urlTemplate,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {}
    );

    return toPage(responseEntity);
  }

  @Override
  public Page<Genre> getGenres(Pageable pageable) {
    var urlTemplate = UriComponentsBuilder.fromHttpUrl(playqdProperties.getMetadataServerBaseUrl() + GENRES_API)
        .queryParams(new LinkedMultiValueMap<>(pageableToQueryParams(pageable)))
        .encode()
        .toUriString();

    ResponseEntity<PageableResponse<Genre>> responseEntity = restTemplate.exchange(
        urlTemplate,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {}
    );

    return toPage(responseEntity);
  }

  @Override
  public Page<Track> getTracksByArtistId(String artistId) {
    var urlTemplate = UriComponentsBuilder.fromHttpUrl(playqdProperties.getMetadataServerBaseUrl() + TRACKS_API)
        .queryParam("artistId", "{artistId}")
        .encode()
        .toUriString();

    ResponseEntity<PageableResponse<Track>> responseEntity = restTemplate.exchange(
        urlTemplate,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {},
        Map.of("artistId", artistId));

    return toPage(responseEntity);
  }

  @Override
  public Page<Track> getTracksByAlbumId(String albumId) {
    var urlTemplate = UriComponentsBuilder.fromHttpUrl(playqdProperties.getMetadataServerBaseUrl() + TRACKS_API)
        .queryParam("albumId", "{albumId}")
        .encode()
        .toUriString();

    ResponseEntity<PageableResponse<Track>> responseEntity = restTemplate.exchange(
        urlTemplate,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {},
        Map.of("albumId", albumId));

    return toPage(responseEntity);
  }

  @Override
  public Page<Track> getPlaylistTracks(String playlistId) {
    return null;
  }

  @Override
  public Page<Track> getPlayedTracks(Pageable pageable) {
    return null;
  }

  @Override
  public Page<Track> getRecentlyAdded(Pageable pageable) {
    return null;
  }

  @Override
  public List<MusicDirectory> getMusicDirectories() {
    var responseEntity = restTemplate.getForEntity("/directories", MusicDirectory[].class);
    return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
  }


  @Override
  public Page<Track> getTracksByLocationIn(Set<String> locations) {
    var urlTemplate = UriComponentsBuilder.fromHttpUrl(playqdProperties.getMetadataServerBaseUrl() + TRACKS_API)
        .queryParam("locations", "{locations}")
        .encode()
        .toUriString();

    var params = Map.of("locations", String.join(",", locations));

    ResponseEntity<PageableResponse<Track>> responseEntity = restTemplate.exchange(
        urlTemplate,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {},
        params);

    return toPage(responseEntity);
  }

  @Override
  public long getMediaItemsCount(MediaItemType type, MediaItemFilter filter) {
    ResponseEntity<MediaItemsCount> responseEntity = restTemplate.exchange(
        COUNTS_API + type.name(),
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {}
    );
    return responseEntity.getBody().count();
  }

  record PageableResponse<T>(List<T> content,
                             long numberOfElements,
                             int size,
                             int number,
                             long totalElements,
                             long totalPages) {
  }

  private static Map<String, List<String>> pageableToQueryParams(Pageable pageable) {
    return Map.of(
        "page", List.of(String.valueOf(pageable.getPageNumber())),
        "size", List.of(String.valueOf(pageable.getPageSize())));
  }

  private static <T> Page<T> toPage(ResponseEntity<PageableResponse<T>> responseEntity) {
    if (responseEntity.getBody() == null) {
      return Page.empty();
    }
    var pageable = responseEntity.getBody();
    return new PageImpl<>(
        pageable.content,
        PageRequest.of(pageable.number, pageable.size),
        pageable.totalElements());
  }
}
