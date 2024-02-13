package io.playqd.upnp.config.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter(AccessLevel.PACKAGE)
public class PlayqdProperties {

    private static final String LOGS_DIR_NAME = "logs";

    private PlayqdLoggingProperties logging;

    private UpnpServiceProperties upnp = new UpnpServiceProperties();
}