package io.playqd.upnp.util;

import org.jupnp.util.MimeType;

import java.util.Formatter;
import java.util.Locale;

public final class DlnaUtils {

  private static final String DLNA_DURATION_FORMAT = "%01d:%02d:%06.3f";

  public static String buildImageProtocolInfo(MimeType mimeType) {
    return String.format("http-get:*:%s:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_FLAGS=00900000000000000000000000000000",
        mimeType.toStringNoParameters());
  }

  public static String formatLength(double duration) {
    double seconds;
    int hours;
    int minutes;
    if (duration < 0) {
      seconds = 0.0;
      hours = 0;
      minutes = 0;
    } else {
      seconds = duration % 60;
      hours = (int) (duration / 3600);
      minutes = ((int) (duration / 60)) % 60;
    }
    if (hours > 99999) {
      // As per DLNA standard
      hours = 99999;
    }
    StringBuilder sb = new StringBuilder();
    try (Formatter formatter = new Formatter(sb, Locale.ROOT)) {
      formatter.format(DLNA_DURATION_FORMAT, hours, minutes, seconds);
    }
    return sb.toString();
  }

  private DlnaUtils() {

  }
}
