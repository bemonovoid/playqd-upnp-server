package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.DirectoryItem;
import io.playqd.commons.data.ItemType;
import io.playqd.commons.data.Track;
import io.playqd.mediaserver.api.soap.data.Browse;
import io.playqd.mediaserver.model.BrowsableObject;
import io.playqd.mediaserver.persistence.BrowsableObjectDao;
import io.playqd.mediaserver.persistence.jpa.dao.BrowsableObjectSetter;
import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.persistence.jpa.dao.PersistedBrowsableObject;
import io.playqd.mediaserver.service.upnp.service.UpnpActionHandlerException;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.DcTagValues;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ResTag;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpTagValues;
import io.playqd.mediaserver.util.DlnaUtils;
import io.playqd.mediaserver.util.MediaLibraryResourceUriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jupnp.model.types.ErrorCode;
import org.jupnp.support.model.ProtocolInfo;
import org.jupnp.util.MimeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
final class DirectoryFinder extends BrowsableObjectBuilder implements ObjectBrowser {

  private final BrowsableObjectDao browsableObjectDao;
  private final MediaLibraryClient mediaLibraryClient;
  private final MediaLibraryResourceUriBuilder mediaLibraryResourceUriBuilder;

  DirectoryFinder(BrowsableObjectDao browsableObjectDao,
                  MediaLibraryClient mediaLibraryClient,
                  MediaLibraryResourceUriBuilder mediaLibraryResourceUriBuilder) {
    this.browsableObjectDao = browsableObjectDao;
    this.mediaLibraryClient = mediaLibraryClient;
    this.mediaLibraryResourceUriBuilder = mediaLibraryResourceUriBuilder;
  }

  @Override
  public BrowseResult browse(BrowseContext context) {
    var parentObject = getParent(context);

    // Prepare children object if the parentObject has not been visited yet
    if (browsableObjectDao.hasChildren(parentObject.id())) {
      log.info("Children objects were previously created, proceeding to query children objects.");
    } else {
      int createdObjectsCount = createChildrenObjects(parentObject);
      log.info("Created {} children objects for parent object with id = {} and objectId = {})",
          createdObjectsCount, parentObject.id(), parentObject.objectId());
    }
    return queryChildrenObjects(parentObject, context.getRequest());
  }

  private int createChildrenObjects(PersistedBrowsableObject parent) {
    var directoryItems = (Page<DirectoryItem>) null;
    if (parent.isRoot()) {
      directoryItems = mediaLibraryClient.musicDirectoryTree(Long.parseLong(parent.location()));
    } else {
      var musicDirectoryId = getObjectSourceDirId(parent);
      var pathBase64Encoded = Base64.getEncoder().encodeToString(
          parent.location().getBytes(StandardCharsets.UTF_8));
      directoryItems = mediaLibraryClient.musicDirectoryTree(musicDirectoryId, pathBase64Encoded);
    }
    var objectSetters =  directoryItems.stream()
        // UpnpClass.item is a generic object for a file we do not support and need to filter out for now
        .filter(directoryItem -> ItemType.otherFile != directoryItem.itemType())
        .map(this::toBrowsableObjectSetter)
        .collect(Collectors.toList());
    browsableObjectDao.save(parent, objectSetters);
    return objectSetters.size();
  }

  private BrowseResult queryChildrenObjects(PersistedBrowsableObject parentObject, Browse browseRequest) {
    var parentId = parentObject.id();
    var startingIndex = browseRequest.getStartingIndex();
    var requestedCount = browseRequest.getRequestedCount();
    var totalCount = (int) browsableObjectDao.countChildren(parentId);

    if (startingIndex > totalCount) {
      return BrowseResult.empty();
    } else if (startingIndex > 0) {
      var window = browsableObjectDao.getChildren(parentId, ScrollPosition.offset(startingIndex));
      var result = buildBrowsableObjects(browseRequest, parentObject, window.getContent());
      return new BrowseResult(window.size(), result.size(), result);
    } else { // first page, startingIdx is 0
      var orders = List.of(
          new Sort.Order(Sort.Direction.DESC, "upnpClass"),
          new Sort.Order(Sort.Direction.ASC, "dcTitle").ignoreCase());
      var pageRequest = PageRequest.of(0, requestedCount, Sort.by(orders));
      var page = browsableObjectDao.getChildren(parentId, pageRequest);
      var result = buildBrowsableObjects(browseRequest, parentObject, page.getContent());
      return new BrowseResult(page.getTotalElements(), result.size(), result);
    }
  }

  private List<BrowsableObject> buildBrowsableObjects(Browse browseRequest,
                                                      PersistedBrowsableObject parentObject,
                                                      List<PersistedBrowsableObject> objects) {
    Map<UpnpClass, List<PersistedBrowsableObject>> upnpClassObjects =
        objects.stream().collect(Collectors.groupingBy(PersistedBrowsableObject::upnpClass));

    var containers = upnpClassObjects.getOrDefault(UpnpClass.storageFolder, Collections.emptyList())
        .stream()
        .map(obj -> buildContainerObject(browseRequest, obj))
        .toList();

    var audioItems = buildAudioTrackObjectItems(
        browseRequest,
        parentObject,
        upnpClassObjects.getOrDefault(UpnpClass.audioItem, Collections.emptyList()));

    var imageItems = upnpClassObjects.getOrDefault(UpnpClass.image, Collections.emptyList()).stream()
        .map(obj -> buildImageObjectItem(browseRequest, obj))
        .toList();

    var result = new ArrayList<BrowsableObject>(containers.size() + audioItems.size() + imageItems.size());

    result.addAll(containers);
    result.addAll(audioItems);
    result.addAll(imageItems);

    return result;
  }

  private List<BrowsableObject> buildAudioTrackObjectItems(Browse browseRequest,
                                                           PersistedBrowsableObject parentObject,
                                                           List<PersistedBrowsableObject> audioFilesPersistedObjects) {
    if (CollectionUtils.isEmpty(audioFilesPersistedObjects)) {
      return Collections.emptyList();
    }
    var musicDirectoryId = getObjectSourceDirId(parentObject);
    Page<Track> tracks;
    if (parentObject.isRoot()) {
      tracks = mediaLibraryClient.tracksByLocation(Pageable.unpaged(), musicDirectoryId, "");
    } else {
      tracks = mediaLibraryClient.tracksByLocation(
          Pageable.unpaged(), musicDirectoryId, parentObject.location());
    }
    var locationToObjectMap = audioFilesPersistedObjects.stream()
        .collect(Collectors.toMap(PersistedBrowsableObject::location, obj -> obj));
    return tracks.stream()
        .map(track -> buildAudioTrackObjectItem(
            browseRequest,
            locationToObjectMap.get(track.fileAttributes().location()),
            track))
        .toList();
  }

  private long getObjectSourceDirId(PersistedBrowsableObject browsableObject) {
    var rootObject = browsableObjectDao.getChildRoot(browsableObject);
    return Long.parseLong(rootObject.location());
  }

  private Consumer<BrowsableObjectSetter> toBrowsableObjectSetter(DirectoryItem directoryItem) {
    return setter -> {
      setter.setDcTitle(directoryItem.name());
      setter.setLocation(directoryItem.path());
      setter.setUpnpClass(UpnpClass.fromDirectoryItemType(directoryItem.itemType()));
      setter.setSize(directoryItem.fileSize());
      setter.setMimeType(directoryItem.mimeType());
      setter.setChildCount(directoryItem.totalChildItemsCount());
      setter.setChildContainerCount(directoryItem.childFoldersCount());
    };
  }

  private PersistedBrowsableObject getParent(BrowseContext context) {
    return browsableObjectDao.getOneByObjectId(context.getObjectId())
        .orElseThrow(() -> {
          log.error("Browsable object with objectId '{}' was not found.", context.getObjectId());
          return new UpnpActionHandlerException(ErrorCode.ARGUMENT_VALUE_INVALID);
        });
  }

  private static BrowsableObject buildContainerObject(Browse browseRequest, PersistedBrowsableObject object) {
    return BrowsableObjectImpl.builder()
        .objectId(object.objectId())
        .parentObjectId(browseRequest.getObjectID())
        .searchable(true)
        .childCount(object.childCount().get())
        .childContainerCount(object.childContainerCount())
        .dc(DcTagValues.builder().title(object.dcTitle()).build())
        .upnp(UpnpTagValues.builder().upnpClass(object.upnpClass()).build())
        .build();
  }

  private BrowsableObject buildAudioTrackObjectItem(Browse browseRequest,
                                                    PersistedBrowsableObject object,
                                                    Track track) {
    return BrowsableObjectImpl.builder()
        .objectId(object.objectId())
        .parentObjectId(browseRequest.getObjectID())
        .dc(DcTagValues.builder()
            .title(object.dcTitle())
            .creator(track.artist().name())
            .build())
        .upnp(UpnpTagValues.builder()
            .artist(track.artist().name())
            .album(track.album().name())
            .genre(track.album().genre())
            .originalTrackNumber(track.number())
            .upnpClass(object.upnpClass())
            .playbackCount(track.playback().count())
            .lastPlaybackTime(UpnpTagValues.formatLastPlaybackTime(track).orElse(null))
            .build())
        .resources(List.of(
            ResTag.builder()
                .id(Long.toString(track.id()))
                .uri(mediaLibraryResourceUriBuilder.getAudioStreamResourceForTrackId(track.uuid()))
                .protocolInfo(new ProtocolInfo(MimeType.valueOf(track.audioFormat().mimeType())).toString())
                .bitsPerSample(Integer.toString(track.audioFormat().bitsPerSample()))
                .bitRate(track.audioFormat().bitRate())
                .sampleFrequency(track.audioFormat().sampleRate())
                .size(track.fileAttributes().size())
                .duration(DlnaUtils.formatLength(track.length().precise()))
                .build()))
        .build();
  }

  private BrowsableObject buildImageObjectItem(Browse browseRequest, PersistedBrowsableObject object) {
    return BrowsableObjectImpl.builder()
        .objectId(object.objectId())
        .parentObjectId(browseRequest.getObjectID())
        .dc(DcTagValues.builder().title(object.dcTitle()).build())
        .upnp(UpnpTagValues.builder().upnpClass(object.upnpClass()).build())
        .resources(List.of(
            ResTag.builder()
                .id(Long.toString(object.id()))
                .uri(mediaLibraryResourceUriBuilder.getImageBinaryResourceForLocation(object.location()))
                .protocolInfo(DlnaUtils.buildImageProtocolInfo(MimeType.valueOf(object.mimeType())))
                .size(object.size() != null ? object.size().toString() : "")
                .image(true)
                .build()))
        .build();
  }
}