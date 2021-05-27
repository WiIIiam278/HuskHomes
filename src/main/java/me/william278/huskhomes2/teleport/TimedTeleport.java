package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Player;

public class TimedTeleport {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private final String targetType; // "player" or "point"

    private TeleportationPoint targetPoint;
    private String targetPlayerName;

    private final Player teleporter;
    private final Location initialTeleporterLocation;
    private final double initialTeleporterHealth;

    private int timeRemaining;
    private final int warmupTime = HuskHomes.getSettings().getTeleportWarmupTime();

    public TimedTeleport(Player teleporter, TeleportationPoint targetPoint, String targetType) {
        this.teleporter = teleporter;
        this.initialTeleporterLocation = teleporter.getLocation();
        this.initialTeleporterHealth = teleporter.getHealth();
        this.targetType = targetType;
        this.targetPoint = targetPoint;
        this.timeRemaining = warmupTime;

        MessageManager.sendMessage(teleporter, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        MessageManager.sendMessage(teleporter, "teleporting_please_stand_still");
    }

    public TimedTeleport(Player teleporter) {
        this.teleporter = teleporter;
        this.initialTeleporterLocation = teleporter.getLocation();
        this.initialTeleporterHealth = teleporter.getHealth();
        this.targetType = "random";
        this.timeRemaining = warmupTime;

        MessageManager.sendMessage(teleporter, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        MessageManager.sendMessage(teleporter, "teleporting_please_stand_still");
    }

    public TimedTeleport(Player teleporter, String targetPlayerName) {
        this.teleporter = teleporter;
        this.initialTeleporterLocation = teleporter.getLocation();
        this.initialTeleporterHealth = teleporter.getHealth();
        this.targetType = "player";
        this.targetPlayerName = targetPlayerName;
        this.timeRemaining = warmupTime;

        MessageManager.sendMessage(teleporter, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        MessageManager.sendMessage(teleporter, "teleporting_please_stand_still");
    }

    public void begin() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {

        });
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
