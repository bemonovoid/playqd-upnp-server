package io.playqd.upnp.client;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.ArtworkSize;
import io.playqd.upnp.util.MediaLibraryResourceUriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
class DiscoveryClientMediaLibraryResourceUriBuilder implements MediaLibraryResourceUriBuilder {

  private final DiscoveryClient discoveryClient;

  DiscoveryClientMediaLibraryResourceUriBuilder(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @Override
  public String getAudioStreamResourceForTrackId(String trackId) {
    return discoveryClient.getInstances(MediaLibraryClient.CLIENT_NAME).stream()
        .findAny()
        .map(ServiceInstance::getUri)
        .map(baseUri -> String.format("%s%s/%s", baseUri, MediaLibraryClient.API_PATH_AUDIO_STREAM, trackId))
        .orElseGet(() -> {
          log.error("Unable to build audio stream resource uri for track id: {}. " +
              "{} service instance info wasn't found.", trackId, MediaLibraryClient.CLIENT_NAME);
          return "";
        });
  }

  @Override
  public String getImageBinaryResourceForLocation(String location) {
    var locationBase64Encoded = Base64.getEncoder().encodeToString(location.getBytes(StandardCharsets.UTF_8));
    return discoveryClient.getInstances(MediaLibraryClient.CLIENT_NAME).stream()
        .findAny()
        .map(ServiceInstance::getUri)
        .map(baseUri ->
            String.format("%s%s/%s", baseUri, MediaLibraryClient.API_PATH_IMAGE_STREAM, locationBase64Encoded))
        .orElseGet(() -> {
          log.error("Unable to build audio stream resource uri for track id: {}. " +
              "{} service instance info wasn't found.", location, MediaLibraryClient.CLIENT_NAME);
          return "";
        });
  }

  @Override
  public String getImageBinaryResourceForAlbum(String albumId, ArtworkSize artworkSize) {
    var albumImageBinaryPath = String.format("%s/albums/%s?size=%s",
        MediaLibraryClient.API_PATH_IMAGE_STREAM, albumId, artworkSize.name());
    return discoveryClient.getInstances(MediaLibraryClient.CLIENT_NAME).stream()
        .findAny()
        .map(ServiceInstance::getUri)
        .map(baseUri ->
            String.format("%s%s", baseUri, albumImageBinaryPath))
        .orElseGet(() -> {
          log.error("Unable to build audio stream resource uri for album id: {}. " +
              "{} service instance info wasn't found.", albumId, MediaLibraryClient.CLIENT_NAME);
          return null;
        });
  }
}