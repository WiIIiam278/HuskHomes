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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a message sent by a {@link Broker} cross-server. See {@link #builder()} for
 * a builder to create a message.
 */
@Getter
@NoArgsConstructor
public class Message {

    public static final String TARGET_ALL = "ALL";

    @NotNull
    @Expose
    private MessageType type;
    @NotNull
    @Expose
    @SerializedName("target_type")
    private TargetType targetType;
    @NotNull
    @Expose
    private String target;
    @NotNull
    @Expose
    private Payload payload;
    @NotNull
    @Expose
    private String sender;
    @NotNull
    @Expose
    @SerializedName("source_server")
    private String sourceServer;

    private Message(@NotNull MessageType type, @NotNull String target, @NotNull TargetType targetType,
                    @NotNull Payload payload) {
        this.type = type;
        this.target = target;
        this.targetType = targetType;
        this.payload = payload;
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    public void send(@NotNull Broker broker, @Nullable OnlineUser sender) {
        this.sender = sender != null ? sender.getName() : broker.getServer();
        this.sourceServer = broker.getServer();
        broker.send(this, sender);
    }

    /**
     * Builder for {@link Message}s
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private MessageType type;
        private Payload payload = Payload.empty();
        private TargetType targetType = TargetType.PLAYER;
        private String target;

        @NotNull
        public Builder type(@NotNull MessageType type) {
            this.type = type;
            return this;
        }

        @NotNull
        public Builder payload(@NotNull Payload payload) {
            this.payload = payload;
            return this;
        }

        @NotNull
        public Builder target(@NotNull String target, @NotNull TargetType targetType) {
            this.target = target;
            this.targetType = targetType;
            return this;
        }

        @NotNull
        public Message build() {
            if (target == null || type == null) {
                throw new IllegalStateException("Message not fully built. Type: " + type + ", Target: " + target);
            }
            return new Message(type, target, targetType, payload);
        }

    }

    /**
     * Type of targets messages can be sent to
     *
     * @since 4.8
     */
    public enum TargetType {
        SERVER("Forward"),
        PLAYER("ForwardToPlayer");

        private final String pluginMessageChannel;

        TargetType(@NotNull String pluginMessageChannel) {
            this.pluginMessageChannel = pluginMessageChannel;
        }

        @NotNull
        public String getPluginMessageChannel() {
            return pluginMessageChannel;
        }
    }

    /**
     * Different types of cross-server messages
     *
     * @since 4.8
     */
    public enum MessageType {
        TELEPORT_TO_POSITION,
        TELEPORT_TO_NETWORKED_POSITION,
        TELEPORT_REQUEST,
        TELEPORT_TO_NETWORKED_USER,
        TELEPORT_REQUEST_RESPONSE,
        REQUEST_USER_LIST,
        UPDATE_USER_LIST,
        UPDATE_HOME,
        UPDATE_WARP,
        UPDATE_CACHES,
        REQUEST_RTP_LOCATION,
        RTP_LOCATION,
    }

}