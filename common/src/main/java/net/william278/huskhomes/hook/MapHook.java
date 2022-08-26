package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
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
            if (plugin.getSettings().publicHomesOnMap) {
                plugin.getDatabase().getPublicHomes().thenAccept(homes -> homes.stream()
                        .filter(home -> plugin.getWorlds().stream().anyMatch(world -> world.uuid.equals(home.world.uuid)))
                        .forEach(this::updateHome));
            }
            if (plugin.getSettings().warpsOnMap) {
                plugin.getDatabase().getWarps().thenAccept(warps -> warps
                        .stream()
                        .filter(warp -> plugin.getWorlds().stream().anyMatch(world -> world.uuid.equals(warp.world.uuid)))
                        .forEach(this::updateWarp));
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

}
