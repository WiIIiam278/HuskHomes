package net.william278.huskhomes.position;

import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a server warp
 */
public class Warp extends SavedPosition {

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

    /**
     * Get the permission node for a warp
     *
     * @param warp The warp name
     * @return The permission node
     */
    @NotNull
    @Subst("huskhomes.command.warp")
    public static String getPermissionNode(@NotNull String warp) {
        return Permission.COMMAND_WARP.node + "." + warp.toLowerCase();
    }

    /**
     * Check if a {@link OnlineUser} has permission to teleport to this warp
     *
     * @param restrictWarps Whether to restrict warps to permission nodes
     * @param user          The {@link OnlineUser} to check
     * @return true if the user has permission to teleport to this warp
     */
    public boolean hasPermission(boolean restrictWarps, @NotNull OnlineUser user) {
        return hasPermission(restrictWarps, user, getMeta().getName());
    }

    /**
     * Check if a {@link OnlineUser} has permission to teleport to this warp
     *
     * @param restrictWarps Whether to restrict warps to permission nodes
     * @param user          The {@link OnlineUser} to check
     * @param warp          The warp name to check
     * @return true if the user has permission to teleport to this warp
     */
    public static boolean hasPermission(boolean restrictWarps, @NotNull OnlineUser user, @NotNull String warp) {
        return !restrictWarps || (user.hasPermission(Permission.COMMAND_SET_WARP.node)
                                  || user.hasPermission(getPermissionNode(warp)));
    }

}
