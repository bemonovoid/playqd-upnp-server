package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.DirectoryItem;
import io.playqd.mediaserver.api.soap.data.Browse;
import io.playqd.mediaserver.model.BrowsableObject;
import io.playqd.mediaserver.persistence.BrowsableObjectDao;
import io.playqd.mediaserver.persistence.jpa.dao.BrowsableObjectSetter;
import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.persistence.jpa.dao.PersistedBrowsableObject;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.DcTagValues;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpTagValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
final class MusicDirectoriesFinder extends BrowsableObjectBuilder implements ObjectBrowser {

  private final BrowsableObjectDao browsableObjectDao;
  private final MediaLibraryClient mediaLibraryClient;

  MusicDirectoriesFinder(MediaLibraryClient mediaLibraryClient, BrowsableObjectDao browsableObjectDao) {
    this.browsableObjectDao = browsableObjectDao;
    this.mediaLibraryClient = mediaLibraryClient;
  }

  @Override
  public BrowseResult browse(BrowseContext context) {

    log.info("Searching available media source(s) ...");

    List<PersistedBrowsableObject> objects = new ArrayList<>(browsableObjectDao.getRoots());

    if (objects.isEmpty()) {

      Page<DirectoryItem> musicDirectories = mediaLibraryClient.tree(Pageable.unpaged());

      objects = musicDirectories.stream()
          .map(this::toPersistedObjectSetter)
          .map(browsableObjectDao::save)
          .toList();
    }

    log.info("Retrieved {} media source(s)", objects.size());

    var result = objects.stream().map(source -> fromPersistedObject(context.getRequest(), source)).toList();

    return new BrowseResult(objects.size(), objects.size(), result);
  }

  private Consumer<BrowsableObjectSetter> toPersistedObjectSetter(DirectoryItem directoryItem) {
    return setter -> {
      setter.setDcTitle(directoryItem.name());
      setter.setLocation(String.valueOf(directoryItem.sourceDirId()));
      setter.setUpnpClass(UpnpClass.fromDirectoryItemType(directoryItem.itemType()));
      setter.setMimeType(directoryItem.mimeType());
      setter.setSize(directoryItem.fileSize());
      setter.setChildCount(directoryItem.totalChildItemsCount());
      setter.setChildContainerCount(directoryItem.childFoldersCount());
    };
  }

  private static BrowsableObject fromPersistedObject(Browse browseRequest, PersistedBrowsableObject persistedObject) {
    return BrowsableObjectImpl.builder()
        .objectId(persistedObject.objectId())
        .parentObjectId(browseRequest.getObjectID())
        .childCount(persistedObject.childCount().get())
        .childContainerCount(persistedObject.childContainerCount())
        .dc(buildDcTagValues(persistedObject))
        .upnp(buildUpnpTagValues())
        .searchable(true)
        .build();
  }

  private static DcTagValues buildDcTagValues(PersistedBrowsableObject source) {
    return DcTagValues.builder().title(source.dcTitle()).build();
  }

  private static UpnpTagValues buildUpnpTagValues() {
    return UpnpTagValues.builder().upnpClass(UpnpClass.storageFolder).build();
  }
}