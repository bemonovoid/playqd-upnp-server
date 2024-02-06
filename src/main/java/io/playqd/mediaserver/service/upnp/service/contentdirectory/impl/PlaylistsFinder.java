package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.Playlist;
import io.playqd.mediaserver.model.BrowsableObject;
import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.DcTagValues;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectIdPattern;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpTagValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class PlaylistsFinder implements ObjectBrowser {

  private final MediaLibraryClient mediaLibraryClient;

  PlaylistsFinder(MediaLibraryClient mediaLibraryClient) {
    this.mediaLibraryClient = mediaLibraryClient;
  }

  @Override
  public BrowseResult browse(BrowseContext context) {
      var result = mediaLibraryClient.getAllPlaylists().stream()
          .map(playlist -> buildContainerObject(context, playlist))
          .toList();
      return new BrowseResult(result.size(), result.size(), result);
  }

  private static BrowsableObject buildContainerObject(BrowseContext context, Playlist playlist) {
    var objectId = ObjectIdPattern.PLAYLIST_PATH.compile(playlist.id());
    return BrowsableObjectImpl.builder()
        .objectId(objectId)
        .parentObjectId(context.getObjectId())
        .searchable(true)
        .childCount(playlist.itemsCount())
        .dc(DcTagValues.builder().title(playlist.title()).build())
        .upnp(buildUpnpTagValues(playlist))
        .build();
  }

  private static UpnpTagValues buildUpnpTagValues(Playlist playlist) {
    return UpnpTagValues.builder().upnpClass(UpnpClass.playlistContainer).build();
  }
}