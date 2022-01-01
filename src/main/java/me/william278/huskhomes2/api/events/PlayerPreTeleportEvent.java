package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerPreTeleportEvent extends PlayerEvent implements Cancellable {

    private TeleportationPoint targetPoint;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired before a player is teleported by HuskHomes for any reason
     *
     * @param player      The Player who is updating the Home
     * @param targetPoint The Player's teleport destination
     */
    public PlayerPreTeleportEvent(Player player, TeleportationPoint targetPoint) {
        super(player);
        this.targetPoint = targetPoint;
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
     * Get the Player's teleportation destination {@link TeleportationPoint}
     *
     * @return the {@link TeleportationPoint}
     */
    public TeleportationPoint getTargetPoint() {
        return targetPoint;
    }

    /**
     * Set's the Player's teleportation destination {@link TeleportationPoint}
     *
     * @param targetPoint Where the player will be teleported to as a {@link TeleportationPoint}
     */
    public void setTargetPoint(TeleportationPoint targetPoint) {
        this.targetPoint = targetPoint;
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
