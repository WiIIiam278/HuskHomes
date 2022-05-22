package net.william278.huskhomes.position;

import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a home set by a {@link User}
 */
public class Home extends SavedPosition {

    /**
     * The {@link User} who owns this home
     */
    @NotNull
    public final User owner;

    /**
     * {@code true} if this home is public
     */
    public boolean isPublic;

    public Home(double x, double y, double z, float yaw, float pitch,
                @NotNull World world, @NotNull Server server,
                @NotNull PositionMeta positionMeta, @NotNull UUID uuid,
                @NotNull User owner, boolean isPublic) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
        this.owner = owner;
        this.isPublic = isPublic;
    }

    public Home(Position position, PositionMeta meta, @NotNull User owner) {
        super(position, meta);
        this.owner = owner;
        this.isPublic = false;
    }
}
