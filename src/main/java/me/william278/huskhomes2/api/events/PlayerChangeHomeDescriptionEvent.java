package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Home;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player changes the description of a home.
 */
public class PlayerChangeHomeDescriptionEvent extends PlayerHomeUpdateEvent {

    private final String oldDescription;
    private final String newDescription;

    /**
     * An event, fired when a player changes the description of a home.
     * @param player The Player who is changing the home's description
     * @param home The Home being changed
     * @param newDescription The new description being set
     * @see PlayerHomeUpdateEvent
     */
    public PlayerChangeHomeDescriptionEvent(Player player, Home home, String newDescription) {
        super(player, home);
        this.oldDescription = home.getDescription();
        this.newDescription = newDescription;
    }

    /**
     * Returns the old description of the home
     * @return the old description String
     */
    public String getOldDescription() {
        return oldDescription;
    }

    /**
     * Returns the new description being set to the home
     * @return the new description String
     */
    public String getNewDescription() {
        return newDescription;
    }
}
