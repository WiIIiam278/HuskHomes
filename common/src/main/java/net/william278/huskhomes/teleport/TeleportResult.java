package net.william278.huskhomes.teleport;

import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents the result of executing a {@link Teleport} or {@link TimedTeleport}
 *
 * @since 3.1
 */
public class TeleportResult {

    /**
     * The state of the {@link Teleport}
     */
    @NotNull
    private final ResultState state;

    /**
     * The {@link User} who is doing the teleporting
     */
    @Nullable
    private final User teleporter;

    /**
     * The {@link Position} the teleporter should be teleported to
     */
    @Nullable
    private final Position destination;

    /**
     * <b>Internal</b> - Create a new {@link TeleportResult} (use {@link #from(ResultState, Teleport)})
     *
     * @param state       The {@link ResultState} of completing the teleport
     * @param teleporter  The {@link User} who is doing the teleporting
     * @param destination The {@link Position} the teleporter should be teleported to
     * @since 3.1
     */
    private TeleportResult(@NotNull ResultState state, @Nullable User teleporter, @Nullable Position destination) {
        this.state = state;
        this.teleporter = teleporter;
        this.destination = destination;
    }

    /**
     * Create a new {@link TeleportResult} for a {@link Teleport} with an outcome {@link ResultState}
     *
     * @param state    The outcome {@link ResultState}
     * @param teleport The {@link Teleport} that was executed
     * @return The {@link TeleportResult} of executing the supplied {@link Teleport}
     * @since 3.1
     */
    public static TeleportResult from(@NotNull ResultState state, @NotNull Teleport teleport) {
        return new TeleportResult(state, teleport.teleporter, teleport.target);
    }

    /**
     * Get the state of the {@link Teleport} after execution has finished
     *
     * @return The {@link ResultState} of the teleport
     * @since 3.1
     */
    @NotNull
    public ResultState getState() {
        return state;
    }

    /**
     * Returns if the teleport was successful
     *
     * @return {@code true} if the teleport was successful, {@code false} otherwise
     * @since 3.1
     */
    public boolean successful() {
        return state.successful;
    }

    /**
     * Get the {@link User} who is doing the teleporting
     *
     * @return The teleporter, if they were resolved
     * @implNote The optional will return empty if the state is {@link ResultState#FAILED_TELEPORTER_NOT_RESOLVED}
     * @since 3.1
     */
    public Optional<User> getTeleporter() {
        return Optional.ofNullable(teleporter);
    }

    /**
     * Get the target {@link Position} the teleporter should have been teleported to
     *
     * @return The target position, if it was resolved
     * @implNote The optional will return empty if the state is {@link ResultState#FAILED_TARGET_NOT_RESOLVED}
     * @since 3.1
     */
    public Optional<Position> getDestination() {
        return Optional.ofNullable(destination);
    }

    /**
     * Send an {@link OnlineUser} the corresponding message indicating the state of the teleport once it has executred
     *
     * @param locales the {@link Locales} instance to use
     * @param user    the {@link OnlineUser} to send the message to
     * @since 3.1
     */
    public void sendResultMessage(@NotNull Locales locales, @NotNull OnlineUser user) {
        this.state.sendResultMessage(locales, user);
    }

    /**
     * Represents the state of a {@link Teleport} after it has finished executing
     */
    public enum ResultState {

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
        FAILED_TELEPORTER_NOT_RESOLVED(false, "error_teleport_target_not_resolved"),

        /**
         * Returns if the {@link Teleport} failed because the target {@link Position} could not be resolved
         */
        FAILED_TARGET_NOT_RESOLVED(false, "error_teleport_teleporter_not_resolved"),

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

        /**
         * The {@link Locales} key to use when sending a message to the teleporter
         */
        @Nullable
        private final String messageId;

        ResultState(boolean successful, @NotNull String messageId) {
            this.successful = successful;
            this.messageId = messageId;
        }

        ResultState(boolean successful) {
            this.successful = successful;
            this.messageId = null;
        }

        /**
         * Send an {@link OnlineUser} the corresponding message for this {@link ResultState}, if there is one
         *
         * @param locales the {@link Locales} instance to use
         * @param user    the {@link OnlineUser} to send the message to
         * @since 3.1
         */
        public void sendResultMessage(@NotNull Locales locales, @NotNull OnlineUser user) {
            Optional.ofNullable(messageId).flatMap(locales::getLocale).ifPresent(user::sendMessage);
        }

    }
}