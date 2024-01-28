package io.playqd.mediaserver.commons;

import io.playqd.mediaserver.config.ApplicationConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({ ApplicationConfiguration.class})
public class TextContextConfig {

}
