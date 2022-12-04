package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.redis.RedisWorker;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Redis (Jedis) messenger implementation
 */
public class RedisMessenger extends PluginMessenger {

    /**
     * The {@link RedisWorker} that will manage the connection to the Redis database via Jedis
     */
    private RedisWorker redisWorker;

    @Override
    public void initialize(@NotNull HuskHomes implementor) {
        super.initialize(implementor);

        redisWorker = new RedisWorker(implementor.getSettings());
        redisWorker.initialize();

        new Thread(() -> {
            try (Jedis jedis = redisWorker.jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String encodedMessage) {
                        if (!channel.equals(NETWORK_MESSAGE_CHANNEL)) {
                            return;
                        }
                        final Request request = Request.fromJson(encodedMessage);
                        if (!request.getClusterId().equals(clusterId)) {
                            return;
                        }
                        final Optional<BukkitPlayer> receiver = BukkitPlayer.get(request.getTargetPlayer());
                        if (receiver.isEmpty()) {
                            return;
                        }
                        handleMessage(receiver.get(), request);
                    }
                }, NETWORK_MESSAGE_CHANNEL);
            }
        }, "Redis Subscriber").start();

    }

    @Override
    public CompletableFuture<Request> dispatchMessage(@NotNull OnlineUser sender, @NotNull Request request) {
        final CompletableFuture<Request> repliedMessage = new CompletableFuture<>();
        processingMessages.put(request.getUuid(), repliedMessage);
        redisWorker.sendMessage(request);
        return repliedMessage;
    }

    @Override
    protected void sendReply(@NotNull OnlineUser replier, @NotNull Request reply) {
        redisWorker.sendMessage(reply);
    }

    @Override
    public void close() {
        redisWorker.terminate();
        super.close();
    }
}
