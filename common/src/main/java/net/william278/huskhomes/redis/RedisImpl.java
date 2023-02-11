package net.william278.huskhomes.redis;


import io.lettuce.core.RedisClient;
import net.william278.huskhomes.redis.lettuce.RedisBase;


public class RedisImpl extends RedisBase {

    public RedisImpl(RedisClient lettuceRedisClient) {
        super(lettuceRedisClient);
    }


}
