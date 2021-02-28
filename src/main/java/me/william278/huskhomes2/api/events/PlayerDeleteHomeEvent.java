package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.Home;
import org.bukkit.entity.Player;

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