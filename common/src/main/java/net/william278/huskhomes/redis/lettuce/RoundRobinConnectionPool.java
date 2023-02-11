package net.william278.huskhomes.redis.lettuce;

import io.lettuce.core.api.StatefulRedisConnection;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class RoundRobinConnectionPool<K, V> {
    private final AtomicInteger next = new AtomicInteger(0);
    private StatefulRedisConnection<K, V>[] elements;
    private final Supplier<StatefulRedisConnection<K, V>> statefulRedisConnectionSupplier;

    public RoundRobinConnectionPool(Supplier<StatefulRedisConnection<K, V>> statefulRedisConnectionSupplier, int poolSize) {
        this.statefulRedisConnectionSupplier = statefulRedisConnectionSupplier;
        this.elements = new StatefulRedisConnection[poolSize];
        for (int i = 0; i < poolSize; i++) {
            elements[i] = statefulRedisConnectionSupplier.get();
        }
    }

    public void expandPool(int expandBy) {
        if (expandBy <= 0) {
            throw new IllegalArgumentException("expandBy must be greater than 0");
        }
        this.elements = Arrays.copyOf(elements, elements.length + expandBy);
    }

    public StatefulRedisConnection<K, V> get() {
        int index = next.getAndIncrement() % elements.length;
        StatefulRedisConnection<K, V> connection = elements[index];
        if (connection != null && connection.isOpen()) {
            return connection;
        }

        connection = statefulRedisConnectionSupplier.get();
        elements[index] = connection;
        return connection;
    }

}