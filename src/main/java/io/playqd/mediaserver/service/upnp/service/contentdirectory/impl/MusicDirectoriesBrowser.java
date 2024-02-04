package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.commons.client.MediaLibraryClient;
import io.playqd.commons.data.MusicDirectory;
import io.playqd.mediaserver.api.soap.data.Browse;
import io.playqd.mediaserver.model.BrowsableObject;
import io.playqd.mediaserver.persistence.BrowsableObjectDao;
import io.playqd.mediaserver.persistence.jpa.dao.BrowsableObjectSetter;
import io.playqd.mediaserver.persistence.jpa.dao.BrowseResult;
import io.playqd.mediaserver.persistence.jpa.dao.PersistedBrowsableObject;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.BrowseContext;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.DcTagValues;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpClass;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.UpnpTagValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
final class MusicDirectoriesBrowser extends AbstractDirectoryBrowser {

    private final MediaLibraryClient mediaLibraryClient;

    MusicDirectoriesBrowser(MediaLibraryClient mediaLibraryClient, BrowsableObjectDao browsableObjectDao) {
        super(browsableObjectDao);
        this.mediaLibraryClient = mediaLibraryClient;
    }

    @Override
    public BrowseResult browse(BrowseContext context) {

        log.info("Searching available media source(s) ...");

        List<PersistedBrowsableObject> objects = new ArrayList<>(browsableObjectDao.getRoot());

        if (objects.isEmpty()) {

            var musicDirectories = mediaLibraryClient.musicDirectories();

            objects = musicDirectories.stream()
                    .filter(musicDirectory -> {
                        var exists = Files.exists(musicDirectory.path());
                        if (!exists) {
                            log.warn("Media source (id: {}) path does not exist: {}. Skipping this media source",
                                    musicDirectory.id(), musicDirectory.path());
                        }
                        return exists;
                    })
                    .map(this::toPersistedObjectSetter)
                    .map(browsableObjectDao::save)
                    .toList();
        }

        log.info("Retrieved {} media source(s)", objects.size());

        var result = objects.stream().map(source -> fromPersistedObject(context.getRequest(), source)).toList();

        return new BrowseResult(objects.size(), objects.size(), result);
    }

    private Consumer<BrowsableObjectSetter> toPersistedObjectSetter(MusicDirectory musicDirectory) {
        return toPersistedObjectSetter(musicDirectory.name(), musicDirectory.path());
    }

    private Consumer<BrowsableObjectSetter> toPersistedObjectSetter(String dcTitle, Path path) {
        return setter -> {
            var counts = countChildren(path);
            setter.setDcTitle(dcTitle);
            setter.setLocation(path.toString());
            setter.setUpnpClass(UpnpClass.storageFolder);
            setter.setChildCount(counts.totalCount());
            setter.setChildContainerCount(counts.childContainerCount());
        };
    }

    private static BrowsableObject fromPersistedObject(Browse browseRequest, PersistedBrowsableObject persistedObject) {
        return BrowsableObjectImpl.builder()
                .objectId(persistedObject.objectId())
                .parentObjectId(browseRequest.getObjectID())
                .childCount(persistedObject.childCount().get())
                .childContainerCount(persistedObject.childContainerCount())
                .dc(buildDcTagValues(persistedObject))
                .upnp(buildUpnpTagValues(persistedObject))
                .build();
    }

    private static DcTagValues buildDcTagValues(PersistedBrowsableObject source) {
        return DcTagValues.builder()
                .title(source.dcTitle())
                .build();
    }

    private static UpnpTagValues buildUpnpTagValues(PersistedBrowsableObject source) {
        return UpnpTagValues.builder().upnpClass(UpnpClass.storageFolder).build();
    }

}
