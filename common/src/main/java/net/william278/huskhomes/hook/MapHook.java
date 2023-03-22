package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A hook for a mapping plugin, such as Dynmap
 */
public abstract class MapHook extends Hook {

    protected static final String WARP_MARKER_IMAGE_NAME = "warp";
    protected static final String PUBLIC_HOME_MARKER_IMAGE_NAME = "public-home";

    protected MapHook(@NotNull HuskHomes plugin, @NotNull MapHook.Plugin type) {
        super(plugin, type.getDisplayName());
    }

    /**
     * Populate the map with public homes and warps
     */
    protected void populateMap() {
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
    }

    /**
     * Update a home, adding it to the map if it exists, or updating it on the map if it doesn't
     *
     * @param home the home to update
     */
    public abstract void updateHome(@NotNull Home home);

    /**
     * Removes a home from the map
     *
     * @param home the home to remove
     */
    public abstract void removeHome(@NotNull Home home);

    /**
     * Clears homes owned by a player from the map
     *
     * @param user the player whose homes to clear
     */
    public abstract void clearHomes(@NotNull User user);

    /**
     * Update a warp, adding it to the map if it exists, or updating it on the map if it doesn't
     *
     * @param warp the warp to update
     */
    public abstract void updateWarp(@NotNull Warp warp);

    /**
     * Removes a warp from the map
     *
     * @param warp the warp to remove
     */
    public abstract void removeWarp(@NotNull Warp warp);

    /**
     * Clears all warps from the map
     */
    public abstract void clearWarps();

    /**
     * Returns if the position is valid to be set on this server
     *
     * @param position the position to check
     * @return if the position is valid
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected final boolean isValidPosition(@NotNull SavedPosition position) {
        if (position instanceof Warp && !plugin.getSettings().isWarpsOnMap()) {
            return false;
        }
        if (position instanceof Home && !plugin.getSettings().isPublicHomesOnMap()) {
            return false;
        }

        return position.getServer().equals(plugin.getServerName());
    }

    @NotNull
    protected final String getPublicHomesKey() {
        return plugin.getKey(getName().toLowerCase(), "public_homes", "markers").toString();
    }

    @NotNull
    protected final String getWarpsKey() {
        return plugin.getKey(getName().toLowerCase(), "warps", "markers").toString();
    }

    /**
     * Identifies types of supported Map plugins
     */
    public enum Plugin {
        DYNMAP("Dynmap"),
        BLUEMAP("BlueMap");

        private final String displayName;

        Plugin(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @NotNull
        public String getDisplayName() {
            return displayName;
        }
    }

}
