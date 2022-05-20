package net.william278.huskhomes.player;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A cross-platform representation of a player
 */
public interface Player {

    /**
     * Return the player's name
     *
     * @return the player's name
     */
    String getName();

    /**
     * Return the player's {@link UUID}
     *
     * @return the player {@link UUID}
     */
    UUID getUuid();

    /**
     * Returns the current {@link Position} of this player
     *
     * @return the player's current {@link Position}
     */
    Position getPosition();

    /**
     * Returns if the player has the permission node
     *
     * @param node The permission node string
     * @return {@code true} if the player has the node; {@code false} otherwise
     */
    boolean hasPermission(String node);

    void sendMessage();

    /**
     * Returns the maximum number of homes this player can set
     *
     * @return a {@link CompletableFuture} providing the max number of homes this player can set
     */
    CompletableFuture<Integer> getMaxHomes();

    /**
     * Returns the number of homes this player can set for free
     *
     * @return a {@link CompletableFuture} providing the max number of homes this player can set
     */
    CompletableFuture<Integer> getFreeHomes();

    /**
     * Teleport the player to a specified {@link Position}
     *
     * @param position the target {@link Position} to teleport to
     * @return A {@link CompletableFuture} callback returning the {@link TeleportResult} of completing the teleport
     */
    CompletableFuture<TeleportResult> teleport(Position position);

}