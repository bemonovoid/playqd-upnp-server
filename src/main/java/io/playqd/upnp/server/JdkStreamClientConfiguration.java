package io.playqd.upnp.server;

import org.jupnp.transport.spi.AbstractStreamClientConfiguration;

import java.util.concurrent.ExecutorService;

public class JdkStreamClientConfiguration extends AbstractStreamClientConfiguration {

    public JdkStreamClientConfiguration(ExecutorService requestExecutorService) {
        super(requestExecutorService);
    }
}
