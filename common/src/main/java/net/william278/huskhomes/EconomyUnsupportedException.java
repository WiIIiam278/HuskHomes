package net.william278.huskhomes;

/**
 * Indicates that economy features of the plugin have tried to be accessed when they are not enabled.
 */
public class EconomyUnsupportedException extends HuskHomesException {

    public EconomyUnsupportedException(String message) {
        super(message);
    }

}
