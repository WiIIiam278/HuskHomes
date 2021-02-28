package me.william278.huskhomes2.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * An object representing a Player's home in-game
 * @author William
 */
public class Home extends TeleportationPoint {

    String name;
    String description;
    boolean isPublic;

    UUID ownerUUID;
    String ownerUsername;

    /**
     * An object representing a Player's home in-game
     * @param location The Bukkit location of the home
     * @param server The Bungee server ID of the home
     * @param homeOwner The Player who owns the home
     * @param name The name of the home
     * @param isPublic Whether or not the home is public
     */
    public Home(Location location, String server, Player homeOwner, String name, boolean isPublic) {
        super(location, server);
        this.name = name;
        this.ownerUUID = homeOwner.getUniqueId();
        this.ownerUsername = homeOwner.getName();
        this.description = ownerUsername + "'s home";
        this.isPublic = isPublic;
    }

    /**
     * An object representing a Player's home in-game
     * @param teleportationPoint The position of the home
     * @param ownerUsername The username of the owner
     * @param ownerUUID The UUID of the owner
     * @param name The name of the home
     * @param description The description of the home
     * @param isPublic Whether or not the home is public
     */
    public Home(TeleportationPoint teleportationPoint, String ownerUsername, String ownerUUID, String name, String description, boolean isPublic) {
        super(teleportationPoint.worldName, teleportationPoint.x, teleportationPoint.y, teleportationPoint.z, teleportationPoint.yaw, teleportationPoint.pitch, teleportationPoint.server);
        this.ownerUsername = ownerUsername;
        this.ownerUUID = UUID.fromString(ownerUUID);
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    /**
     * Returns whether or not the Home is public
     * @return if the Home is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Set the privacy of the home
     * @param isPublic should the home be public?
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Returns the UUID of the Home's owner
     * @return the owner's UUID
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * Returns the username of the Home's owner
     * @return the owner's username
     */
    public String getOwnerUsername() {
        return ownerUsername;
    }

    /**
     * Returns the Player who owns the home
     * @return the Player owner of the home
     */
    public Player getOwner() {
        Player player = Bukkit.getPlayer(ownerUUID);
        if (player != null) {
            return player;
        } else {
            throw new IllegalStateException("The home owner is not online");
        }
    }

    /**
     * Set the owner of the home
     * @param player The new owner of the home
     */
    public void setOwner(Player player) {
        this.ownerUUID = player.getUniqueId();
        this.ownerUsername = player.getName();
    }

    /**
     * Returns the name of the home
     * @return the Home name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the home
     * @return the Home description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the name of the home
     * @param name the new Home name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the description of the home
     * @param description the new Home description
     */
    public void setDescription(String description) {
        this.name = description;
    }

}
