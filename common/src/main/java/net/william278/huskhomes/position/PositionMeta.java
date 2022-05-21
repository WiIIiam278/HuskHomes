package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Represents metadata about a {@link Position}, used in {@link SavedPosition} implementations
 */
public class PositionMeta {

    /**
     * The name of a position
     */
    @NotNull
    public String name;

    /**
     * A description of a position
     */
    @NotNull
    public String description;

    /**
     * The epoch timestamp of a position's creation
     */
    public long timestamp;

    public PositionMeta(@NotNull String name, @NotNull String description, long timestamp) {
        this.name = name;
        this.description = description;
        this.timestamp = timestamp;
    }

    public PositionMeta(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
        this.timestamp = Instant.now().getEpochSecond();
    }

}
