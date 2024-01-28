package io.playqd.mediaserver.service.upnp.service.contentdirectory.impl;

import io.playqd.mediaserver.exception.PlayqdException;
import io.playqd.mediaserver.persistence.BrowsableObjectDao;
import io.playqd.mediaserver.service.upnp.service.contentdirectory.ObjectBrowser;
import io.playqd.mediaserver.util.FileUtils;
import org.jaudiotagger.audio.SupportedFileFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractDirectoryBrowser extends BrowsableObjectBuilder implements ObjectBrowser {

    private static final Set<String> SUPPORTED_AUDIO_EXTENSIONS = Arrays.stream(SupportedFileFormat.values())
        .map(SupportedFileFormat::getFilesuffix)
        .collect(Collectors.toSet());

    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    protected final BrowsableObjectDao browsableObjectDao;

    AbstractDirectoryBrowser(BrowsableObjectDao browsableObjectDao) {
        this.browsableObjectDao = browsableObjectDao;
    }

    protected NestedObjectsCount countChildren(Path path) {
        try (Stream<Path> pathStream = Files.list(path)) {
            var mediaItems = pathStream.collect(Collectors.groupingBy(Files::isDirectory));
            var containerCount = (long) mediaItems.getOrDefault(true, Collections.emptyList()).size();
            var itemsCount = mediaItems.getOrDefault(false, Collections.emptyList()).stream()
                .filter(AbstractDirectoryBrowser::isSupportMediaItem)
                .count();
            return new NestedObjectsCount(containerCount, itemsCount);
        } catch (IOException e) {
            throw new PlayqdException(String.format("Failed counting media source children at %s", path), e);
        }
    }

    private static boolean isSupportMediaItem(Path path) {
        var fileExtension = FileUtils.getFileExtension(path);
        return isSupportedAudioFile(fileExtension) || isSupportedImageFile(fileExtension);
    }

    protected record NestedObjectsCount(long childContainerCount, long childItemsCount) {

        long totalCount() {
            return childContainerCount() + childItemsCount();
        }
    }

    protected static boolean isSupportedAudioFile(String fileExtension) {
        return SUPPORTED_AUDIO_EXTENSIONS.contains(fileExtension);
    }

    protected static boolean isSupportedImageFile(String fileExtension) {
        return SUPPORTED_IMAGE_EXTENSIONS.contains(fileExtension);
    }

}