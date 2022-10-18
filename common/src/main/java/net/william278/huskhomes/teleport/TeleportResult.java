package net.william278.huskhomes.teleport;

import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents the result of a completed {@link Teleport}
 */
public enum TeleportResult {

    /**
     * Returns if the {@link Teleport} was completed locally, successfully
     */
    COMPLETED_LOCALLY(true, "teleporting_complete"),

    /**
     * Returns if the {@link Teleport} was completed cross-server
     */
    COMPLETED_CROSS_SERVER(true, "teleporting_complete"),

    /**
     * Returns if the {@link Teleport} failed because the user is already teleporting
     */
    FAILED_ALREADY_TELEPORTING(false, "error_already_teleporting"),

    /**
     * Returns if the {@link TimedTeleport} warmup could not be started because they were moving
     */
    FAILED_MOVING(false, "error_teleport_warmup_stand_still"),

    /**
     * Returns if the {@link Teleport} failed because the {@link World} of the
     * target {@link Position} could not be found
     */
    FAILED_INVALID_WORLD(false, "error_invalid_world"),

    /**
     * Returns if the {@link Teleport} failed because the server of the target
     * {@link Position} was invalid or not online
     */
    FAILED_INVALID_SERVER(false, "error_invalid_server"),

    /**
     * Returns if the {@link Teleport} failed because the coordinates of the
     * target {@link Position} were outside the world border limits
     */
    FAILED_ILLEGAL_COORDINATES(false, "error_illegal_target_coordinates"),

    /**
     * Returns if the {@link Teleport} failed because the teleporter could not be resolved or was offline
     */
    FAILED_TELEPORTER_NOT_RESOLVED(false), //todo error message

    /**
     * Returns if the {@link Teleport} failed because the target {@link Position} could not be resolved
     */
    FAILED_TARGET_NOT_RESOLVED(false), //todo error message

    /**
     * Returns if the teleport was cancelled, for example if a {@link TimedTeleport} in progress is cancelled
     */
    CANCELLED(false),

    /**
     * Returns if the {@link Teleport} was cancelled due to failing economy checks
     */
    CANCELLED_ECONOMY(false);

    /**
     * Is {@code true} if the teleport was a success
     */
    public final boolean successful;

    @Nullable
    private final String messageId;

    TeleportResult(boolean successful, @NotNull String messageId) {
        this.successful = successful;
        this.messageId = messageId;
    }

    TeleportResult(boolean successful) {
        this.successful = successful;
        this.messageId = null;
    }

    /**
     * Send an {@link OnlineUser} the corresponding message for this {@link TeleportResult}, if there is one
     *
     * @param locales the {@link Locales} instance to use
     * @param user    the {@link OnlineUser} to send the message to
     * @since 3.1
     */
    public void sendMessage(@NotNull Locales locales, @NotNull OnlineUser user) {
        Optional.ofNullable(messageId).flatMap(locales::getLocale).ifPresent(user::sendMessage);
    }

}
