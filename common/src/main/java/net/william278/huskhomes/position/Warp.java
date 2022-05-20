package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a server warp
 */
public class Warp extends SavedPosition {

    public Warp(double x, double y, double z, float yaw, float pitch,
                @NotNull World world, @NotNull Server server,
                @NotNull PositionMeta positionMeta) {
        super(x, y, z, yaw, pitch, world, server, positionMeta);
    }

}
