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
    protected final HuskHomes implementor;

    public EventProcessor(@NotNull HuskHomes implementor) {
        this.implementor = implementor;
    }

    /**
     * Handle when a {@link Player} joins the server
     *
     * @param player the joining {@link Player}
     */
    public void onPlayerJoin(@NotNull Player player) {
        // Ensure the player is present on the database first
        implementor.getDatabase().ensureUser(player).thenRun(() -> {

            // If the server is in proxy mode, check if the player is teleporting cross-server and handle
            if (implementor.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
                implementor.getDatabase().getCurrentTeleport(new User(player))
                        .thenAccept(teleport -> teleport.ifPresent(currentTeleport ->
                                player.teleport(currentTeleport.target).thenAccept(teleportResult -> {
                                    if (!teleportResult.successful) {
                                        implementor.getLocales().getLocale("error_invalid_on_arrival")
                                                .ifPresent(player::sendMessage);
                                    } else {
                                        implementor.getLocales().getLocale("teleporting_complete")
                                                .ifPresent(player::sendMessage);
                                    }
                                })));
                // Update the player list
                assert implementor.getNetworkMessenger() != null;
                implementor.getCache().updatePlayerList(implementor, player);
            }

            // Cache this user's homes
            implementor.getDatabase().getHomes(new User(player)).thenAccept(homes ->
                    implementor.getCache().homes.put(player.getUuid(),
                            homes.stream().map(home -> home.meta.name).collect(Collectors.toList())));
        });
    }

    /**
     * Handle when a {@link Player} leaves the server
     *
     * @param player the leaving {@link Player}
     */
    public void onPlayerLeave(@NotNull Player player) {
        // Remove this user's home cache
        implementor.getCache().homes.remove(player.getUuid());

        // Update the player list
        if (implementor.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
            assert implementor.getNetworkMessenger() != null;
            implementor.getOnlinePlayers().stream().filter(
                            onlinePlayer -> !onlinePlayer.getUuid().equals(player.getUuid()))
                    .findAny().ifPresent(updater ->
                            implementor.getCache().updatePlayerList(implementor, updater));
        }
    }

}