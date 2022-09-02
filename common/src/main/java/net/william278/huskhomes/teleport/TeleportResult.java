package net.william278.huskhomes.teleport;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;

/**
 * Represents the result of a completed {@link Teleport}
 */
public enum TeleportResult {

    /**
     * Returns if the {@link Teleport} was completed locally, successfully
     */
    COMPLETED_LOCALLY(true),

    /**
     * Returns if the {@link Teleport} was completed cross-server
     */
    COMPLETED_CROSS_SERVER(true),

    /**
     * Returns if the {@link Teleport} failed because the user is already teleporting
     */
    FAILED_ALREADY_TELEPORTING(false),

    /**
     * Returns if the {@link TimedTeleport} warmup could not be started because they were moving
     */
    FAILED_MOVING(false),

    /**
     * Returns if the {@link Teleport} failed because the {@link World} of the
     * target {@link Position} could not be found
     */
    FAILED_INVALID_WORLD(false),

    /**
     * Returns if the {@link Teleport} failed because the server of the target
     * {@link Position} was invalid or not online
     */
    FAILED_INVALID_SERVER(false),

    /**
     * Returns if the {@link Teleport} failed because the coordinates of the
     * target {@link Position} were outside the world border limits
     */
    FAILED_ILLEGAL_COORDINATES(false),

    /**
     * Returns if the teleport was cancelled, for example if a {@link TimedTeleport} in progress is cancelled
     */
    CANCELLED(false);

    /**
     * Is {@code true} if the teleport was a success
     */
    public final boolean successful;

    TeleportResult(boolean successful) {
        this.successful = successful;
    }

}
