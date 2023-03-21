package net.william278.huskhomes;

import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.teleport.TimedTeleport;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A cache used to hold frequently accessed data (i.e. TAB-completed homes and warps)
 */
public class Cache {

    private final HuskHomes plugin;
    private final Map<String, List<String>> homes = new HashMap<>();
    private final Map<String, List<String>> publicHomes = new HashMap<>();
    private final List<String> warps = new ArrayList<>();
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
     * Cached user UUIDs currently on warmup countdowns for {@link TimedTeleport}s
     */
    public Set<UUID> getCurrentlyOnWarmup() {
        return currentlyOnWarmup;
    }
}
