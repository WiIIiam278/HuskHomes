package net.william278.huskhomes.teleport;

import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link Teleport} due to occur after a warmup period
 */
public class TimedTeleport {

    private final OnlineUser onlineUser;
    private final Position targetPosition;
    private final Location startLocation;
    private final double startHealth;
    protected int timeLeft;
    public boolean cancelled = false;

    protected TimedTeleport(@NotNull OnlineUser onlineUser, @NotNull Position targetPosition, int duration) {
        this.onlineUser = onlineUser;
        this.targetPosition = targetPosition;
        this.startLocation = onlineUser.getLocation();
        this.startHealth = onlineUser.getHealth();
        this.timeLeft = duration;
    }

    public final boolean isDone() {
        return timeLeft <= 0;
    }

    public final void countDown() {
        timeLeft--;
    }

    /**
     * Get the teleporting player
     *
     * @return the {@link OnlineUser} who is teleporting
     */
    public OnlineUser getPlayer() {
        return onlineUser;
    }

    /**
     * Returns if the player has moved since the timed teleport started
     *
     * @return {@code true} if the player has moved; {@code false} otherwise
     */
    public boolean hasMoved() {
        final double maxMovementDistance = 0.1d;
        double movementDistance = Math.abs(startLocation.x - onlineUser.getLocation().x) +
                                  Math.abs(startLocation.y - onlineUser.getLocation().y) +
                                  Math.abs(startLocation.z - onlineUser.getLocation().z);
        return movementDistance > maxMovementDistance;
    }

    /**
     * Returns if the player has taken damage since the timed teleport started
     *
     * @return {@code true} if the player has taken damage
     */
    public boolean hasTakenDamage() {
        return onlineUser.getHealth() < startHealth;
    }

    /**
     * Returns the target {@link Position} of this timed teleport
     *
     * @return the target {@link Position}
     */
    public Position getTargetPosition() {
        return targetPosition;
    }

}
