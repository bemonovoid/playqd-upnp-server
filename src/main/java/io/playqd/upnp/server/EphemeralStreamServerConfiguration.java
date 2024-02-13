package io.playqd.upnp.server;

import io.playqd.upnp.config.properties.UpnpStreamServerProperties;
import org.jupnp.transport.spi.StreamServerConfiguration;

public class EphemeralStreamServerConfiguration implements StreamServerConfiguration {

    private final int port;

    public EphemeralStreamServerConfiguration(UpnpStreamServerProperties properties) {
        this.port = properties.getPort();
    }

    @Override
    public int getListenPort() {
        return port;
    }
}
