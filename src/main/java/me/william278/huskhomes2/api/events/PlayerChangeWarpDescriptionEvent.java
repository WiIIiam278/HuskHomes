package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.Warp;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player changes the description of a warp.
 */
public class PlayerChangeWarpDescriptionEvent extends PlayerWarpUpdateEvent {

    private final String oldDescription;
    private final String newDescription;

    /**
     * An event, fired when a player changes the description of a warp.
     * @param player The Player who is changing the warp's description
     * @param warp The Warp being changed
     * @param newDescription The new description being set
     * @see PlayerWarpUpdateEvent
     */
    public PlayerChangeWarpDescriptionEvent(Player player, Warp warp, String newDescription) {
        super(player, warp);
        this.oldDescription = warp.getDescription();
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
    }}