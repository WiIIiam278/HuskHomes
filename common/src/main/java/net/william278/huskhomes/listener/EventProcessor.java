package net.william278.huskhomes.listener;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * A handler for when events take place
 */
public class EventProcessor {

    @NotNull
    protected final HuskHomes plugin;

    public EventProcessor(@NotNull HuskHomes implementor) {
        this.plugin = implementor;
    }

    /**
     * Handle when a {@link Player} joins the server
     *
     * @param player the joining {@link Player}
     */
    public void onPlayerJoin(@NotNull Player player) {
        // Ensure the player is present on the database first
        plugin.getDatabase().ensureUser(player).thenRun(() -> {

            // If the server is in proxy mode, check if the player is teleporting cross-server and handle
            if (plugin.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
                plugin.getDatabase().getCurrentTeleport(new User(player))
                        .thenAccept(teleport -> teleport.ifPresent(currentTeleport ->
                                // Teleport the player locally
                                player.teleport(currentTeleport.target).thenAccept(teleportResult -> {
                                    if (!teleportResult.successful) {
                                        plugin.getLocales().getLocale("error_invalid_on_arrival")
                                                .ifPresent(player::sendMessage);
                                    } else {
                                        plugin.getLocales().getLocale("teleporting_complete")
                                                .ifPresent(player::sendMessage);
                                    }
                                })));
                // Update the player list
                assert plugin.getNetworkMessenger() != null;
                plugin.getCache().updatePlayerList(plugin, player);
            }

            // Cache this user's homes
            plugin.getDatabase().getHomes(new User(player)).thenAccept(homes ->
                    plugin.getCache().homes.put(player.getUuid(),
                            homes.stream().map(home -> home.meta.name).collect(Collectors.toList())));

            // Ensure the server has been set
            if (plugin.getOnlinePlayers().size() == 1) {
                plugin.getServer(player);
            }
        });
    }

    /**
     * Handle when a {@link Player} leaves the server
     *
     * @param player the leaving {@link Player}
     */
    public void onPlayerLeave(@NotNull Player player) {
        // Remove this user's home cache
        plugin.getCache().homes.remove(player.getUuid());

        // Update the player list
        if (plugin.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
            assert plugin.getNetworkMessenger() != null;
            plugin.getOnlinePlayers().stream().filter(
                            onlinePlayer -> !onlinePlayer.getUuid().equals(player.getUuid()))
                    .findAny().ifPresent(updater ->
                            plugin.getCache().updatePlayerList(plugin, updater));
        }

        // Set offline position
        player.getPosition().thenAcceptAsync(position -> plugin.getDatabase().setOfflinePosition(
                new User(player.getUuid(), player.getName()), position));
    }

    /**
     * Handle when a {@link Player} dies
     *
     * @param player the {@link Player} who died
     */
    public void onPlayerDeath(@NotNull Player player) {

    }

    /**
     * Handle when a {@link Player} respawns after dying
     *
     * @param player the respawning {@link Player}
     */
    public void onPlayerRespawn(@NotNull Player player) {

    }

}