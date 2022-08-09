package net.william278.huskhomes.listener;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * A handler for when events take place
 */
public class EventListener {

    @NotNull
    protected final HuskHomes plugin;

    public EventListener(@NotNull HuskHomes implementor) {
        this.plugin = implementor;
    }

    /**
     * Handle when a {@link OnlineUser} joins the server
     *
     * @param onlineUser the joining {@link OnlineUser}
     */
    public void onPlayerJoin(@NotNull OnlineUser onlineUser) {
        // Ensure the player is present on the database first
        plugin.getDatabase().ensureUser(onlineUser).thenRun(() -> {

            // If the server is in proxy mode, check if the player is teleporting cross-server and handle
            if (plugin.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
                plugin.getDatabase().getCurrentTeleport(onlineUser)
                        .thenAccept(teleport -> teleport.ifPresent(currentTeleport ->
                                // Teleport the player locally
                                onlineUser.teleport(currentTeleport.target).thenAccept(teleportResult -> {
                                    if (!teleportResult.successful) {
                                        plugin.getLocales().getLocale("error_invalid_on_arrival")
                                                .ifPresent(onlineUser::sendMessage);
                                    } else {
                                        plugin.getLocales().getLocale("teleporting_complete")
                                                .ifPresent(onlineUser::sendMessage);
                                    }
                                })));
                // Update the player list
                assert plugin.getNetworkMessenger() != null;
                plugin.getCache().updatePlayerList(plugin, onlineUser);
            }

            // Cache this user's homes
            plugin.getDatabase().getHomes(onlineUser).thenAccept(homes ->
                    plugin.getCache().homes.put(onlineUser.uuid,
                            homes.stream().map(home -> home.meta.name).collect(Collectors.toList())));

            // Ensure the server has been set
            if (plugin.getOnlinePlayers().size() == 1) {
                plugin.getServer(onlineUser);
            }
        });
    }

    /**
     * Handle when a {@link OnlineUser} leaves the server
     *
     * @param onlineUser the leaving {@link OnlineUser}
     */
    public void onPlayerLeave(@NotNull OnlineUser onlineUser) {
        // Remove this user's home cache
        plugin.getCache().homes.remove(onlineUser.uuid);

        // Update the player list
        if (plugin.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
            assert plugin.getNetworkMessenger() != null;
            plugin.getOnlinePlayers().stream().filter(
                            onlinePlayer -> !onlinePlayer.uuid.equals(onlineUser.uuid))
                    .findAny().ifPresent(updater ->
                            plugin.getCache().updatePlayerList(plugin, updater));
        }

        // Set offline position
        onlineUser.getPosition().thenAcceptAsync(position -> plugin.getDatabase()
                .setOfflinePosition(onlineUser, position));
    }

    /**
     * Handle when a {@link OnlineUser} dies
     *
     * @param onlineUser the {@link OnlineUser} who died
     */
    public void onPlayerDeath(@NotNull OnlineUser onlineUser) {
        //todo
    }

    /**
     * Handle when a {@link OnlineUser} respawns after dying
     *
     * @param onlineUser the respawning {@link OnlineUser}
     */
    public void onPlayerRespawn(@NotNull OnlineUser onlineUser) {
        //todo
    }

    /**
     * Handle when the plugin is disabling (server is shutting down)
     */
    public void handlePluginDisable() {
        //todo
    }

}