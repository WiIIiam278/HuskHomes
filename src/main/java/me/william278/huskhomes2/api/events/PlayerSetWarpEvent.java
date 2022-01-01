package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * An event, fired when a player sets a new warp
 */
public class PlayerSetWarpEvent extends PlayerEvent implements Cancellable {

    private final Warp warp;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired when a Player sets a Warp
     *
     * @param player the {@link Player} setting the warp
     * @param warp   the {@link Warp} being set
     */
    public PlayerSetWarpEvent(Player player, Warp warp) {
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
     * Get the Warp being set
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
