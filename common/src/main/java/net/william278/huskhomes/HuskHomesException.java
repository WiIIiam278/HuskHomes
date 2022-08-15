package net.william278.huskhomes;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates an exception caused by HuskHomes' plugin logic occurred
 */
public class HuskHomesException extends RuntimeException {

    public HuskHomesException(@NotNull String message) {
        super(message);
    }

    public HuskHomesException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

}
