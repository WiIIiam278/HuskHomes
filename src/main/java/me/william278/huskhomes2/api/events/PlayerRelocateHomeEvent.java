package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player edits a Warp to change it's location
 */
public class PlayerRelocateHomeEvent extends PlayerHomeUpdateEvent {
    private final TeleportationPoint oldTeleportationPoint;
    private final TeleportationPoint newTeleportationPoint;

    /**
     * An event, fired when a player relocates a home
     *
     * @param player                The Player who is relocating the Home
     * @param home                  The Home being moved
     * @param newTeleportationPoint The new teleportation position of the Home
     * @see PlayerWarpUpdateEvent
     */
    public PlayerRelocateHomeEvent(Player player, Home home, TeleportationPoint newTeleportationPoint) {
        super(player, home);
        this.oldTeleportationPoint = home;
        this.newTeleportationPoint = newTeleportationPoint;
    }

    /**
     * Returns the teleportation position of the Home before it is updated
     *
     * @return the pre-update teleportation position
     */
    public TeleportationPoint getOldTeleportationPoint() {
        return oldTeleportationPoint;
    }

    /**
     * Returns the updated teleportation position of the Home
     *
     * @return the updated teleportation position
     */
    public TeleportationPoint getNewTeleportationPoint() {
        return newTeleportationPoint;
    }
}
