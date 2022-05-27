package net.william278.huskhomes.util;

/**
 * Signals that economy features of the plugin have tried to be accessed when they are not enabled.
 */
public class EconomyUnsupportedException extends IllegalStateException {

    public EconomyUnsupportedException(String message) {
        super(message);
    }

}
