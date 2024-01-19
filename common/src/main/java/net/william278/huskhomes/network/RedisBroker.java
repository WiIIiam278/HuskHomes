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
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

/**
 * Redis PubSub broker implementation.
 */
public class RedisBroker extends PluginMessageBroker {
    private JedisPool jedisPool;

    private boolean enabled;
    private final PubSub pubSub = new PubSub();

    public RedisBroker(@NotNull HuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws IllegalStateException {
        super.initialize();

        final String password = plugin.getSettings().getRedisPassword();
        final String host = plugin.getSettings().getRedisHost();
        final int port = plugin.getSettings().getRedisPort();
        final boolean useSSL = plugin.getSettings().useRedisSsl();

        this.jedisPool = password.isEmpty() ? new JedisPool(new JedisPoolConfig(), host, port, 0, useSSL)
                : new JedisPool(new JedisPoolConfig(), host, port, 0, password, useSSL);

        enabled = true;
        new Thread(getSubscriber(), plugin.getKey("redis_subscriber").toString()).start();

        plugin.log(Level.INFO, "Initialized Redis connection pool");
    }

    @NotNull
    private Runnable getSubscriber() {
        return () -> {
            boolean reconnected = false;
            while (enabled && !Thread.interrupted() && jedisPool != null && !jedisPool.isClosed()) {
                try (Jedis jedis = jedisPool.getResource()) {
                    if (reconnected) {
                        plugin.log(Level.INFO, "Redis connection is alive again");
                    }
                    // Subscribe channels and lock the thread
                    jedis.subscribe(pubSub, getSubChannelId());
                } catch (Throwable t) {
                    // Thread was unlocked due error
                    if (enabled) {
                        if (reconnected) {
                            plugin.log(Level.WARNING,
                                    "Redis connection dropped, automatic reconnection in 8 seconds", t);
                        }
                        try {
                            pubSub.unsubscribe();
                        } catch (Throwable ignored) {
                            // empty catch
                        }

                        // Make an instant subscribe if ocurrs any error on initialization
                        if (!reconnected) {
                            reconnected = true;
                        } else {
                            try {
                                Thread.sleep(8000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        };
    }

    @Override
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        plugin.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(getSubChannelId(), plugin.getGson().toJson(message));
            }
        });
    }

    @Override
    public void close() {
        enabled = false;
        super.close();
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    public class PubSub extends JedisPubSub {
        @Override
        public void onMessage(@NotNull String channel, @NotNull String encodedMessage) {
            if (!channel.equals(getSubChannelId())) {
                return;
            }

            final Message message;
            try {
                message = plugin.getGson().fromJson(encodedMessage, Message.class);
            } catch (Exception e) {
                plugin.log(Level.WARNING, "Failed to decode message from Redis: " + e.getMessage());
                return;
            }

            if (message.getScope() == Message.Scope.PLAYER) {
                plugin.getOnlineUsers().stream()
                        .filter(online -> message.getTarget().equals(Message.TARGET_ALL)
                                || online.getUsername().equals(message.getTarget()))
                        .forEach(receiver -> handle(receiver, message));
                return;
            }

            if (message.getTarget().equals(plugin.getServerName())
                    || message.getTarget().equals(Message.TARGET_ALL)) {
                plugin.getOnlineUsers().stream()
                        .findAny()
                        .ifPresent(receiver -> handle(receiver, message));
            }
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            plugin.log(Level.INFO, "Redis subscribed to channel '" + channel + "'");
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            plugin.log(Level.INFO, "Redis unsubscribed from channel '" + channel + "'");
        }
    }

}