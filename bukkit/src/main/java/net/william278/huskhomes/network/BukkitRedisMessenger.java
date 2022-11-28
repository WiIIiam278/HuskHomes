package net.william278.huskhomes.network;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.redis.RedisWorker;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Redis (Jedis) messenger implementation
 */
public class BukkitRedisMessenger extends BukkitPluginMessenger implements RedisMessenger {

    /**
     * The {@link RedisWorker} that will manage the connection to the Redis database via Jedis
     */
    private RedisWorker redisWorker;

    @Override
    public void initialize(@NotNull HuskHomes implementor) {
        super.initialize(implementor);
        this.initializeRedis(implementor);
    }

    @Override
    @NotNull
    public HashMap<UUID, CompletableFuture<Message>> getProcessingMessages() {
        return processingMessages;
    }

    @Override
    @NotNull
    public RedisWorker getRedisWorker() {
        return redisWorker;
    }

    @Override
    @NotNull
    public String getNetworkMessagingChannel() {
        return NETWORK_MESSAGE_CHANNEL;
    }

    @Override
    public void setRedisWorker(@NotNull RedisWorker redisWorker) {
        this.redisWorker = redisWorker;
    }

    @Override
    @NotNull
    public NetworkMessenger getNetworkMessenger() {
        return this;
    }

    @Override
    public void sendReply(@NotNull OnlineUser replier, @NotNull Message reply) {
        this.getRedisWorker().sendMessage(reply);
    }

    @Override
    public void terminate() {
        this.terminateRedis();
        super.terminate();
    }
}
