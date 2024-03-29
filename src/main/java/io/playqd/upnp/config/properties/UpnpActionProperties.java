package io.playqd.upnp.config.properties;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter(AccessLevel.PACKAGE)
@Validated
public class UpnpActionProperties {

    private BrowseActionProperties browse = new BrowseActionProperties();

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @Validated
    public static class BrowseActionProperties {

        @PositiveOrZero
        private int maxDisplayedRecentlyAdded;

        @PositiveOrZero
        private int maxDisplayedRecentlyPlayed;

        @PositiveOrZero
        private int maxDisplayedMostPlayed;
    }
}
