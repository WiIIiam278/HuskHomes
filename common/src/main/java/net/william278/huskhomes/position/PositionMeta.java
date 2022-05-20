package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

/**
 * Represents metadata about a {@link Position}, used in {@link SavedPosition} implementations
 */
public class PositionMeta {

    /**
     * The name of this position
     */
    @NotNull
    public String name;

    /**
     * A description of this position
     */
    @NotNull
    public String description;

    public PositionMeta(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
    }

}
