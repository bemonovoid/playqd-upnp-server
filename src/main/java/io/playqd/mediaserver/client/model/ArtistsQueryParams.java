package io.playqd.mediaserver.client.model;

import lombok.Builder;

@Builder
public record ArtistsQueryParams(String genreId) {
}
