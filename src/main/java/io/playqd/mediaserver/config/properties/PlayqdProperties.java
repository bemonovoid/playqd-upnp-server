package io.playqd.mediaserver.config.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Validated
@Getter
@Setter(AccessLevel.PACKAGE)
public class PlayqdProperties {

    private static final String LOGS_DIR_NAME = "logs";

    private String workingDir;
    private String metadataServerBaseUrl;

    private PlayqdLoggingProperties logging;

    private UpnpServiceProperties upnp = new UpnpServiceProperties();

    public String buildHostAddress() {
        var streamServerProps = this.getUpnp().getStreamServer();
        return streamServerProps.getHost() + ":" + streamServerProps.getPort();
    }

    public Path getWorkingDirOrDefault() {
        return Optional.ofNullable(workingDir)
                .map(Paths::get)
                .orElseGet(() -> Paths.get(System.getProperty("user.home")));
    }

    public Path getLogsDir() {
        return getWorkingDirOrDefault().resolve(LOGS_DIR_NAME);
    }
}
