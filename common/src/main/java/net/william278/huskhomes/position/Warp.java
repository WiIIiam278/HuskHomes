package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a server warp
 */
public class Warp extends SavedPosition {

    public Warp(double x, double y, double z, float yaw, float pitch,
                @NotNull World world, @NotNull Server server,
                @NotNull PositionMeta positionMeta, @NotNull UUID uuid) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
    }

    /**
     * Create a new {@link Warp} from a {@link Position} and {@link PositionMeta}
     *
     * @param position The {@link Position} to save as a warp
     * @param meta     {@link PositionMeta} information about this position
     */
    public Warp(Position position, PositionMeta meta) {
        super(position, meta);
    }

}
