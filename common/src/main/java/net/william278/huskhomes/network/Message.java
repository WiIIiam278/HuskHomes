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
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a message sent by a {@link Broker} cross-server. See {@link #builder()} for
 * a builder to create a message.
 */
public class Message {

    /**
     * Message target indicating all players.
     */
    public static final String TARGET_ALL = "ALL";

    @Expose
    private UUID id;
    @Expose
    private Type type;
    @Expose
    private Scope scope;
    @Expose
    private String target;
    @Expose
    private Payload payload;
    @Expose
    private String sender;
    @Expose
    @SerializedName("source_server")
    private String sourceServer;

    private Message(@NotNull Type type, @NotNull Scope scope, @NotNull String target, @NotNull Payload payload) {
        this.type = type;
        this.scope = scope;
        this.target = target;
        this.payload = payload;
        this.id = UUID.randomUUID();
    }

    @SuppressWarnings("unused")
    private Message() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public void send(@NotNull Broker broker, @NotNull OnlineUser sender) {
        this.sender = sender.getName();
        this.sourceServer = broker.getServer();
        broker.send(this, sender);
    }

    public void send(@NotNull Broker broker, @NotNull String sender) {
        this.sender = sender;
        this.sourceServer = broker.getServer();
        broker.send(this);
    }


    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public Scope getScope() {
        return scope;
    }

    @NotNull
    public String getTarget() {
        return target;
    }

    @NotNull
    public Payload getPayload() {
        return payload;
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    @NotNull
    public String getSourceServer() {
        return sourceServer;
    }

    @NotNull
    public UUID getUuid() {
        return id;
    }

    /**
     * Builder for {@link Message}s.
     */
    public static class Builder {
        private Type type;
        private Scope scope = Scope.PLAYER;
        private Payload payload = Payload.empty();
        private String target;

        private Builder() {
        }

        @NotNull
        public Builder type(@NotNull Type type) {
            this.type = type;
            return this;
        }

        @NotNull
        public Builder scope(@NotNull Scope scope) {
            this.scope = scope;
            return this;
        }

        @NotNull
        public Builder payload(@NotNull Payload payload) {
            this.payload = payload;
            return this;
        }

        @NotNull
        public Builder target(@NotNull String target) {
            this.target = target;
            return this;
        }

        @NotNull
        public Message build() {
            if (type == null) {
                throw new IllegalStateException("Message type must be set");
            }
            if (target == null) {
                throw new IllegalStateException("Message target must be set");
            }
            return new Message(type, scope, target, payload);
        }

    }

    /**
     * Different types of cross-server messages.
     */
    public enum Type {
        TELEPORT_TO_POSITION,
        TELEPORT_TO_NETWORKED_POSITION,
        TELEPORT_REQUEST,
        TELEPORT_TO_NETWORKED_USER,
        TELEPORT_REQUEST_RESPONSE,
        REQUEST_PLAYER_LIST,
        PLAYER_LIST,
        UPDATE_HOME,
        UPDATE_WARP,
        UPDATE_CACHES,
        REQUEST_RTP_LOCATION,
        RTP_LOCATION,
    }

    public enum Scope {
        /**
         * The target is a server name, or "all" to indicate all servers.
         */
        SERVER("Forward"),
        /**
         * The target is a player name, or "all" to indicate all players.
         */
        PLAYER("ForwardToPlayer");

        private final String pluginMessageChannel;

        Scope(@NotNull String pluginMessageChannel) {
            this.pluginMessageChannel = pluginMessageChannel;
        }

        @NotNull
        public String getPluginMessageChannel() {
            return pluginMessageChannel;
        }
    }

}