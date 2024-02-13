package io.playqd.upnp.config.properties;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter(AccessLevel.PACKAGE)
@Validated
public class UpnpServiceProperties {

    private UpnpActionProperties action = new UpnpActionProperties();

    @NotNull
    private UpnpStreamServerProperties streamServer;

}
