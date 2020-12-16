package me.william278.huskhomes2.API;

import me.william278.huskhomes2.Objects.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event, fired when a player sets a new home
 */
public class PlayerSetHomeEvent extends Event implements Cancellable {

    private Player player;
    private Home home;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired when a player sets a Home
     * @param player the Player setting the Home
     * @param home the Home being set
     */
    public PlayerSetHomeEvent(Player player, Home home) {
        this.player = player;
        this.home = home;
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
     * Get the player involved in this event
     * @return the Player who is setting the home
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the Home being set
     * @return the Home being set
     */
    public Home getHome() {
        return home;
    }
}
