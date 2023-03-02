package net.william278.huskhomes.teleport;

import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents the result of executing a {@link Teleport} or {@link TimedTeleport}
 *
 * @since 3.1
 */
public class CompletedTeleport {

    /**
     * The state of the {@link Teleport}
     */
    @NotNull
    private final TeleportResult state;

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
     * <b>Internal</b> - Create a new {@link CompletedTeleport} (use {@link #from(TeleportResult, Teleport)})
     *
     * @param state       The {@link TeleportResult} of completing the teleport
     * @param teleporter  The {@link User} who is doing the teleporting
     * @param destination The {@link Position} the teleporter should be teleported to
     * @since 3.1
     */
    private CompletedTeleport(@NotNull TeleportResult state, @Nullable User teleporter, @Nullable Position destination) {
        this.state = state;
        this.teleporter = teleporter;
        this.destination = destination;
    }

    /**
     * Create a new {@link CompletedTeleport} for a {@link Teleport} with an outcome {@link TeleportResult}
     *
     * @param state    The outcome {@link TeleportResult}
     * @param teleport The {@link Teleport} that was executed
     * @return The {@link CompletedTeleport} of executing the supplied {@link Teleport}
     * @since 3.1
     */
    public static CompletedTeleport from(@NotNull TeleportResult state, @NotNull Teleport teleport) {
        return new CompletedTeleport(state, teleport.teleporter, teleport.target);
    }

    /**
     * Get the state of the {@link Teleport} after execution has finished
     *
     * @return The {@link TeleportResult} of the teleport
     * @since 3.1
     */
    @NotNull
    public TeleportResult getState() {
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
     * @implNote The optional will return empty if the state is {@link TeleportResult#FAILED_TELEPORTER_NOT_RESOLVED}
     * @since 3.1
     */
    public Optional<User> getTeleporter() {
        return Optional.ofNullable(teleporter);
    }

    /**
     * Get the target {@link Position} the teleporter should have been teleported to
     *
     * @return The target position, if it was resolved
     * @implNote The optional will return empty if the state is {@link TeleportResult#FAILED_TARGET_NOT_RESOLVED}
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

}