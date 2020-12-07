package me.william278.huskhomes2.Objects;

import org.bukkit.Location;

public class Warp extends TeleportationPoint {

    String name;
    String description;

    public Warp(Location location, String server, String name) {
        super(location, server);
        this.name = name;
        this.description = "A publicly accessible warp";
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.name = description;
    }

}
