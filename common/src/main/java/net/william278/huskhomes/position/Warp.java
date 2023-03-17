package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a server warp
 */
public class Warp extends SavedPosition {

    private static final String PERMISSION_PREFIX = "huskhomes.command.warp.";

    public Warp(double x, double y, double z, float yaw, float pitch,
                @NotNull World world, @NotNull String server,
                @NotNull PositionMeta positionMeta, @NotNull UUID uuid) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
    }

    /**
     * Create a new {@link Warp} from a {@link Position} and {@link PositionMeta}
     *
     * @param position The {@link Position} to save as a warp
     * @param meta     {@link PositionMeta} information about this position
     */
    public Warp(@NotNull Position position, @NotNull PositionMeta meta) {
        super(position, meta);
    }

    @NotNull
    public static String getPermission(@NotNull String warpName) {
        return PERMISSION_PREFIX + warpName.toLowerCase();
    }

    @NotNull
    public String getPermission() {
        return getPermission(getName());
    }

    @NotNull
    public static String getWildcardPermission() {
        return PERMISSION_PREFIX + "*";
    }

}
