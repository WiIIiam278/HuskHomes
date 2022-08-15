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
     * The time the position was created
     */
    @NotNull
    public Instant creationTime;

    public PositionMeta(@NotNull String name, @NotNull String description, @NotNull Instant creationTime) {
        this.name = name;
        this.description = description;
        this.creationTime = creationTime;
    }

    public PositionMeta(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
        this.creationTime = Instant.now();
    }

}
