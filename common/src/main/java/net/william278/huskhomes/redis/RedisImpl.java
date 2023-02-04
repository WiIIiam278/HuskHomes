package net.william278.huskhomes.redis;


import io.lettuce.core.RedisClient;
import net.william278.huskhomes.redis.redisdata.RedisAbstract;


public class RedisImpl extends RedisAbstract {

    public RedisImpl(RedisClient lettuceRedisClient) {
        super(lettuceRedisClient);
    }


}
