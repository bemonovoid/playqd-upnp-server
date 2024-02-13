package io.playqd.upnp.commons;

import io.playqd.upnp.config.ApplicationConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({ ApplicationConfiguration.class})
public class TextContextConfig {

}
