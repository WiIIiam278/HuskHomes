package net.william278.huskhomes.api;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.util.BukkitAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The HuskHomes API implementation for the Bukkit platform, providing methods to access player data, homes, warps
 * & process teleports
 * </p>
 * Retrieve an instance of the API class via {@link #getInstance()}.
 */
@SuppressWarnings("unused")
public class HuskHomesAPI extends BaseHuskHomesAPI {

    /**
     * <b>(Internal use only)</b> - Instance of the API class
     */
    private static final HuskHomesAPI INSTANCE = new HuskHomesAPI();

    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the API
     */
    private HuskHomesAPI() {
        super(BukkitHuskHomes.getInstance());
    }

    /**
     * Entrypoint to the HuskHomes API - returns an instance of the API
     *
     * @return instance of the HuskHomes API
     */
    public static HuskHomesAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Returns an {@link OnlineUser} instance for the given bukkit {@link Player}.
     *
     * @param player the bukkit player to get the {@link User} instance for
     * @return the {@link OnlineUser} instance for the given bukkit {@link Player}
     * @since 3.0
     */
    @NotNull
    public OnlineUser adaptUser(@NotNull Player player) {
        return BukkitPlayer.adapt(player);
    }

    /**
     * Returns the bukkit {@link Player} being represented by the given {@link OnlineUser}.
     *
     * @param user {@link OnlineUser} to get the bukkit player from
     * @return the bukkit {@link Player} being represented by the given {@link OnlineUser}
     * @since 3.0
     */
    @NotNull
    public Player getPlayer(@NotNull OnlineUser user) {
        return ((BukkitPlayer) user).getPlayer();
    }

    /**
     * Returns the bukkit {@link Location} being represented by the given {@link Position}.
     *
     * @param position the {@link Position} to get the bukkit location from
     * @return the bukkit {@link Location} being represented by the given {@link Position}
     * @since 3.0
     */
    @Nullable
    public org.bukkit.Location getLocation(@NotNull Position position) {
        return BukkitAdapter.adaptLocation(position).orElse(null);
    }

    /**
     * Returns a {@link Location} instance for the given bukkit {@link Location}.
     *
     * @param location the bukkit location to get the {@link Location} instance for
     * @return the {@link Location} instance for the given bukkit {@link Location}
     * @since 3.0
     */
    @Nullable
    public Location adaptLocation(@NotNull org.bukkit.Location location) {
        return BukkitAdapter.adaptLocation(location).orElse(null);
    }

    /**
     * Returns a {@link Position} instance for the given bukkit {@link Location} on the given {@link Server}.
     *
     * @param location the bukkit location to get the {@link Position} instance for
     * @param server   the {@link Server} the position is on
     * @see Position#server
     * @since 3.0
     */
    @Nullable
    public Position adaptPosition(@NotNull org.bukkit.Location location, @NotNull Server server) {
        return new Position(Objects.requireNonNull(adaptLocation(location)), server);
    }

    /**
     * Get this {@link Server}.
     *
     * @param requester a player; needed to send a plugin message requesting the server if it has not been cached
     * @return the {@link Server}
     */
    @NotNull
    public Server getServer(@NotNull OnlineUser requester) {
        return plugin.getServer(requester);
    }

}
