package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.redis.RedisWorker;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for redis-based network messenger implementations
 */
public interface RedisMessenger {

    @NotNull
    HashMap<UUID, CompletableFuture<Message>> getProcessingMessages();

    @NotNull
    RedisWorker getRedisWorker();

    @NotNull
    String getNetworkMessagingChannel();

    void setRedisWorker(@NotNull RedisWorker redisWorker);

    default CompletableFuture<Message> dispatchMessage(@NotNull OnlineUser sender, @NotNull Message message) {
        final CompletableFuture<Message> repliedMessage = new CompletableFuture<>();
        getProcessingMessages().put(message.uuid, repliedMessage);
        getRedisWorker().sendMessage(message);
        return repliedMessage;
    }

    default void initializeRedis(@NotNull HuskHomes implementor) {
        setRedisWorker(new RedisWorker(implementor.getSettings()));
        getRedisWorker().initialize();

        new Thread(() -> {
            try (Jedis jedis = getRedisWorker().jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String encodedMessage) {
                        if (!channel.equals(getNetworkMessagingChannel())) {
                            return;
                        }
                        final Message message = Message.fromJson(encodedMessage);
                        if (!message.clusterId.equals(implementor.getSettings().clusterId)) {
                            return;
                        }
                        final Optional<OnlineUser> receiver = implementor.findOnlinePlayer(message.targetPlayer);
                        if (receiver.isEmpty()) {
                            return;
                        }
                        getNetworkMessenger().handleMessage(receiver.get(), message);
                    }
                }, getNetworkMessagingChannel());
            }
        }, "Redis Subscriber").start();
    }

    @NotNull
    NetworkMessenger getNetworkMessenger();

    default void terminateRedis() {
        getRedisWorker().terminate();
    }

}
