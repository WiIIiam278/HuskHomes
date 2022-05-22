package net.william278.huskhomes.listener;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

/**
 * A handler for when events take place
 */
public class EventProcessor {

    @NotNull
    private final HuskHomes implementor;

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
        implementor.getDatabase().ensurePlayer(player).thenRun(() -> {
            // If the server is in proxy mode, check if the player is teleporting cross-server
            if (implementor.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
                implementor.getDatabase().getCurrentTeleport(new User(player.getUuid(), player.getName()))
                        .thenAccept(teleport -> teleport.ifPresent(currentTeleport ->
                                player.teleport(currentTeleport.target).thenAccept(teleportResult -> {
                                    if (!teleportResult.successful) {
                                        //todo error handling if the teleport was not successful on arrival
                                        return;
                                    } else {
                                        //todo display teleport complete ^-^
                                    }
                                })));
            }
        });
    }

}