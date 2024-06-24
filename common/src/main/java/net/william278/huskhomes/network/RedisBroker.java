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

import lombok.AllArgsConstructor;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static net.william278.huskhomes.config.Settings.CrossServerSettings.RedisSettings;

/**
 * Redis PubSub broker implementation.
 */
public class RedisBroker extends PluginMessageBroker {

    private final Subscriber subscriber;

    public RedisBroker(@NotNull HuskHomes plugin) {
        super(plugin);
        this.subscriber = new Subscriber(this, getSubChannelId());
    }

    @Blocking
    @Override
    public void initialize() throws IllegalStateException {
        // Initialize plugin message channels
        super.initialize();

        // Establish a connection with the Redis server
        final Pool<Jedis> jedisPool = getJedisPool(plugin.getSettings().getCrossServer().getRedis());
        try {
            jedisPool.getResource().ping();
        } catch (JedisException e) {
            throw new IllegalStateException("Failed to establish connection with Redis. "
                    + "Please check the supplied credentials in the config file", e);
        }

        // Subscribe using a thread (rather than a task)
        subscriber.enable(jedisPool);
        new Thread(subscriber::subscribe, "huskhomes:redis_subscriber").start();
    }

    @NotNull
    private static Pool<Jedis> getJedisPool(@NotNull RedisSettings settings) {
        // Get the Redis connection settings
        final String password = settings.getPassword();
        final String host = settings.getHost();
        final int port = settings.getPort();
        final boolean useSSL = settings.isUseSsl();

        // Create the jedis pool
        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(0);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        // Check if sentinels are to be used
        final RedisSettings.SentinelSettings sentinel = settings.getSentinel();
        Set<String> redisSentinelNodes = new HashSet<>(sentinel.getNodes());
        if (!redisSentinelNodes.isEmpty()) {
            final String sentinelPassword = sentinel.getPassword();
            return new JedisSentinelPool(sentinel.getMasterName(), redisSentinelNodes, password.isEmpty()
                    ? null : password, sentinelPassword.isEmpty() ? null : sentinelPassword);
        }

        // Otherwise, use the standard Jedis pool
        return password.isEmpty()
                ? new JedisPool(config, host, port, 0, useSSL)
                : new JedisPool(config, host, port, 0, password, useSSL);
    }

    @Override
    protected void send(@NotNull Message message, @NotNull OnlineUser sender) {
        plugin.runAsync(() -> subscriber.send(message));
    }

    @Override
    protected void send(@NotNull Message message) {
        plugin.runAsync(() -> subscriber.send(message));
    }

    @Override
    @Blocking
    public void close() {
        super.close();
        subscriber.disable();
    }


    @AllArgsConstructor
    private static class Subscriber extends JedisPubSub {
        private static final int RECONNECTION_TIME = 8000;

        private final RedisBroker broker;
        private final String channel;

        private Pool<Jedis> jedisPool;
        private boolean enabled;
        private boolean reconnected;

        private Subscriber(@NotNull RedisBroker broker, @NotNull String channel) {
            this.broker = broker;
            this.channel = channel;
        }

        private void enable(@NotNull Pool<Jedis> jedisPool) {
            this.jedisPool = jedisPool;
            this.enabled = true;
        }

        @Blocking
        private void disable() {
            this.enabled = false;
            if (jedisPool != null && !jedisPool.isClosed()) {
                jedisPool.close();
            }
            this.unsubscribe();
        }

        @Blocking
        public void send(@NotNull Message message) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(channel, broker.plugin.getGson().toJson(message));
            }
        }

        @Blocking
        private void subscribe() {
            while (enabled && !Thread.interrupted() && jedisPool != null && !jedisPool.isClosed()) {
                try (Jedis jedis = jedisPool.getResource()) {
                    if (reconnected) {
                        broker.plugin.log(Level.INFO, "Redis connection is alive again");
                    }

                    // Subscribe to channel and lock the thread
                    jedis.subscribe(this, channel);
                } catch (Throwable t) {
                    // Thread was unlocked due error
                    onThreadUnlock(t);
                }
            }
        }

        private void onThreadUnlock(@NotNull Throwable t) {
            if (!enabled) {
                return;
            }

            if (reconnected) {
                broker.plugin.log(Level.WARNING, "Redis Server connection lost. Attempting reconnect in %ss..."
                        .formatted(RECONNECTION_TIME / 1000), t);
            }
            try {
                this.unsubscribe();
            } catch (Throwable ignored) {
                // empty catch
            }

            // Make an instant subscribe if occurs any error on initialization
            if (!reconnected) {
                reconnected = true;
            } else {
                try {
                    Thread.sleep(RECONNECTION_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public void onMessage(@NotNull String channel, @NotNull String encoded) {
            final Message message;
            try {
                message = broker.plugin.getGson().fromJson(encoded, Message.class);
            } catch (Exception e) {
                broker.plugin.log(Level.WARNING, "Failed to decode message from Redis: " + e.getMessage());
                return;
            }

            if (message.getType() == Message.Type.REQUEST_RTP_LOCATION) {
                broker.handleRTPRequest(message);
                return;
            }

            if (message.getScope() == Message.Scope.PLAYER) {
                broker.plugin.getOnlineUsers().stream()
                        .filter(online -> message.getTarget().equals(Message.TARGET_ALL)
                                || online.getUsername().equals(message.getTarget()))
                        .forEach(receiver -> broker.handle(receiver, message));
                return;
            }

            if (message.getTarget().equals(broker.plugin.getServerName())
                    || message.getTarget().equals(Message.TARGET_ALL)) {
                broker.plugin.getOnlineUsers().stream()
                        .findAny()
                        .ifPresent(receiver -> broker.handle(receiver, message));
            }
        }
    }

}