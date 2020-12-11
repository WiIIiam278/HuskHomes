package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.Main;
import me.william278.huskhomes2.messageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TimedTeleport {

    String targetType; // "player" or "point"

    TeleportationPoint targetPoint;
    String targetPlayerName;

    Player teleporter;
    Location initialTeleporterLocation;
    double initialTeleporterHealth;

    int timeRemaining;
    final int warmupTime = Main.settings.getTeleportWarmupTime();

    public TimedTeleport(Player teleporter, TeleportationPoint targetPoint) {
        this.teleporter = teleporter;
        this.initialTeleporterLocation = teleporter.getLocation();
        this.initialTeleporterHealth = teleporter.getHealth();
        this.targetType = "point";
        this.targetPoint = targetPoint;
        this.timeRemaining = warmupTime;

        messageManager.sendMessage(teleporter, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        messageManager.sendMessage(teleporter, "teleporting_please_stand_still");
    }

    public TimedTeleport(Player teleporter, String targetPlayerName) {
        this.teleporter = teleporter;
        this.initialTeleporterLocation = teleporter.getLocation();
        this.initialTeleporterHealth = teleporter.getHealth();
        this.targetType = "player";
        this.targetPlayerName = targetPlayerName;
        this.timeRemaining = warmupTime;

        messageManager.sendMessage(teleporter, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        messageManager.sendMessage(teleporter, "teleporting_please_stand_still");
    }

    public String getTargetType() {
        return targetType;
    }

    public TeleportationPoint getTargetPoint() {
        return targetPoint;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    // This converts a negative to a positive double, used in checking if a player has moved
    private static double makePositive(double d) {
        if (d < 0) {
            d = d * -1D;
        }
        return d;
    }

    // This returns if the player has lost health during a timed teleport
    public boolean hasLostHealth(Player p) {
        return p.getHealth() < initialTeleporterHealth;
    }

    // This returns if the player has moved during a timed teleport
    public boolean hasMoved(Player p) {
        Location currentLocation = p.getLocation();
        final double movementThreshold = 0.1;

        double xDiff = makePositive(initialTeleporterLocation.getX() - currentLocation.getX());
        double yDiff = makePositive(initialTeleporterLocation.getY() - currentLocation.getY());
        double zDiff = makePositive(initialTeleporterLocation.getZ() - currentLocation.getZ());
        double totalDiff = xDiff + yDiff + zDiff;

        return totalDiff > movementThreshold;
    }

    public Player getTeleporter() {
        return teleporter;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void decrementTimeRemaining() {
        timeRemaining = timeRemaining - 1;
    }

}
