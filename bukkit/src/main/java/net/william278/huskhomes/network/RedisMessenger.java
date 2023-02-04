package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.redis.RedisWorker;
import net.william278.huskhomes.redis.redisdata.RedisPubSub;
import org.jetbrains.annotations.NotNull;

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

        CompletableFuture.runAsync(() -> {
            redisWorker.getRedisImpl().getPubSubConnection(connection -> {


                connection.addListener(new RedisPubSub<>() {
                    @Override
                    public void message(String channel, String encodedMessage) {
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
                });

                connection.async().subscribe(NETWORK_MESSAGE_CHANNEL);
            });
        });

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
