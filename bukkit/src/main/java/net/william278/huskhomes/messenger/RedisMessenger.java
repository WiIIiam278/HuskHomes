package net.william278.huskhomes.messenger;

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
                        final Message message = Message.fromJson(encodedMessage);
                        if (!message.clusterId.equals(clusterId)) {
                            return;
                        }
                        final Optional<BukkitPlayer> receiver = BukkitPlayer.get(message.targetPlayer);
                        if (receiver.isEmpty()) {
                            return;
                        }
                        handleMessage(receiver.get(), message);
                    }
                });
            }
        }, "Redis Subscriber").start();

    }

    @Override
    public CompletableFuture<Message> sendMessage(@NotNull OnlineUser sender, @NotNull Message message) {
        final CompletableFuture<Message> repliedMessage = new CompletableFuture<>();
        processingMessages.put(message.uuid, repliedMessage);
        redisWorker.sendMessage(message);
        return repliedMessage;
    }

    @Override
    protected void sendReply(@NotNull OnlineUser replier, @NotNull Message reply) {
        redisWorker.sendMessage(reply);
    }

    @Override
    public void terminate() {
        redisWorker.terminate();
        super.terminate();
    }
}
