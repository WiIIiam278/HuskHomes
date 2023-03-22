package net.william278.huskhomes.position;

import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a home set by a {@link User}
 */
public class Home extends SavedPosition {

    private final User owner;
    private boolean isPublic;

    private Home(double x, double y, double z, float yaw, float pitch, @NotNull World world, @NotNull String server,
                 @NotNull PositionMeta positionMeta, @NotNull UUID uuid, @NotNull User owner, boolean isPublic) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
        this.owner = owner;
        this.setPublic(isPublic);
    }

    private Home(@NotNull Position position, @NotNull PositionMeta meta, @NotNull User owner) {
        super(position, meta);
        this.owner = owner;
        this.setPublic(false);
    }

    @NotNull
    public static Home from(double x, double y, double z, float yaw, float pitch, @NotNull World world, @NotNull String server,
                            @NotNull PositionMeta positionMeta, @NotNull UUID uuid, @NotNull User owner, boolean isPublic) {
        return new Home(x, y, z, yaw, pitch, world, server, positionMeta, uuid, owner, isPublic);
    }

    @NotNull
    public static Home from(@NotNull Position position, @NotNull PositionMeta meta, @NotNull User owner) {
        return new Home(position, meta, owner);
    }

    /**
     * The {@link User} who owns this home
     */
    @NotNull
    public User getOwner() {
        return owner;
    }

    /**
     * {@code true} if this home is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
