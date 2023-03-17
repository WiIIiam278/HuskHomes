package net.william278.huskhomes;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.paginedown.ListOptions;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A cache used to hold frequently accessed data (i.e. TAB-completed homes and warps)
 */
public class Cache {

    private final HuskHomes plugin;
    private final Map<String, List<String>> homes = new HashMap<>();
    private final Map<String, List<String>> publicHomes = new HashMap<>();
    private final List<String> warps = new ArrayList<>();
    private final Set<String> players = new HashSet<>();
    private final Map<String, PaginatedList> privateHomeLists = new HashMap<>();
    private final Map<UUID, PaginatedList> publicHomeLists = new HashMap<>();
    private final Map<UUID, PaginatedList> warpLists = new HashMap<>();
    private final Set<UUID> currentlyOnWarmup = new HashSet<>();

    /**
     * Create a new cache
     */
    public Cache(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.initialize();
    }

    /**
     * Initialize the cache with public home and warp names
     */
    public void initialize() {
        final Database database = plugin.getDatabase();
        plugin.runAsync(() -> {
            database.getPublicHomes().forEach(home -> {
                this.getPublicHomes().putIfAbsent(home.getOwner().getUsername(), new ArrayList<>());
                this.getPublicHomes().get(home.getOwner().getUsername()).add(home.getMeta().getName());
            });
            database.getWarps().forEach(warp -> this.getWarps().add(warp.getMeta().getName()));
        });
    }

    /**
     * Updates the cached list of online players and returns it
     *
     * @param plugin the implementing plugin
     */
    public CompletableFuture<Set<String>> updatePlayerListCache(@NotNull HuskHomes plugin, @NotNull OnlineUser requester) {
        getPlayers().clear();
        getPlayers().addAll(plugin.getOnlineUsers()
                .stream()
                .filter(player -> !player.isVanished())
                .map(User::getUsername)
                .toList());
//todo
//        if (plugin.getSettings().isCrossServer()) {
//            return plugin.getMessenger()
//                    .getOnlinePlayerNames(requester)
//                    .thenApply(networkedPlayers -> {
//                        getPlayers().addAll(Set.of(networkedPlayers));
//                        return getPlayers();
//                    });
//        }
        return CompletableFuture.completedFuture(getPlayers());
    }

    /**
     * Returns if the given user is currently warming up to teleport to a home.
     *
     * @param userUuid The user to check.
     * @return If the user is currently warming up.
     * @since 3.1
     */
    public boolean isWarmingUp(@NotNull UUID userUuid) {
        return this.getCurrentlyOnWarmup().contains(userUuid);
    }


    /**
     * Cached home names - maps a {@link UUID} to a list of their homes
     */
    public Map<String, List<String>> getHomes() {
        return homes;
    }

    /**
     * Cached home names - maps a username to a list of their public homes
     */
    public Map<String, List<String>> getPublicHomes() {
        return publicHomes;
    }

    /**
     * Cached warp names
     */
    public List<String> getWarps() {
        return warps;
    }

    /**
     * Cached player list
     */
    public Set<String> getPlayers() {
        return players;
    }

    /**
     * Cached lists of private homes for pagination, mapped to the username of the home owner
     */
    public Map<String, PaginatedList> getPrivateHomeLists() {
        return privateHomeLists;
    }

    /**
     * Cached lists of public homes for pagination
     */
    public Map<UUID, PaginatedList> getPublicHomeLists() {
        return publicHomeLists;
    }

    /**
     * Cached lists of warps for pagination
     */
    public Map<UUID, PaginatedList> getWarpLists() {
        return warpLists;
    }

    /**
     * Cached user UUIDs currently on warmup countdowns for {@link TimedTeleport}s
     */
    public Set<UUID> getCurrentlyOnWarmup() {
        return currentlyOnWarmup;
    }
}
