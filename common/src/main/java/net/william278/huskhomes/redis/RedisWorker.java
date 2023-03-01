package net.william278.huskhomes.redis;

import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.network.Request;
import net.william278.huskhomes.network.Messenger;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.CompletableFuture;

/**
 * Handles Redis database pub/sub network messaging requests over Jedis
 */
public class RedisWorker {

    public String host;

    public int port;

    public String password;

    public boolean ssl;
    public JedisPool jedisPool;

    public RedisWorker(@NotNull Settings settings) {
        this.host = settings.getRedisHost();
        this.port = settings.getRedisPort();
        this.password = settings.getRedisPassword() != null ? settings.getRedisPassword() : "";
        this.ssl = settings.isRedisUseSsl() && !(this.host.equalsIgnoreCase("localhost")
                                             || this.host.equalsIgnoreCase("127.0.0.1"));
    }

    /**
     * Initialize the JedisPool connection
     */
    public void initialize() {
        if (password.isEmpty()) {
            jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 0, ssl);
        } else {
            jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 0, password, ssl);
        }
    }

    public CompletableFuture<Void> sendMessage(@NotNull Request request) {
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(Messenger.NETWORK_MESSAGE_CHANNEL, request.toJson());
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
