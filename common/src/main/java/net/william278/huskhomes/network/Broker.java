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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Broker implements MessageHandler {

    protected final HuskHomes plugin;

    /**
     * Handle an inbound {@link Message}.
     *
     * @param receiver The user who received the message if a receiver exists
     * @param message  The message
     */
    protected void handle(@NotNull OnlineUser receiver, @NotNull Message message) {
        if (message.getSourceServer().equals(getServer())) {
            return;
        }
        switch (message.getType()) {
            case REQUEST_USER_LIST -> handleRequestUserList(message, receiver);
            case UPDATE_USER_LIST -> handleUpdateUserList(message);
            case TELEPORT_TO_POSITION -> handleTeleportToPosition(message, receiver);
            case TELEPORT_TO_NETWORKED_POSITION -> handleTeleportToNetworkedPosition(message, receiver);
            case TELEPORT_TO_NETWORKED_USER -> handleTeleportToNetworkedUser(message, receiver);
            case TELEPORT_REQUEST -> handleTeleportRequest(message, receiver);
            case TELEPORT_REQUEST_RESPONSE -> handleTeleportRequestResponse(message, receiver);
            case UPDATE_HOME -> handleUpdateHome(message, receiver);
            case UPDATE_WARP -> handleUpdateWarp(message, receiver);
            case UPDATE_CACHES -> handleUpdateCaches();
            case RTP_LOCATION -> handleRtpLocation(message, receiver);
            default -> plugin.log(Level.SEVERE, "Received unknown message type: " + message.getType());
        }
    }

    /**
     * Initialize the message broker
     *
     * @throws RuntimeException if the broker fails to initialize
     */
    public abstract void initialize() throws RuntimeException;

    /**
     * Send a message to the broker
     *
     * @param message the message to send
     * @param sender  the sender of the message
     */
    protected abstract void send(@NotNull Message message, @Nullable OnlineUser sender);

    /**
     * Terminate the broker
     */
    public abstract void close();

    /**
     * Get the sub-channel ID for broker communications
     *
     * @return the sub-channel ID
     * @since 1.0
     */
    @NotNull
    protected String getSubChannelId() {
        return plugin.getKey(plugin.getSettings().getCrossServer().getClusterId(), getFormattedVersion()).asString();
    }

    /**
     * Return the server name
     *
     * @return the server name
     * @since 1.0
     */
    protected String getServer() {
        return plugin.getServerName();
    }

    // Returns the formatted version of the plugin (format: x.x)
    @NotNull
    private String getFormattedVersion() {
        return String.format("%s.%s", plugin.getPluginVersion().getMajor(), plugin.getPluginVersion().getMinor());
    }

    // Return this broker instance
    @NotNull
    @Override
    public Broker getBroker() {
        return this;
    }

    /**
     * Identifies types of message brokers
     */
    @Getter
    public enum Type {
        PLUGIN_MESSAGE("Plugin Messages"),
        REDIS("Redis"),
        CUSTOM("Custom");

        @NotNull
        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

}