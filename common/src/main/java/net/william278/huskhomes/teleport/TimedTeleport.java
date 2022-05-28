package net.william278.huskhomes.teleport;

import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link Teleport} due to occur
 */
public class TimedTeleport {

    private final Player player;
    private final Position targetPosition;

    private final Location startLocation;
    private final double startHealth;

    private final int duration;
    private int timeLeft;

    public boolean cancelled = false;

    protected TimedTeleport(@NotNull Player player, @NotNull Position targetPosition, int duration) {
        this.player = player;
        this.targetPosition = targetPosition;
        this.startLocation = player.getLocation();
        this.startHealth = player.getHealth();
        this.duration = duration;
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
     * @return the {@link Player} who is teleporting
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns if the player has moved since the timed teleport started
     *
     * @return {@code true} if the player has moved; {@code false} otherwise
     */
    public boolean hasMoved() {
        final double maxMovementDistance = 0.1d;
        double movementDistance = Math.abs(startLocation.x - player.getLocation().x) +
                Math.abs(startLocation.y - player.getLocation().y) +
                Math.abs(startLocation.z - player.getLocation().z);
        return movementDistance > maxMovementDistance;
    }

    /**
     * Returns if the player has taken damage since the timed teleport started
     *
     * @return {@code true} if the player has taken damage
     */
    public boolean hasTakenDamage() {
        return player.getHealth() < startHealth;
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
