package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A hook for a mapping plugin, such as Dynmap
 */
public abstract class MapHook extends PluginHook {

    protected static final String PUBLIC_HOMES_MARKER_SET_ID = "huskhomes-public-homes";
    protected static final String WARPS_MARKER_SET_ID = "huskhomes-warps";
    protected static final String WARP_MARKER_IMAGE_NAME = "warp";
    protected static final String PUBLIC_HOME_MARKER_IMAGE_NAME = "public-home";

    protected MapHook(@NotNull HuskHomes implementor, @NotNull String hookName) {
        super(implementor, hookName);
    }

    @Override
    public final boolean initialize() {
        initializeMap().thenRun(() -> {
            if (plugin.getSettings().isPublicHomesOnMap()) {
                plugin.getDatabase()
                        .getLocalPublicHomes(plugin)
                        .forEach(this::updateHome);
            }
            if (plugin.getSettings().isWarpsOnMap()) {
                plugin.getDatabase()
                        .getLocalWarps(plugin)
                        .forEach(this::updateWarp);
            }
        });
        return true;
    }

    /**
     * Prepare the map plugin for adding homes to
     *
     * @return a {@link CompletableFuture} that completes when the map plugin is ready
     */
    protected abstract CompletableFuture<Void> initializeMap();

    /**
     * Update a home, adding it to the map if it exists, or updating it on the map if it doesn't
     *
     * @param home the home to update
     */
    @SuppressWarnings("UnusedReturnValue")
    public abstract CompletableFuture<Void> updateHome(@NotNull Home home);

    /**
     * Removes a home from the map
     *
     * @param home the home to remove
     */
    public abstract CompletableFuture<Void> removeHome(@NotNull Home home);

    /**
     * Clears homes owned by a player from the map
     *
     * @param user the player whose homes to clear
     * @return a {@link CompletableFuture} that completes when the homes have been cleared
     */
    public abstract CompletableFuture<Void> clearHomes(@NotNull User user);

    /**
     * Update a warp, adding it to the map if it exists, or updating it on the map if it doesn't
     *
     * @param warp the warp to update
     */
    @SuppressWarnings("UnusedReturnValue")
    public abstract CompletableFuture<Void> updateWarp(@NotNull Warp warp);

    /**
     * Removes a warp from the map
     *
     * @param warp the warp to remove
     */
    public abstract CompletableFuture<Void> removeWarp(@NotNull Warp warp);

    /**
     * Clears all warps from the map
     *
     * @return a {@link CompletableFuture} that completes when the warps have been cleared
     */
    public abstract CompletableFuture<Void> clearWarps();

    /**
     * Returns if the position is valid to be set on this server
     *
     * @param position the position to check
     * @return if the position is valid
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected final boolean isValidPosition(@NotNull SavedPosition position) {
        if (position instanceof Warp && !plugin.getSettings().isWarpsOnMap()) return false;
        if (position instanceof Home && !plugin.getSettings().isPublicHomesOnMap()) return false;

        try {
            return position.getServer().equals(plugin.getServerName());
        } catch (IllegalStateException e) {
            return plugin.getWorlds().stream()
                    .map(World::getUuid)
                    .anyMatch(uuid -> uuid.equals(position.getWorld().getUuid()));
        }
    }

}
