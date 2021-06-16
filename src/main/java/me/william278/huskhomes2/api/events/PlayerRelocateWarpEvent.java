package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player edits a Warp to change it's location
 */
public class PlayerRelocateWarpEvent extends PlayerWarpUpdateEvent {
    private final TeleportationPoint oldTeleportationPoint;
    private final TeleportationPoint newTeleportationPoint;

    /**
     * An event, fired when a player relocates a warp
     * @param player The Player who is relocating the Warp
     * @param warp The Warp being moved
     * @param newTeleportationPoint The new teleportation position of the Warp
     * @see PlayerWarpUpdateEvent
     */
    public PlayerRelocateWarpEvent(Player player, Warp warp, TeleportationPoint newTeleportationPoint) {
        super(player, warp);
        this.oldTeleportationPoint = warp;
        this.newTeleportationPoint = newTeleportationPoint;
    }

    /**
     * Returns the teleportation position of the Warp before it is updated
     * @return the pre-update teleportation position
     */
    public TeleportationPoint getOldTeleportationPoint() {
        return oldTeleportationPoint;
    }

    /**
     * Returns the updated teleportation position of the Warp
     * @return the updated teleportation position
     */
    public TeleportationPoint getNewTeleportationPoint() {
        return newTeleportationPoint;
    }
}
