package io.playqd.upnp.exception;

public class DatabaseEntityNotFoundException extends PlayqdException {

    public DatabaseEntityNotFoundException(String message) {
        super(message);
    }

    public DatabaseEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
