package net.william278.huskhomes.listener;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportType;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;

/**
 * A handler for when events take place
 */
public class EventListener {

    @NotNull
    protected final HuskHomes plugin;

    protected EventListener(@NotNull HuskHomes implementor) {
        this.plugin = implementor;
    }

    /**
     * Handle when a {@link OnlineUser} joins the server
     *
     * @param onlineUser the joining {@link OnlineUser}
     */
    protected final void handlePlayerJoin(@NotNull OnlineUser onlineUser) {
        plugin.runAsync(() -> {
            // Ensure the user is in the database
            plugin.getDatabase().ensureUser(onlineUser);

            // Handle any inbound teleports
            if (plugin.getSettings().isCrossServer()) {
                this.handleInboundTeleport(onlineUser);
            }

            // Set their ignoring requests state
            plugin.getDatabase().getUserData(onlineUser.uuid).ifPresent(userData -> {
                final boolean ignoringRequests = userData.ignoringTeleports();
                plugin.getRequestManager().setIgnoringRequests(onlineUser, ignoringRequests);

                // Send a reminder message if they are still ignoring requests
                if (ignoringRequests) {
                    plugin.getLocales().getRawLocale("tpignore_on_notification", plugin.getLocales()
                            .getRawLocale("tpignore_toggle_button")
                            .orElse("")).ifPresent(locale -> onlineUser.sendMessage(new MineDown(locale)));
                }
            });

            // Cache this user's homes
            plugin.getCache().getHomes().put(onlineUser.uuid, plugin.getDatabase()
                    .getHomes(onlineUser).stream()
                    .map(Home::getName)
                    .toList());
        });
    }

    /**
     * Handle inbound cross-server teleports
     *
     * @param onlineUser user to handle the checks for
     */
    private void handleInboundTeleport(@NotNull OnlineUser onlineUser) {
        plugin.getDatabase().getCurrentTeleport(onlineUser).ifPresent(teleport -> {
            if (teleport.type == TeleportType.RESPAWN) {
                final Optional<Position> bedPosition = onlineUser.getBedSpawnPosition();
                if (bedPosition.isEmpty()) {
                    plugin.getLocalCachedSpawn()
                            .flatMap(spawn -> spawn.getPosition(plugin.getServerName()))
                            .ifPresent(position -> onlineUser.teleportLocally(position,
                                    plugin.getSettings().isAsynchronousTeleports()));
                    onlineUser.sendTranslatableMessage("block.minecraft.spawn.not_valid");
                } else {
                    onlineUser.teleportLocally(bedPosition.get(), plugin.getSettings().isAsynchronousTeleports());
                }
                plugin.getDatabase().setCurrentTeleport(onlineUser, null);
                plugin.getDatabase().setRespawnPosition(onlineUser, bedPosition.orElse(null));
                return;
            }
            teleport.execute()
                    .thenRun(() -> plugin.getDatabase().setCurrentTeleport(onlineUser, null))
                    .exceptionally(throwable -> {
                        plugin.getLoggingAdapter().log(Level.SEVERE,
                                "An error occurred while teleporting an inbound player", throwable);
                        return null;
                    });
        });
    }

    /**
     * Handle when a {@link OnlineUser} leaves the server
     *
     * @param onlineUser the leaving {@link OnlineUser}
     */
    protected final void handlePlayerLeave(@NotNull OnlineUser onlineUser) {
        // Remove this user's home cache
        plugin.getCache().getHomes().remove(onlineUser.uuid);

        // Update the cached player list using another online player if possible
        plugin.getOnlinePlayers()
                .stream()
                .filter(onlinePlayer -> !onlinePlayer.equals(onlineUser))
                .findAny()
                .ifPresent(updater -> plugin.getCache().updatePlayerListCache(plugin, updater));


        // Set offline position
        plugin.getDatabase().setOfflinePosition(onlineUser, onlineUser.getPosition());
    }

    /**
     * Handle when a {@link OnlineUser} dies
     *
     * @param onlineUser the {@link OnlineUser} who died
     */
    protected final void handlePlayerDeath(@NotNull OnlineUser onlineUser) {
        // Set the player's last position to where they died
        if (plugin.getSettings().isBackCommandReturnByDeath()
                && onlineUser.hasPermission(Permission.COMMAND_BACK_RETURN_BY_DEATH.node)) {
            plugin.getDatabase().setLastPosition(onlineUser, onlineUser.getPosition());
        }
    }

    /**
     * Handle when a {@link OnlineUser} respawns after dying
     *
     * @param onlineUser the respawning {@link OnlineUser}
     */
    protected final void handlePlayerRespawn(@NotNull OnlineUser onlineUser) {
        plugin.runAsync(() -> {
            // Display the return by death via /back notification
            if (plugin.getSettings().isBackCommandReturnByDeath()
                    && onlineUser.hasPermission(Permission.COMMAND_BACK.node)
                    && onlineUser.hasPermission(Permission.COMMAND_BACK_RETURN_BY_DEATH.node)) {
                plugin.getLocales().getLocale("return_by_death_notification")
                        .ifPresent(onlineUser::sendMessage);
            }

            // Respawn the player cross-server if needed
            if (plugin.getSettings().isCrossServer() && plugin.getSettings().isGlobalRespawning()) {
                plugin.getDatabase().getRespawnPosition(onlineUser).ifPresent(respawnPosition -> {
                    if (!respawnPosition.getServer().equals(plugin.getServerName())) {
                        Teleport.builder(plugin, onlineUser)
                                .setType(TeleportType.RESPAWN)
                                .setTarget(respawnPosition)
                                .toTeleport()
                                .thenAccept(Teleport::execute);
                    }
                });
            }
        });
    }

    /**
     * Handle when a player teleports
     *
     * @param onlineUser     the {@link OnlineUser} who teleported
     * @param sourcePosition the source {@link Position} they came from
     */
    protected final void handlePlayerTeleport(@NotNull OnlineUser onlineUser, @NotNull Position sourcePosition) {
        if (!plugin.getSettings().isBackCommandSaveOnTeleportEvent()) {
            return;
        }

        plugin.runAsync(() -> plugin.getDatabase().getUserData(onlineUser.uuid)
                .ifPresent(data -> plugin.getDatabase().setLastPosition(data.user(), sourcePosition)));
    }

    /**
     * Handle when an {@link OnlineUser}'s spawn point is updated
     *
     * @param onlineUser the {@link OnlineUser} whose spawn point was updated
     * @param position   the new spawn point
     */
    protected final void handlePlayerUpdateSpawnPoint(@NotNull OnlineUser onlineUser, @NotNull Position position) {
        if (plugin.getSettings().isCrossServer() && plugin.getSettings().isGlobalRespawning()) {
            plugin.getDatabase().setRespawnPosition(onlineUser, position);
        }
    }

    /**
     * Handle when the plugin is disabling (server is shutting down)
     */
    public final void handlePluginDisable() {
        plugin.getLoggingAdapter().log(Level.INFO, "Successfully disabled HuskHomes v" + plugin.getPluginVersion());
    }

}