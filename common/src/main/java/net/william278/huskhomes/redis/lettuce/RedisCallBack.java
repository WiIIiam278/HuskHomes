package net.william278.huskhomes.redis.lettuce;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

@FunctionalInterface
public interface RedisCallBack<R> {
    R useConnection(StatefulRedisConnection<String, String> connection);

    @FunctionalInterface
    interface Binary<T> {
        T useBinaryConnection(StatefulRedisConnection<String, Object> connection);
    }

    @FunctionalInterface
    interface PubSub {
        void useConnection(StatefulRedisPubSubConnection<String, String> connection);
        @FunctionalInterface
        interface Binary {
            void useConnection(StatefulRedisPubSubConnection<String, Object> connection);
        }
    }

}
