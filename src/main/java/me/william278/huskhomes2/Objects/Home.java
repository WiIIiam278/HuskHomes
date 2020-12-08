package me.william278.huskhomes2.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Home extends TeleportationPoint {

    String name;
    String description;
    boolean isPublic;

    UUID ownerUUID;
    String ownerUsername;

    public Home(Location location, String server, Player homeOwner, String name, boolean isPublic) {
        super(location, server);
        this.name = name;
        this.ownerUUID = homeOwner.getUniqueId();
        this.ownerUsername = homeOwner.getName();
        this.description = ownerUsername + "'s home";
        this.isPublic = isPublic;
    }

    public Home(TeleportationPoint teleportationPoint, String ownerUsername, String ownerUUID, String name, String description, boolean isPublic) {
        super(teleportationPoint.worldName, teleportationPoint.x, teleportationPoint.y, teleportationPoint.z, teleportationPoint.yaw, teleportationPoint.pitch, teleportationPoint.server);
        this.ownerUsername = ownerUsername;
        this.ownerUUID = UUID.fromString(ownerUUID);
        this.name = name;
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Player getOwner() {
        Player player = Bukkit.getPlayer(ownerUUID);
        if (player != null) {
            return player;
        } else {
            throw new IllegalStateException("The home owner is not online");
        }
    }

    public void setOwner(Player p) {
        this.ownerUUID = p.getUniqueId();
        this.ownerUsername = p.getName();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.name = description;
    }

}
