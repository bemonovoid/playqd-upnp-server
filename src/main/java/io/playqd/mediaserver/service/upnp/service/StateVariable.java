package io.playqd.mediaserver.service.upnp.service;

import java.time.Instant;

public record StateVariable(StateVariableName name, String value, Instant lastModifiedDate) {
}
