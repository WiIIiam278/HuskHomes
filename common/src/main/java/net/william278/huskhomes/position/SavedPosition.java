package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a persistently-saved {@link Position}
 */
public abstract class SavedPosition extends Position implements Comparable<SavedPosition> {

    /**
     * Metadata about this position (name, description)
     */
    @NotNull
    public PositionMeta meta;

    /**
     * A unique ID representing this position
     */
    @NotNull
    public final UUID uuid;

    protected SavedPosition(double x, double y, double z, float yaw, float pitch,
                            @NotNull World world, @NotNull Server server,
                            @NotNull PositionMeta meta, @NotNull UUID uuid) {
        super(x, y, z, yaw, pitch, world, server);
        this.meta = meta;
        this.uuid = uuid;
    }

    /**
     * Create a new {@link SavedPosition} from a {@link Position} and {@link PositionMeta}
     *
     * @param position The {@link Position} to save
     * @param meta     {@link PositionMeta} information about this position
     */
    protected SavedPosition(@NotNull Position position, @NotNull PositionMeta meta) {
        super(position.x, position.y, position.z, position.yaw, position.pitch,
                position.world, position.server);
        this.meta = meta;
        this.uuid = UUID.randomUUID();
    }

    // Compare based on names for alphabetical sorting
    @Override
    public int compareTo(@NotNull SavedPosition o) {
        return this.meta.name.compareTo(o.meta.name);
    }
}
