package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event, fired when a home is updated
 * Fires when a player renames a Home, updates its' location, changes its' name, changes whether or not it is public/private.
 * Also fires when it is deleted
 */
public class PlayerHomeUpdateEvent extends Event {
    private final Player player;
    private final Home home;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired when a home is updated
     * Fires when a player renames a Home, updates its' location, changes its' name, changes whether or not it is public/private, or when it is deleted
     * Does not fire when a player sets a Home
     *
     * @param player The Player who is updating the Home
     * @param home   The Home being changed
     */
    public PlayerHomeUpdateEvent(Player player, Home home) {
        this.player = player;
        this.home = home;
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
     * Get the Player involved in this event
     *
     * @return the Player who updated the home
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the Player involved in this event
     *
     * @return the Home being updated
     */
    public Home getHome() {
        return home;
    }
}
