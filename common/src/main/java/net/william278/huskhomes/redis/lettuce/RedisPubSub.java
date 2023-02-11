package net.william278.huskhomes.redis.lettuce;

import io.lettuce.core.pubsub.RedisPubSubListener;

public abstract class RedisPubSub<K,V> implements RedisPubSubListener<K,V> {
    public abstract void message(K channel, V message);


    @Override
    public void message(K pattern, K channel, V message){

    }


    @Override
    public void subscribed(K channel, long count){

    }

    @Override
    public void psubscribed(K pattern, long count){

    }

    @Override
    public void unsubscribed(K channel, long count){

    }

    @Override
    public void punsubscribed(K pattern, long count){

    }
}
