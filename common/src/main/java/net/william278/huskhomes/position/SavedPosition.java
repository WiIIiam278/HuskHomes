package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a persistently-saved {@link Position}
 */
public class SavedPosition extends Position {

    /**
     * Metadata about this position (name, description)
     */
    @NotNull
    public final PositionMeta positionMeta;

    public SavedPosition(double x, double y, double z, float yaw, float pitch,
                         @NotNull World world, @NotNull Server server,
                         @NotNull PositionMeta positionMeta) {
        super(x, y, z, yaw, pitch, world, server);
        this.positionMeta = positionMeta;
    }

}
