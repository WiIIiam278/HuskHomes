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

package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public abstract class Broker {

    protected final HuskHomes plugin;

    /**
     * Create a new broker
     *
     * @param plugin the HuskHomes plugin instance
     */
    protected Broker(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle an inbound {@link Message}
     *
     * @param receiver The user who received the message, if a receiver exists
     * @param message  The message
     */
    protected void handle(@NotNull OnlineUser receiver, @NotNull Message message) {
        if (message.getSourceServer().equals(getServer())) {
            return;
        }
        switch (message.getType()) {
            case TELEPORT_TO_POSITION -> message.getPayload()
                    .getPosition().ifPresent(position -> {
                        try {
                            Teleport.builder(plugin)
                                    .teleporter(receiver)
                                    .target(position)
                                    .toTeleport()
                                    .execute();
                        } catch (TeleportationException e) {
                            e.displayMessage(plugin.getConsole());
                        }
                    });
            case TELEPORT_TO_NETWORKED_POSITION -> Message.builder()
                    .type(Message.Type.TELEPORT_TO_POSITION)
                    .target(message.getSender())
                    .payload(Payload.withPosition(receiver.getPosition()))
                    .build().send(this, receiver);
            case TELEPORT_TO_NETWORKED_USER -> message.getPayload()
                    .getString().ifPresent(target -> Message.builder()
                            .type(Message.Type.TELEPORT_TO_NETWORKED_POSITION)
                            .target(target)
                            .build().send(this, receiver));
            case TELEPORT_REQUEST -> message.getPayload()
                    .getTeleportRequest()
                    .ifPresent(teleportRequest -> plugin.getManager().requests()
                            .sendLocalTeleportRequest(teleportRequest, receiver));
            case TELEPORT_REQUEST_RESPONSE -> message.getPayload()
                    .getTeleportRequest()
                    .ifPresent(teleportRequest -> plugin.getManager().requests()
                            .handleLocalRequestResponse(receiver, teleportRequest));
            case REQUEST_PLAYER_LIST -> Message.builder()
                    .type(Message.Type.PLAYER_LIST)
                    .scope(Message.Scope.SERVER)
                    .target(message.getSourceServer())
                    .payload(Payload.withStringList(plugin.getLocalPlayerList()))
                    .build().send(this, receiver);
            case PLAYER_LIST -> message.getPayload()
                    .getStringList()
                    .ifPresent(players -> plugin.setPlayerList(message.getSourceServer(), players));
            case UPDATE_HOME -> message.getPayload().getString()
                    .map(UUID::fromString)
                    .ifPresent(homeId -> {
                        final Optional<Home> optionalHome = plugin.getDatabase().getHome(homeId);
                        if (optionalHome.isPresent()) {
                            plugin.getManager().homes().cacheHome(optionalHome.get(), false);
                        } else {
                            plugin.getManager().homes().unCacheHome(homeId, false);
                        }
                    });
            case UPDATE_WARP -> message.getPayload().getString()
                    .map(UUID::fromString)
                    .ifPresent(warpId -> {
                        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(warpId);
                        if (optionalWarp.isPresent()) {
                            plugin.getManager().warps().cacheWarp(optionalWarp.get(), false);
                        } else {
                            plugin.getManager().warps().unCacheWarp(warpId, false);
                        }
                    });
            case UPDATE_CACHES -> {
                plugin.getManager().homes().updatePublicHomeCache();
                plugin.getManager().warps().updateWarpCache();
            }
        }
    }

    /**
     * Initialize the message broker
     *
     * @throws RuntimeException if the broker fails to initialize
     */
    public abstract void initialize() throws IllegalStateException;

    /**
     * Send a message to the broker
     *
     * @param message the message to send
     * @param sender  the sender of the message
     */
    protected abstract void send(@NotNull Message message, @NotNull OnlineUser sender);

    /**
     * Move an {@link OnlineUser} to a new server on the proxy network
     *
     * @param user   the user to move
     * @param server the server to move the user to
     */
    public abstract void changeServer(@NotNull OnlineUser user, @NotNull String server);

    /**
     * Terminate the broker
     */
    public abstract void close();

    @NotNull
    protected String getSubChannelId() {
        final String version = plugin.getVersion().getMajor() + "." + plugin.getVersion().getMinor();
        return plugin.getKey(plugin.getSettings().getClusterId(), version).asString();
    }

    @NotNull
    protected String getServer() {
        return plugin.getServerName();
    }

    /**
     * Identifies types of message brokers
     */
    public enum Type {
        PLUGIN_MESSAGE("Plugin Messages"),
        REDIS("Redis");
        @NotNull
        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @NotNull
        public String getDisplayName() {
            return displayName;
        }
    }

}