package io.playqd.mediaserver.client.model;

import lombok.Builder;

@Builder
public record AlbumsQueryParams(String artistId, String genreId) {
}
