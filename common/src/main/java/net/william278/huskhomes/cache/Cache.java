package net.william278.huskhomes.cache;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.data.Database;
import net.william278.huskhomes.list.PositionList;
import net.william278.huskhomes.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * A cache used to hold persistently used data
 */
public class Cache {

    /**
     * Cached home names - maps a {@link UUID} to a list of their homes
     */
    public final HashMap<UUID, List<String>> homes;

    /**
     * Cached home names - maps a username to a list of their public homes
     */
    public final HashMap<String, List<String>> publicHomes;

    /**
     * Cached warp names
     */
    public final List<String> warps;

    /**
     * Cached player list
     */
    public final List<String> players;

    /**
     * Cached position lists for optimized list navigating
     */
    public final HashMap<UUID, PositionList> positionLists;

    /**
     * Create a new cache
     */
    public Cache() {
        this.homes = new HashMap<>();
        this.publicHomes = new HashMap<>();
        this.warps = new ArrayList<>();
        this.players = new ArrayList<>();
        this.positionLists = new HashMap<>();
    }

    /**
     * Initialize the cache, request basic data to load into memory
     *
     * @param database the database to load data from
     */
    public void initialize(@NotNull Database database) {
        CompletableFuture.runAsync(() -> {
            database.getPublicHomes().thenAccept(publicHomeList -> publicHomeList.forEach(home -> {
                this.publicHomes.putIfAbsent(home.owner.username, new ArrayList<>());
                this.publicHomes.get(home.owner.username).add(home.meta.name);
            }));
            database.getWarps().thenAccept(warpsList -> warpsList.forEach(warp ->
                    this.warps.add(warp.meta.name)));
        });
    }

    /**
     * Updates the cached list of online players
     *
     * @param plugin the implementing plugin
     */
    public void updatePlayerList(@NotNull HuskHomes plugin, @NotNull Player requester) {
        if (plugin.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
            assert plugin.getNetworkMessenger() != null;
            CompletableFuture.runAsync(() -> plugin.getNetworkMessenger().
                    getOnlinePlayerNames(requester).thenAcceptAsync(returnedPlayerList -> {
                        players.clear();
                        players.addAll(List.of(returnedPlayerList));
                    }));
        } else {
            players.clear();
            players.addAll(plugin.getOnlinePlayers().stream().map(Player::getName).toList());
        }
    }

}
