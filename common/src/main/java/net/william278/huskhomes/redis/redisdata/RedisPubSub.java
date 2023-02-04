package net.william278.huskhomes.redis.redisdata;

import io.lettuce.core.pubsub.RedisPubSubListener;

public abstract class RedisPubSub<K,V> implements RedisPubSubListener<K,V> {
    public abstract void message(K channel, V message);

    /**
     * Message received from a pattern subscription.
     *
     * @param pattern Pattern
     * @param channel Channel
     * @param message Message
     */
    public void message(K pattern, K channel, V message){

    }

    /**
     * Subscribed to a channel.
     *
     * @param channel Channel
     * @param count Subscription count.
     */
    public void subscribed(K channel, long count){

    }

    /**
     * Subscribed to a pattern.
     *
     * @param pattern Pattern.
     * @param count Subscription count.
     */
    public void psubscribed(K pattern, long count){

    }

    /**
     * Unsubscribed from a channel.
     *
     * @param channel Channel
     * @param count Subscription count.
     */
    public void unsubscribed(K channel, long count){

    }

    /**
     * Unsubscribed from a pattern.
     *
     * @param pattern Channel
     * @param count Subscription count.
     */
    public void punsubscribed(K pattern, long count){

    }
}
