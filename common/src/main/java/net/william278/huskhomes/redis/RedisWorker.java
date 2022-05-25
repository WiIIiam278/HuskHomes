package net.william278.huskhomes.redis;

import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.messenger.Message;
import net.william278.huskhomes.messenger.NetworkMessenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.CompletableFuture;

/**
 * Handles Redis database pub/sub network messaging requests over Jedis
 */
public class RedisWorker {

    @NotNull
    private final String redisHost;
    private final int redisPort;
    @Nullable
    private final String redisPassword;
    private final boolean redisSsl;
    public JedisPool jedisPool;

    public RedisWorker(@NotNull Settings settings) {
        this.redisHost = settings.getStringValue(Settings.ConfigOption.REDIS_HOST);
        this.redisPort = settings.getIntegerValue(Settings.ConfigOption.REDIS_PORT);
        this.redisPassword = settings.getStringValue(Settings.ConfigOption.REDIS_PASSWORD).isEmpty() ? null : settings.getStringValue(Settings.ConfigOption.REDIS_PASSWORD);
        this.redisSsl = !redisHost.equalsIgnoreCase("localhost") && !redisHost.equalsIgnoreCase("127.0.0.1") && settings.getBooleanValue(Settings.ConfigOption.REDIS_USE_SSL);
    }

    /**
     * Initialize the JedisPool connection
     */
    public void initialize() {
        if (redisPassword == null) {
            jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 0, redisSsl);
        } else {
            jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 0, redisPassword, redisSsl);
        }
    }

    public CompletableFuture<Void> sendMessage(@NotNull Message message) {
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(NetworkMessenger.NETWORK_MESSAGE_CHANNEL, message.toJson());
            }
        });
    }

    /**
     * Close the JedisPool connection
     */
    public void terminate() {
        if (jedisPool != null) {
            if (!jedisPool.isClosed()) {
                jedisPool.close();
            }
        }
    }
}
