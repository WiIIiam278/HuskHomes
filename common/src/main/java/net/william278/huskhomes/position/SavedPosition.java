package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a persistently-saved {@link Position}
 */
public abstract class SavedPosition extends Position implements Comparable<SavedPosition> {

    private PositionMeta meta;
    private final UUID uuid;

    protected SavedPosition(double x, double y, double z, float yaw, float pitch,
                            @NotNull World world, @NotNull String server,
                            @NotNull PositionMeta meta, @NotNull UUID uuid) {
        super(x, y, z, yaw, pitch, world, server);
        this.setMeta(meta);
        this.uuid = uuid;
    }

    /**
     * Create a new {@link SavedPosition} from a {@link Position} and {@link PositionMeta}
     *
     * @param position The {@link Position} to save
     * @param meta     {@link PositionMeta} information about this position
     */
    protected SavedPosition(@NotNull Position position, @NotNull PositionMeta meta) {
        super(position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch(),
                position.getWorld(), position.getServer());
        this.setMeta(meta);
        this.uuid = UUID.randomUUID();
    }

    // Compare based on names for alphabetical sorting
    @Override
    public int compareTo(@NotNull SavedPosition o) {
        return this.getMeta().getName().compareTo(o.getMeta().getName());
    }

    /**
     * Metadata about this position (name, description)
     */
    @NotNull
    public PositionMeta getMeta() {
        return meta;
    }

    public void setMeta(@NotNull PositionMeta meta) {
        this.meta = meta;
    }

    /**
     * The name of this position. Shortcut for {@link #getMeta()}.{@link PositionMeta#getName()}
     */
    @NotNull
    public String getName() {
        return getMeta().getName();
    }

    /**
     * A unique ID representing this position
     */
    @NotNull
    public UUID getUuid() {
        return uuid;
    }
}
