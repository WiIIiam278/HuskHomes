package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.objects.Warp;
import org.bukkit.entity.Player;

/**
 * An event, fired when a player renames a warp
 */
public class PlayerRenameWarpEvent extends PlayerWarpUpdateEvent {
    private final String oldName;
    private final String newName;

    /**
     * An event, fired when a player changes the name of a warp
     * @param player The Player who is changing the Warp's name
     * @param warp The Warp being changed
     * @param newName The new name of the warp
     * @see PlayerWarpUpdateEvent
     */
    public PlayerRenameWarpEvent(Player player, Warp warp, String newName) {
        super(player, warp);
        this.oldName = warp.getName();
        this.newName = newName;
    }

    /**
     * Get the name of the Warp pre-update
     * @return the name of the Warp before it is updated
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Get the changed name of the Warp
     * @return the new name of the Warp
     */
    public String getNewName() {
        return newName;
    }}