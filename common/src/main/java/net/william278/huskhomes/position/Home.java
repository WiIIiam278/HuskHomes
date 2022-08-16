package net.william278.huskhomes.position;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

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

    public Home(@NotNull Position position, @NotNull PositionMeta meta, @NotNull User owner) {
        super(position, meta);
        this.owner = owner;
        this.isPublic = false;
    }

}
