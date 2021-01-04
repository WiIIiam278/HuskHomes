package me.william278.huskhomes2.API.Events;

import me.william278.huskhomes2.Objects.Home;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player makes a home private
 */
public class PlayerMakeHomePrivateEvent extends PlayerHomeUpdateEvent {
    /**
     * An event, fired when a player makes a home private
     * @param player The Player who is making the Home private
     * @param home The Home being changed
     * @see PlayerHomeUpdateEvent
     */
    public PlayerMakeHomePrivateEvent(Player player, Home home) {
        super(player, home);
    }
}
