package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.EconomyUnsupportedException;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A cross-platform representation of a logged-in {@link User}
 */
public abstract class OnlineUser extends User {

    public OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }


    /**
     * Returns the current {@link Position} of this player
     *
     * @return the player's current {@link Position}
     */
    public abstract CompletableFuture<Position> getPosition();

    /**
     * Returns the current local {@link Location} of this player
     *
     * @return the player's current {@link Location} on the server
     */
    public abstract Location getLocation();

    /**
     * Returns the health of this player
     *
     * @return the player's health points
     */
    public abstract double getHealth();

    /**
     * Returns if the player has the permission node
     *
     * @param node The permission node string
     * @return {@code true} if the player has the node; {@code false} otherwise
     */
    public abstract boolean hasPermission(@NotNull String node);

    /**
     * Dispatch a MineDown-formatted action bar message to this player
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public abstract void sendActionBar(@NotNull MineDown mineDown);


    /**
     * Dispatch a MineDown-formatted chat message to this player
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public abstract void sendMessage(@NotNull MineDown mineDown);

    /**
     * Teleport a player to the specified {@link Location}
     *
     * @param location the {@link Location} to teleport the player to
     */
    public abstract CompletableFuture<TeleportResult> teleport(Location location);

    /**
     * Returns the maximum number of homes this player can set
     *
     * @return a {@link CompletableFuture} providing the max number of homes this player can set
     */
    public abstract CompletableFuture<Integer> getMaxHomes();

    /**
     * Returns the number of homes this player can set for free
     *
     * @return a {@link CompletableFuture} providing the max number of homes this player can set
     */
    public abstract CompletableFuture<Integer> getFreeHomes();

    /**
     * Get this player's economy balance
     *
     * @return the player's economy balance
     * @throws EconomyUnsupportedException if the economy integration is not enabled
     */
    public abstract double getEconomyBalance() throws EconomyUnsupportedException;

    /**
     * Deduct money from this player's economy balance
     *
     * @throws EconomyUnsupportedException if the economy integration is not enabled
     */
    public abstract void deductEconomyBalance() throws EconomyUnsupportedException;


}