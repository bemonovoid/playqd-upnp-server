package io.playqd.upnp.config.upnp;

import io.playqd.upnp.config.properties.PlayqdProperties;
import io.playqd.upnp.server.UpnpServiceContextHolder;
import io.playqd.upnp.service.StateVariableContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpnpServerContextConfiguration {

  @Bean
  UpnpServiceContextHolder upnpServiceContextHolder(PlayqdProperties playqdProperties,
                                                    StateVariableContextHolder stateVariableContextHolder) {
    return new UpnpServiceContextHolder(playqdProperties, stateVariableContextHolder);
  }
}
