package io.playqd.mediaserver.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class TimeUtils {

    public static Instant millisToInstant(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    public static LocalDate millisToLocalDate(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private TimeUtils() {

    }
}
