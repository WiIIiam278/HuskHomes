package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.util.EconomyUnsupportedException;
import org.jetbrains.annotations.NotNull;

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
    CompletableFuture<Position> getPosition();

    /**
     * Returns the current local {@link Location} of this player
     *
     * @return the player's current {@link Location} on the server
     */
    Location getLocation();

    /**
     * Returns the health of this player
     *
     * @return the player's health points
     */
    double getHealth();

    /**
     * Returns if the player has the permission node
     *
     * @param node The permission node string
     * @return {@code true} if the player has the node; {@code false} otherwise
     */
    boolean hasPermission(@NotNull String node);

    /**
     * Dispatch a MineDown-formatted message to this player
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    void sendMessage(@NotNull MineDown mineDown);

    /**
     * Teleport a player to the specified {@link Location}
     *
     * @param location the {@link Location} to teleport the player to
     */
    CompletableFuture<TeleportResult> teleport(Location location);

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
     * Get this player's economy balance
     *
     * @return the player's economy balance
     * @throws EconomyUnsupportedException if the economy integration is not enabled
     */
    double getEconomyBalance() throws EconomyUnsupportedException;

    /**
     * Deduct money from this player's economy balance
     *
     * @throws EconomyUnsupportedException if the economy integration is not enabled
     */
    void deductEconomyBalance() throws EconomyUnsupportedException;


}