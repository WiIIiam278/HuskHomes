package net.william278.huskhomes.teleport;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;

/**
 * Represents the result of a completed teleport
 */
public enum TeleportResult {

    /**
     * Returns if the teleport completed successfully
     */
    SUCCESS(true),

    /**
     * Returns if the teleport failed because the {@link World} of the
     * target {@link Position} could not be found
     * <p>
     */
    FAILED_INVALID_WORLD(false),

    /**
     * Returns if the teleport failed because the server of the target
     * {@link Position} was invalid or not online
     * <p>
     */
    FAILED_INVALID_SERVER(false),

    /**
     * Returns if the teleport failed because the coordinates of the
     * target {@link Position} were outside the world border limits
     * <p>
     */
    FAILED_INVALID_COORDINATES(false);

    /**
     * Is {@code true} if the teleport was a success
     */
    public final boolean successful;

    TeleportResult(boolean successful) {
        this.successful = successful;
    }

}
