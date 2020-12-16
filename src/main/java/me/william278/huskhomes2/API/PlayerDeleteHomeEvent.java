package me.william278.huskhomes2.API;

import me.william278.huskhomes2.Objects.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event, fired when a player deletes a home
 */
public class PlayerDeleteHomeEvent extends PlayerHomeUpdateEvent {
    /**
     * An event, fired when a player deletes a home
     * @param player The Player who is deleting the home
     * @param home The Home being deleted
     * @see PlayerHomeUpdateEvent
     */
    public PlayerDeleteHomeEvent(Player player, Home home) {
        super(player, home);
    }
}