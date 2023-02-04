package net.william278.huskhomes.redis;

import io.lettuce.core.RedisClient;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.network.Messenger;
import net.william278.huskhomes.network.Request;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Handles Redis database pub/sub network messaging requests over Jedis
 */
public class RedisWorker {

    public String host;

    public int port;

    public String password;

    public boolean ssl;

    private RedisImpl redisImpl;

    public RedisWorker(@NotNull Settings settings) {
        this.host = settings.redisHost;
        this.port = settings.redisPort;
        this.password = settings.redisPassword != null ? settings.redisPassword : "";
        this.ssl = settings.redisUseSsl && !(this.host.equalsIgnoreCase("localhost")
                                             || this.host.equalsIgnoreCase("127.0.0.1"));
    }

    /**
     * Initialize the JedisPool connection
     */
    public void initialize() {
        RedisClient client;
        if (password.isEmpty()) {
            client = RedisClient.create("redis://" + host + ":" + port);
        } else {
            client = RedisClient.create("redis://" + password + "@" + host + ":" + port);
        }
        redisImpl = new RedisImpl(client);
    }

    public CompletableFuture<Void> sendMessage(@NotNull Request request) {
        return CompletableFuture.runAsync(() -> {
            redisImpl.getConnectionAsync(c -> c.publish(Messenger.NETWORK_MESSAGE_CHANNEL, request.toJson()));

        });
    }

    /**
     * Close the JedisPool connection
     */
    public void terminate() {
        redisImpl.close();
    }

    public RedisImpl getRedisImpl() {
        return redisImpl;
    }
}
