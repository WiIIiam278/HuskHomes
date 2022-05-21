package net.william278.huskhomes.position;

import net.william278.huskhomes.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a home set by a {@link Player}
 */
public class Home extends SavedPosition {

    /**
     * The {@link Player} who owns this home
     */
    @NotNull
    public final Player owner;

    /**
     * {@code true} if this home is public
     */
    public boolean isPublic;

    public Home(double x, double y, double z, float yaw, float pitch,
                @NotNull World world, @NotNull Server server,
                @NotNull PositionMeta positionMeta, @NotNull UUID uuid,
                @NotNull Player owner, boolean isPublic) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
        this.owner = owner;
        this.isPublic = isPublic;
    }

    public Home(Position position, PositionMeta meta, @NotNull Player owner) {
        super(position, meta);
        this.owner = owner;
        this.isPublic = false;
    }
}
