package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * An event, fired when a warp is updated
 * Fires when a player renames a Warp, updates its' location or changes its' name.
 * Also fires when a warp is deleted
 */
public class PlayerWarpUpdateEvent extends PlayerEvent implements Cancellable {

    private final Warp warp;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired when a {@link Warp} is updated.
     * Fires when a player renames a Warp, updates its' location, changes its' name or when it is deleted.
     * Does not fire when a player sets a Warp.
     *
     * @param player The Player who is updating the Warp
     * @param warp   The Warp being changed
     */
    public PlayerWarpUpdateEvent(Player player, Warp warp) {
        super(player);
        this.warp = warp;
        this.isCancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Get the {@link Warp} being updated
     *
     * @return the {@link Warp}
     */
    public Warp getWarp() {
        return warp;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
}
