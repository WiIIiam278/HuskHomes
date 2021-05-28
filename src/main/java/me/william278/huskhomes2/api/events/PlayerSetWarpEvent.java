package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event, fired when a player sets a new warp
 */
public class PlayerSetWarpEvent extends Event {

    private final Player player;
    private final Warp warp;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired when a Player sets a Warp
     * @param player the Player setting the warp
     * @param warp the Warp being set
     */
    public PlayerSetWarpEvent(Player player, Warp warp) {
        this.player = player;
        this.warp = warp;
        this.isCancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Get the Player involved in this event
     * @return the Player who is setting the warp
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the Warp being set
     * @return the Warp that is being set
     */
    public Warp getWarp() {
        return warp;
    }
}
