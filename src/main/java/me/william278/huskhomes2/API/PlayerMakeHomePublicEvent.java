package me.william278.huskhomes2.API;

import me.william278.huskhomes2.Objects.Home;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player makes a home public
 */
public class PlayerMakeHomePublicEvent extends PlayerHomeUpdateEvent {
    /**
     * An event, fired when a player makes a home public
     * @param player The Player who is making the Home public
     * @param home The Home being changed
     * @see PlayerHomeUpdateEvent
     */
    public PlayerMakeHomePublicEvent(Player player, Home home) {
        super(player, home);
    }
}