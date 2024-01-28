package io.playqd.mediaserver.util;

import io.playqd.mediaserver.api.rest.controller.RestApiResources;

public class UpnpResourceUriBuilder {

  public static String forImageObject(String hostname, String albumId) {
    return String.format("http://%s%s/%s", hostname, RestApiResources.BROWSABLE_OBJECT_IMAGE, albumId);
  }

  public static String forTrackAudioStreamObject(String metadataServerHostname, long trackId) {
    return String.format("http://%s%s/%s", metadataServerHostname, RestApiResources.AUDIO_STREAM, trackId);
  }
}
