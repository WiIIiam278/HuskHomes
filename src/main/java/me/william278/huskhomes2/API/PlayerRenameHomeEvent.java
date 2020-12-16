package me.william278.huskhomes2.API;

import me.william278.huskhomes2.Objects.Home;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player renames a home
 */
public class PlayerRenameHomeEvent extends PlayerHomeUpdateEvent {

    private final String oldName;
    private final String newName;

    /**
     * An event, fired when a player changes the name of a home
     * @param player The Player who is changing the Home's name
     * @param home The Home being changed
     * @param newName The new name of the home
     * @see PlayerHomeUpdateEvent
     */
    public PlayerRenameHomeEvent(Player player, Home home, String newName) {
        super(player, home);
        this.oldName = home.getName();
        this.newName = newName;
    }

    /**
     * Get the name of the Home pre-update
     * @return the name of the Home before it is updated
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Get the changed name of the Home
     * @return the new name of the Home
     */
    public String getNewName() {
        return newName;
    }
}
