package net.william278.huskhomes.listener;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.user.OnlineUser;
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

    protected EventListener(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
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

            // Handle cross server checks
            if (plugin.getSettings().isCrossServer()) {
                this.handleInboundTeleport(onlineUser);

                // Update caches
                if (plugin.getOnlineUsers().size() == 1) {
                    plugin.getManager().homes().updatePublicHomeCache();
                    plugin.getManager().warps().updateWarpCache();
                }

                // Update the player list
                plugin.runLater(() -> {
                    // Send a player list update to other servers
                    sendPlayerListUpdates(onlineUser);

                    // Request updated player lists from other servers
                    plugin.getGlobalPlayerList().clear();
                    Message.builder()
                            .scope(Message.Scope.SERVER)
                            .target(Message.TARGET_ALL)
                            .type(Message.Type.REQUEST_PLAYER_LIST)
                            .build().send(plugin.getMessenger(), onlineUser);
                }, plugin.getSettings().getBrokerType() == Broker.Type.PLUGIN_MESSAGE ? 40L : 0L);
            }

            // Cache this user's homes
            plugin.getManager().homes().cacheUserHomes(onlineUser);

            // Set their ignoring requests state
            plugin.getDatabase().getUserData(onlineUser.getUuid()).ifPresent(userData -> {
                final boolean ignoringRequests = userData.isIgnoringTeleports();
                plugin.getManager().requests().setIgnoringRequests(onlineUser, ignoringRequests);

                // Send a reminder message if they are still ignoring requests
                if (ignoringRequests) {
                    plugin.getLocales().getRawLocale("tpignore_on_notification", plugin.getLocales()
                            .getRawLocale("tpignore_toggle_button")
                            .orElse("")).ifPresent(locale -> onlineUser.sendMessage(new MineDown(locale)));
                }
            });
        });
    }

    /**
     * Handle inbound cross-server teleports
     *
     * @param teleporter user to handle the checks for
     */
    private void handleInboundTeleport(@NotNull OnlineUser teleporter) {
        plugin.getDatabase().getCurrentTeleport(teleporter).ifPresent(teleport -> {
            if (teleport.getType() == Teleport.Type.RESPAWN) {
                final Optional<Position> bedPosition = teleporter.getBedSpawnPosition();
                if (bedPosition.isEmpty()) {
                    plugin.getSpawn().ifPresent(spawn -> {
                        if (plugin.getSettings().isCrossServer() && !spawn.getServer().equals(plugin.getServerName())) {
                            plugin.runLater(() -> Teleport.builder(plugin)
                                    .teleporter(teleporter)
                                    .target(spawn)
                                    .updateLastPosition(false)
                                    .toTeleport().execute(), 40L);
                        } else {
                            teleporter.teleportLocally(spawn, plugin.getSettings().isAsynchronousTeleports());
                        }
                        teleporter.sendTranslatableMessage("block.minecraft.spawn.not_valid");
                    });
                } else {
                    teleporter.teleportLocally(bedPosition.get(), plugin.getSettings().isAsynchronousTeleports());
                }
                plugin.getDatabase().setCurrentTeleport(teleporter, null);
                plugin.getDatabase().setRespawnPosition(teleporter, bedPosition.orElse(null));
                return;
            }

            teleporter.teleportLocally((Position) teleport.getTarget(), plugin.getSettings().isAsynchronousTeleports());
            plugin.getDatabase().setCurrentTeleport(teleporter, null);
            teleport.displayTeleportingComplete(teleporter);
        });
    }

    /**
     * Handle when a {@link OnlineUser} leaves the server
     *
     * @param onlineUser the leaving {@link OnlineUser}
     */
    protected final void handlePlayerLeave(@NotNull OnlineUser onlineUser) {
        // Set offline position
        plugin.getDatabase().setOfflinePosition(onlineUser, onlineUser.getPosition());

        // Remove this user's home cache
        plugin.getManager().homes().removeUserHomes(onlineUser);

        // Update global lists
        if (plugin.getSettings().isCrossServer()) {
            plugin.getOnlineUsers().stream()
                    .filter(user -> !user.equals(onlineUser))
                    .findAny()
                    .ifPresent(this::sendPlayerListUpdates);
        }
    }

    private void sendPlayerListUpdates(@NotNull OnlineUser user) {
        Message.builder()
                .scope(Message.Scope.SERVER)
                .target(Message.TARGET_ALL)
                .payload(Payload.withStringList(plugin.getLocalPlayerList()))
                .build().send(plugin.getMessenger(), user);
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
                        Teleport.builder(plugin)
                                .teleporter(onlineUser)
                                .type(Teleport.Type.RESPAWN)
                                .target(respawnPosition)
                                .toTeleport()
                                .execute();
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

        plugin.runAsync(() -> plugin.getDatabase().getUserData(onlineUser.getUuid())
                .ifPresent(data -> plugin.getDatabase().setLastPosition(data.getUser(), sourcePosition)));
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
        plugin.log(Level.INFO, "Successfully disabled HuskHomes v" + plugin.getVersion());
    }

}