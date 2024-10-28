package net.william278.huskhomes.util;

import net.william278.desertwell.util.UpdateChecker;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public interface MetaProvider {

    int SPIGOT_RESOURCE_ID = 83767;

    /**
     * Get the plugin {@link Version}.
     *
     * @return the plugin version
     * @since 4.8
     */
    @NotNull
    Version getPluginVersion();

    /**
     * Get the plugin {@link UpdateChecker}.
     *
     * @return the plugin {@link UpdateChecker}
     * @since 4.8
     */
    @NotNull
    default UpdateChecker getUpdateChecker() {
        return UpdateChecker.builder()
                .currentVersion(getPluginVersion())
                .endpoint(UpdateChecker.Endpoint.SPIGOT)
                .resource(Integer.toString(SPIGOT_RESOURCE_ID))
                .build();
    }

    /**
     * Check for updates and log a warning if an update is available.
     *
     * @since 4.8
     */
    default void checkForUpdates() {
        if (!getPlugin().getSettings().isCheckForUpdates()) {
            return;
        }
        getUpdateChecker().check().thenAccept(checked -> {
            if (checked.isUpToDate()) {
                return;
            }
            getPlugin().log(Level.WARNING, String.format(
                    "A new version of HuskHomes is available: v%s (running v%s)",
                    checked.getLatestVersion(), getPluginVersion())
            );
        });
    }

    /**
     * Get the server type.
     *
     * @return the server type
     * @since 4.8
     */
    @NotNull
    String getServerType();

    /**
     * Get the Minecraft version.
     *
     * @return the Minecraft version
     * @since 4.8
     */
    @NotNull
    Version getMinecraftVersion();

    @NotNull
    HuskHomes getPlugin();

}
