package net.william278.huskhomes;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates an exception occurred while initialising the HuskHomes plugin
 */
public class HuskHomesInitializationException extends HuskHomesException {
    public HuskHomesInitializationException(@NotNull String message) {
        super(message);
    }
}
