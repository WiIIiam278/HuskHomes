package me.william278.huskhomes2.API;

import me.william278.huskhomes2.Objects.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event, fired when a warp is updated
 * Fires when a player renames a Warp, updates its' location or changes its' name.
 * Also fires when a warp is deleted
 */
public class PlayerWarpUpdateEvent extends Event implements Cancellable {

    private final Player player;
    private final Warp warp;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired when a Warp is updated.
     * Fires when a player renames a Warp, updates its' location, changes its' name or when it is deleted.
     * Does not fire when a player sets a Warp.
     * @param player The Player who is updating the Warp
     * @param warp The Warp being changed
     */
    public PlayerWarpUpdateEvent(Player player, Warp warp) {
        this.player = player;
        this.warp = warp;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Get the Player involved in this event
     * @return the Player who updated the Warp
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the Warp being updated
     * @return the Warp being updated
     */
    public Warp getWarp() {
        return warp;
    }
}
