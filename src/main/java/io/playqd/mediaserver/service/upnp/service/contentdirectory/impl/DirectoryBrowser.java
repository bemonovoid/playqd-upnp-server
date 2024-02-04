package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.Track;
import io.playqd.commons.utils.Tuple;
import io.playqd.mediaserver.api.soap.data.Browse;
import io.playqd.mediaserver.config.properties.PlayqdProperties;
import io.playqd.mediaserver.exception.PlayqdException;
import io.playqd.mediaserver.model.BrowsableObject;
import io.playqd.mediaserver.persistence.BrowsableObjectDao;
import io.playqd.mediaserver.persistence.jpa.dao.BrowsableObjectSetter;
import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.persistence.jpa.dao.PersistedBrowsableObject;
import io.playqd.mediaserver.service.upnp.service.UpnpActionHandlerException;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.DcTagValues;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ResTag;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpTagValues;
import io.playqd.mediaserver.util.DlnaUtils;
import io.playqd.mediaserver.util.FileUtils;
import io.playqd.mediaserver.util.UpnpResourceUriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jupnp.model.types.ErrorCode;
import org.jupnp.support.model.ProtocolInfo;
import org.jupnp.util.MimeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
final class DirectoryBrowser extends AbstractDirectoryBrowser {

  private final String hostAddress;
  private final String metadataServerHostAddress;
  private final MediaLibraryClient mediaLibraryClient;

  DirectoryBrowser(PlayqdProperties playqdProperties,
                   BrowsableObjectDao browsableObjectDao,
                   MediaLibraryClient mediaLibraryClient) {
    super(browsableObjectDao);
    this.mediaLibraryClient = mediaLibraryClient;
    this.hostAddress = playqdProperties.buildHostAddress();
    this.metadataServerHostAddress = playqdProperties.getMetadataServerBaseUrl();
  }

  @Override
  public BrowseResult browse(BrowseContext context) {
    var parentObject = getParent(context);

    checkBrowsingValidContainerObject(parentObject);

    // Prepare children object if the parentObject has not been visited yet
    if (!browsableObjectDao.hasChildren(parentObject.id())) {
      int createdObjectsCount = createChildrenObjects(parentObject);
      log.info("Created {} children objects for parent object with id = {} and objectId = {})",
          createdObjectsCount, parentObject.id(), parentObject.objectId());
    } else {
      log.info("Children objects were previously created, proceeding to query children objects.");
    }
    return queryChildrenObjects(parentObject.id(), context.getRequest());
  }

  private int createChildrenObjects(PersistedBrowsableObject parent) {
    try (Stream<Path> dirs = Files.list(parent.location())) { //TODO check if exists
      var objectSetters = dirs
          .map(DirectoryBrowser::pathToUpnpClass)
          // UpnpClass.item is a generic object for a file we do not support and need to filter out for now
          .filter(tuple -> UpnpClass.item != tuple.right())
          .map(this::toBrowsableObjectSetter)
          .collect(Collectors.toList());
      browsableObjectDao.save(parent, objectSetters);
      return objectSetters.size();
    } catch (IOException e) {
      throw new PlayqdException(e);
    }
  }

  private BrowseResult queryChildrenObjects(long parentId, Browse browseRequest) {
    var startingIndex = browseRequest.getStartingIndex();
    var requestedCount = browseRequest.getRequestedCount();
    var totalCount = (int) browsableObjectDao.countChildren(parentId);

    if (startingIndex > totalCount) {
      return BrowseResult.empty();
    } else if (startingIndex > 0) {
      var pageRequest = PageRequest.of(0, totalCount + 1, Sort.by(Sort.Order.by("dcTitle")));
      var children = browsableObjectDao.getChildren(parentId, pageRequest);
      var childrenOffset = children.stream().skip(startingIndex).toList();

      var result = buildBrowsableObjects(browseRequest, childrenOffset.stream().limit(requestedCount).toList());

      return new BrowseResult(childrenOffset.size(), result.size(), result);
    } else {
      var pageRequest = PageRequest.of(0, requestedCount, Sort.by(Sort.Order.by("dcTitle")));
      var page = browsableObjectDao.getChildren(parentId, pageRequest);
      var result = buildBrowsableObjects(browseRequest, page.getContent());
      return new BrowseResult(page.getSize(), page.getSize(), result);
    }
  }

  private List<BrowsableObject> buildBrowsableObjects(Browse browseRequest, List<PersistedBrowsableObject> objects) {
    Map<UpnpClass, List<PersistedBrowsableObject>> upnpClassObjects =
        objects.stream().collect(Collectors.groupingBy(PersistedBrowsableObject::upnpClass));

    var containers = upnpClassObjects.getOrDefault(UpnpClass.storageFolder, Collections.emptyList())
        .stream()
        .map(obj -> buildContainerObject(browseRequest, obj))
        .toList();

    var audioTrackItems = buildAudioTrackObjectItems(
        browseRequest,
        upnpClassObjects.getOrDefault(UpnpClass.audioItem, Collections.emptyList()));

    var imageItems = upnpClassObjects.getOrDefault(UpnpClass.image, Collections.emptyList()).stream()
        .map(obj -> buildImageObjectItem(browseRequest, obj))
        .toList();

//        var textItems = upnpClassObjects.getOrDefault(UpnpClass.text, Collections.emptyList()).stream()
//                .map(obj -> buildImageObjectItem(browseRequest, obj))
//                .toList();

    var result = new ArrayList<BrowsableObject>(containers.size() + audioTrackItems.size() + imageItems.size());

    result.addAll(containers);
    result.addAll(audioTrackItems);
    result.addAll(imageItems);

    return result;
  }

  private List<BrowsableObject> buildAudioTrackObjectItems(Browse browseRequest,
                                                           List<PersistedBrowsableObject> persistedObjects) {
    if (persistedObjects.isEmpty()) {
      return Collections.emptyList();
    }
    var locationToObjectMap = persistedObjects.stream()
        .collect(Collectors.toMap(obj -> obj.location().toString(), obj -> obj));
    return mediaLibraryClient.tracksByLocationIn(Pageable.unpaged(), locationToObjectMap.keySet()).stream()
        .filter(track -> locationToObjectMap.containsKey(track.fileAttributes().location()))
        .map(track -> buildAudioTrackObjectItem(
            browseRequest,
            track,
            locationToObjectMap.get(track.fileAttributes().location())))
        .toList();
  }

  private Consumer<BrowsableObjectSetter> toBrowsableObjectSetter(Tuple<Path, UpnpClass> tuple) {
    var counts = tuple.right().isContainer() ? countChildren(tuple.left()) : new NestedObjectsCount(0, 0);
    return setter -> {
      setter.setDcTitle(resolveDcTitle(tuple.left(), tuple.right()));
      setter.setLocation(tuple.left().toString());
      setter.setUpnpClass(tuple.right());
      setter.setChildCount(counts.totalCount());
      setter.setChildContainerCount(counts.childContainerCount());
    };
  }

  private PersistedBrowsableObject getParent(BrowseContext context) {
    return browsableObjectDao.getOneByObjectId(context.getObjectId())
        .orElseThrow(() -> {
          log.error("Browsable object with objectId '{}' was not found.", context.getObjectId());
          return new UpnpActionHandlerException(ErrorCode.ARGUMENT_VALUE_INVALID);
        });
  }

  private static Tuple<Path, UpnpClass> pathToUpnpClass(Path path) {
    if (Files.isDirectory(path)) {
      return Tuple.from(path, UpnpClass.storageFolder);
    }
    var fileExtension = FileUtils.getFileExtension(path.toString());
    if (isSupportedAudioFile(fileExtension)) {
      return Tuple.from(path, UpnpClass.audioItem);
    }
    if (isSupportedImageFile(fileExtension)) {
      return Tuple.from(path, UpnpClass.image);
    }
//        if (SupportedTextFiles.isSupportedTextFile(fileExtension)) {
//            return Tuple.from(path, UpnpClass.text);
//        }
    return Tuple.from(path, UpnpClass.item);
  }

  private static void checkBrowsingValidContainerObject(PersistedBrowsableObject browsableObject) {
    if (browsableObject.upnpClass().isItem()) {
      // This shouldn't really happen, because the renderers must not browse a file, unless the object type was
      // mistakenly set to container type
      log.error("Browsing {} file {} is illegal. ", browsableObject.upnpClass(), browsableObject.location());
      throw new UpnpActionHandlerException(ErrorCode.ARGUMENT_VALUE_INVALID);
    }
    if (!Files.exists(browsableObject.location())) {
      log.error("Browsable object (id={}, objectId={}) does not exist at location: {}",
          browsableObject.id(), browsableObject.objectId(), browsableObject.location());
      throw new UpnpActionHandlerException(ErrorCode.ARGUMENT_VALUE_INVALID);
    }
  }

  private static String resolveDcTitle(Path path, UpnpClass upnpClass) {
    var fileName = path.getFileName().toString();
    if (UpnpClass.audioItem == upnpClass) {
      return FileUtils.getFileNameWithoutExtension(fileName);
    }
    return fileName;
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
                                                    Track track,
                                                    PersistedBrowsableObject object) {
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
                .uri(UpnpResourceUriBuilder.forTrackAudioStreamObject(metadataServerHostAddress, track.id()))
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
    var path = object.location();
    return BrowsableObjectImpl.builder()
        .objectId(object.objectId())
        .parentObjectId(browseRequest.getObjectID())
        .dc(DcTagValues.builder().title(object.dcTitle()).build())
        .upnp(UpnpTagValues.builder().upnpClass(object.upnpClass()).build())
        .resources(List.of(
            ResTag.builder()
                .id(Long.toString(object.id()))
                .uri(UpnpResourceUriBuilder.forImageObject(hostAddress, object.objectId()))
                .protocolInfo(
                    DlnaUtils.buildImageProtocolInfo(
                        MimeType.valueOf(FileUtils.detectMimeType(path))))
                .size(String.valueOf(FileUtils.getFileSize(path)))
                .image(true)
                .build()))
        .build();
  }
}