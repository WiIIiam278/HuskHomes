package net.william278.huskhomes.teleport;

import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a teleport in the process of being executed
 */
public class Teleport {

    /**
     * The player involved in the teleport
     */
    @NotNull
    public Player player;

    /**
     * The target position for the player
     */
    @NotNull
    public Position target;

    public Teleport(@NotNull Player player, @NotNull Position target) {
        this.player = player;
        this.target = target;
    }
}
