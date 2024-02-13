package io.playqd.upnp.util;

import io.playqd.commons.data.ArtworkSize;

public interface MediaLibraryResourceUriBuilder {

  String getAudioStreamResourceForTrackId(String trackId);

  String getImageBinaryResourceForLocation(String locationBase64Encoded);

  String getImageBinaryResourceForAlbum(String albumId, ArtworkSize artworkSize);
}
