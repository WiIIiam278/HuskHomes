package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player deletes a warp
 */
public class PlayerDeleteWarpEvent extends PlayerWarpUpdateEvent {
    /**
     * An event, fired when a player deletes a warp
     *
     * @param player The Player who is deleting the warp
     * @param warp   The Warp being deleted
     * @see PlayerWarpUpdateEvent
     */
    public PlayerDeleteWarpEvent(Player player, Warp warp) {
        super(player, warp);
    }
}