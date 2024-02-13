package io.playqd.upnp.commons;

import io.playqd.upnp.config.ApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ApplicationConfiguration.class)
public abstract class AbstractSpringBootTest {

}
