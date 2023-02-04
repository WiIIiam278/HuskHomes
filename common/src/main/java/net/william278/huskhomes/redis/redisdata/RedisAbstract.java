package net.william278.huskhomes.redis.redisdata;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.AllArgsConstructor;

import java.util.concurrent.*;
import java.util.function.Function;

@AllArgsConstructor
public abstract class RedisAbstract {

    protected static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final RoundRobinConnectionPool<String, Object> roundRobinConnectionPool;
    private final RoundRobinConnectionPool<String, String> roundRobinConnectionPoolString;
    protected RedisClient lettuceRedisClient;

    public RedisAbstract(RedisClient lettuceRedisClient) {
        this.lettuceRedisClient = lettuceRedisClient;
        this.roundRobinConnectionPool = new RoundRobinConnectionPool<>(() -> lettuceRedisClient.connect(new SerializedObjectCodec()), 5);
        this.roundRobinConnectionPoolString = new RoundRobinConnectionPool<>(lettuceRedisClient::connect, 2);
    }

    public <T> ScheduledFuture<T> scheduleConnection(Function<StatefulRedisConnection<String, Object>, T> function, int timeout, TimeUnit timeUnit) {
        return executorService.schedule(() -> function.apply(roundRobinConnectionPool.get()), timeout, timeUnit);
    }

//    public <T> CompletionStage<T> getConnectionAsync(Function<RedisAsyncCommands<String, Object>, CompletionStage<T>> redisCallBack) {
//        return redisCallBack.apply(roundRobinConnectionPool.get().async());
//    }

    public <T> CompletionStage<T> getBinaryConnectionAsync(Function<RedisAsyncCommands<String, Object>, CompletionStage<T>> redisCallBack) {
        return redisCallBack.apply(roundRobinConnectionPool.get().async());
    }

    public <T> CompletionStage<T> getBinaryConnection(Function<RedisAsyncCommands<String, Object>, CompletionStage<T>> redisCallBack) {
        return redisCallBack.apply(roundRobinConnectionPool.get().async());
    }

    public <T> CompletionStage<T> getConnectionAsync(Function<RedisAsyncCommands<String, String>, CompletionStage<T>> redisCallBack) {
        return redisCallBack.apply(roundRobinConnectionPoolString.get().async());
    }

    //Get pubsub
    public StatefulRedisPubSubConnection<String, Object> getBinaryPubSubConnection() {
        return lettuceRedisClient.connectPubSub(new SerializedObjectCodec());
    }
    public void getBinaryPubSubConnection(RedisCallBack.PubSub.Binary redisCallBack) {
        redisCallBack.useConnection(lettuceRedisClient.connectPubSub(new SerializedObjectCodec()));
    }

    public void getPubSubConnection(RedisCallBack.PubSub redisCallBack) {
        redisCallBack.useConnection(lettuceRedisClient.connectPubSub());
    }
//    public void getPubSubConnection(RedisCallBack.PubSub redisCallBack) {
//        try (StatefulRedisPubSubConnection<String, String> connection = lettuceRedisClient.connectPubSub()) {
//            redisCallBack.useConnection(connection);
//        }
//    }

    public StatefulRedisConnection<String, String> getUnclosedConnection() {
        return lettuceRedisClient.connect();
    }

    public boolean isConnected() {
        try (StatefulRedisConnection<String, String> connection = lettuceRedisClient.connect()){
            return connection.isOpen();
        } catch (Exception e) {
            return false;
        }
    }
    public void close() {
        lettuceRedisClient.shutdownAsync();
    }

}
