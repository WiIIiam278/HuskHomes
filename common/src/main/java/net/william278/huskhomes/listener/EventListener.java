/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.listener;

import de.themoep.minedown.adventure.MineDown;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.command.BackCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * A handler for when events take place.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EventListener {

    @NotNull
    private final HuskHomes plugin;

    public abstract void register();

    /**
     * Handle when a {@link OnlineUser} joins the server.
     *
     * @param onlineUser the joining {@link OnlineUser}
     */
    protected final void handlePlayerJoin(@NotNull OnlineUser onlineUser) {
        plugin.runAsync(() -> {
            // Ensure the user is in the database
            plugin.getDatabase().ensureUser(onlineUser);
            plugin.getCurrentlyOnWarmup().remove(onlineUser.getUuid());

            // Handle cross-server checks
            if (plugin.getSettings().getCrossServer().isEnabled()) {
                this.handleInboundTeleport(onlineUser);

                // Synchronize the global player list
                plugin.runSyncDelayed(() -> this.updateUserList(
                        onlineUser,
                        plugin.getOnlineUsers().stream().map(u -> (User) u).toList()),
                        onlineUser,
                        40L
                );

                // Request updated player lists from other servers
                if (plugin.getOnlineUsers().size() == 1) {
                    plugin.getManager().homes().updatePublicHomeCache();
                    plugin.getManager().warps().updateWarpCache();
                }
            }

            // Cache this user's homes
            plugin.getManager().homes().cacheUserHomes(onlineUser);

            // Set their ignoring requests state
            plugin.getDatabase().getUser(onlineUser.getUuid()).ifPresent(userData -> {
                plugin.getSavedUsers().add(userData);

                // Send a reminder message if they are still ignoring requests
                if (userData.isIgnoringTeleports()) {
                    plugin.getLocales().getRawLocale("tpignore_on_notification", plugin.getLocales()
                            .getRawLocale("tpignore_toggle_button")
                            .orElse("")).ifPresent(locale -> onlineUser.sendMessage(new MineDown(locale)));
                }
            });
        });
    }

    /**
     * Handle when a {@link OnlineUser} leaves the server.
     *
     * @param online the leaving {@link OnlineUser}
     */
    protected final void handlePlayerLeave(@NotNull OnlineUser online) {
        plugin.getOnlineUserMap().remove(online.getUuid());
        online.removeInvulnerabilityIfPermitted();

        plugin.runAsync(() -> {
           // Set offline position
            plugin.getDatabase().setOfflinePosition(online, online.getPosition());

            // Remove this user's home cache
            plugin.getManager().homes().removeUserHomes(online);

            // Update global lists
            if (plugin.getSettings().getCrossServer().isEnabled()) {
                final List<User> users = plugin.getOnlineUsers().stream().map(u -> (User) u).toList();
                if (plugin.getSettings().getCrossServer().getBrokerType() == Broker.Type.REDIS) {
                    this.updateUserList(online, users);
                    return;
                }

                plugin.getOnlineUsers().stream()
                        .filter(user -> !user.equals(online))
                        .findAny()
                        .ifPresent(player -> this.updateUserList(player, users));
            }
        });
    }

    /**
     * Handle inbound cross-server teleports.
     *
     * @param teleporter user to handle the checks for
     */
    private void handleInboundTeleport(@NotNull OnlineUser teleporter) {
        plugin.getDatabase().getCurrentTeleport(teleporter).ifPresent(teleport -> {
            if (teleport.getType() == Teleport.Type.RESPAWN) {
                handleInboundRespawn(teleporter);
                return;
            }

            try {
                teleporter.teleportLocally(
                        (Position) teleport.getTarget(),
                        plugin.getSettings().getGeneral().isTeleportAsync()
                );
            } catch (TeleportationException e) {
                e.displayMessage(teleporter);
            }
            plugin.getDatabase().clearCurrentTeleport(teleporter);
            teleport.displayTeleportingComplete(teleporter);
            teleporter.handleInvulnerability();
        });
    }

    /**
     * Handle an inbound global respawn.
     *
     * @param teleporter the user to handle the checks for
     */
    private void handleInboundRespawn(@NotNull OnlineUser teleporter) {
        final Optional<Position> bedPosition = teleporter.getBedSpawnPosition();
        if (bedPosition.isEmpty()) {
            plugin.getSpawn().ifPresent(spawn -> {
                if (plugin.getSettings().getCrossServer().isEnabled()
                    && !spawn.getServer().equals(plugin.getServerName())) {
                    plugin.runSyncDelayed(() -> Teleport.builder(plugin)
                            .teleporter(teleporter)
                            .target(spawn)
                            .updateLastPosition(false)
                            .buildAndComplete(false), teleporter, 40L);
                } else {
                    try {
                        teleporter.teleportLocally(spawn, plugin.getSettings().getGeneral().isTeleportAsync());
                    } catch (TeleportationException e) {
                        e.displayMessage(teleporter);
                    }
                }
                teleporter.sendTranslatableMessage("block.minecraft.spawn.not_valid");
            });
        } else {
            try {
                teleporter.teleportLocally(bedPosition.get(), plugin.getSettings().getGeneral().isTeleportAsync());
            } catch (TeleportationException e) {
                e.displayMessage(teleporter);
            }
        }
        plugin.getDatabase().clearCurrentTeleport(teleporter);
        plugin.getDatabase().setRespawnPosition(teleporter, bedPosition.orElse(null));
    }

    // Synchronize the global player list
    private void updateUserList(@NotNull OnlineUser user, @NotNull List<User> localPlayerList) {
        plugin.getBroker().ifPresent(broker -> {
            // Send this server's player list to all servers
            Message.builder()
                    .type(Message.MessageType.UPDATE_USER_LIST)
                    .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                    .payload(Payload.userList(localPlayerList))
                    .build().send(broker, user);

            // Clear cached global player lists and request updated lists from all servers
            if (plugin.getOnlineUsers().size() == 1) {
                plugin.getGlobalUserList().clear();
                Message.builder()
                        .type(Message.MessageType.REQUEST_USER_LIST)
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build().send(broker, user);
            }
        });
    }

    /**
     * Handle when a {@link OnlineUser} dies.
     *
     * @param onlineUser the {@link OnlineUser} who died
     */
    protected final void handlePlayerDeath(@NotNull OnlineUser onlineUser) {
        if (plugin.getSettings().getGeneral().getBackCommand().isReturnByDeath() && plugin.getCommand(BackCommand.class)
                .map(Command::getPermission).map(onlineUser::hasPermission).orElse(false)) {
            plugin.getDatabase().setLastPosition(onlineUser, onlineUser.getPosition());
        }
    }

    /**
     * Handle when a {@link OnlineUser} respawns after dying.
     *
     * @param onlineUser the respawning {@link OnlineUser}
     */
    protected final void handlePlayerRespawn(@NotNull OnlineUser onlineUser) {
        plugin.runAsync(() -> {
            // Display the return by death via /back notification
            final boolean canReturnByDeath = plugin.getCommand(BackCommand.class)
                    .map(command -> onlineUser.hasPermission(command.getPermission())
                                    && onlineUser.hasPermission(command.getPermission("death")))
                    .orElse(false);
            if (plugin.getSettings().getGeneral().getBackCommand().isReturnByDeath() && canReturnByDeath) {
                plugin.getLocales().getLocale("return_by_death_notification")
                        .ifPresent(onlineUser::sendMessage);
            }

            // Respawn the player via the global respawn system
            final Settings.CrossServerSettings crossServer = plugin.getSettings().getCrossServer();
            if (crossServer.isEnabled() && crossServer.isGlobalRespawning()) {
                this.respawnGlobally(onlineUser);
            }
        });
    }

    // Respawn a player to where they should be
    private void respawnGlobally(@NotNull OnlineUser onlineUser) {
        final TeleportBuilder builder = Teleport.builder(plugin)
                .teleporter(onlineUser)
                .type(Teleport.Type.RESPAWN)
                .updateLastPosition(false);

        plugin.getDatabase()
                .getRespawnPosition(onlineUser)
                .flatMap(pos -> {
                    if (pos.getServer().equals(plugin.getServerName()) && onlineUser.getBedSpawnPosition().isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(pos);
                })
                .or(() -> {
                    builder.type(Teleport.Type.TELEPORT);
                    return plugin.getSpawn();
                })
                .ifPresent(pos -> {
                    builder.target(pos);
                    try {
                        builder.toTeleport().execute();
                    } catch (TeleportationException e) {
                        e.displayMessage(onlineUser);
                    }
                });
    }

    /**
     * Handle when a player teleports.
     *
     * @param onlineUser     the {@link OnlineUser} who teleported
     * @param sourcePosition the source {@link Position} they came from
     */
    protected final void handlePlayerTeleport(@NotNull OnlineUser onlineUser, @NotNull Position sourcePosition) {
        if (!plugin.getSettings().getGeneral().getBackCommand().isSaveOnTeleportEvent()) {
            return;
        }

        plugin.runAsync(() -> plugin.getDatabase().getUser(onlineUser.getUuid())
                .ifPresent(data -> plugin.getDatabase().setLastPosition(data.getUser(), sourcePosition)));
    }

    /**
     * Handle when an {@link OnlineUser}'s spawn point is updated.
     *
     * @param onlineUser the {@link OnlineUser} whose spawn point was updated
     * @param position   the new spawn point
     */
    protected final void handlePlayerUpdateSpawnPoint(@NotNull OnlineUser onlineUser, @NotNull Position position) {
        if (!plugin.getSettings().getGeneral().isAlwaysRespawnAtSpawn()
            && plugin.getSettings().getCrossServer().isEnabled()
            && plugin.getSettings().getCrossServer().isGlobalRespawning()) {
            plugin.getDatabase().setRespawnPosition(onlineUser, position);
        }
    }

    @NotNull
    protected HuskHomes getPlugin() {
        return plugin;
    }

}
