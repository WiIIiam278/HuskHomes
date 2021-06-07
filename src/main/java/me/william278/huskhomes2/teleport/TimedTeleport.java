package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.RandomPoint;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class TimedTeleport {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private final TargetType targetType;

    private TeleportationPoint targetPoint;
    private String targetPlayerName;

    private final Player player;
    private final Location initialLocation;
    private final double initialHealth;

    private int timeRemaining;
    private final int warmupTime = HuskHomes.getSettings().getTeleportWarmupTime();

    public TimedTeleport(Player player, TeleportationPoint targetPoint, TargetType targetType) {
        this.player = player;
        this.initialLocation = player.getLocation();
        this.initialHealth = player.getHealth();
        this.targetType = targetType;
        this.targetPoint = targetPoint;
        this.timeRemaining = warmupTime;

        MessageManager.sendMessage(player, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        MessageManager.sendMessage(player, "teleporting_please_stand_still");
    }

    public TimedTeleport(Player player) {
        this.player = player;
        this.initialLocation = player.getLocation();
        this.initialHealth = player.getHealth();
        this.targetType = TargetType.RANDOM;
        this.timeRemaining = warmupTime;

        MessageManager.sendMessage(player, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        MessageManager.sendMessage(player, "teleporting_please_stand_still");
    }

    public TimedTeleport(Player player, String targetPlayerName) {
        this.player = player;
        this.initialLocation = player.getLocation();
        this.initialHealth = player.getHealth();
        this.targetType = TargetType.PLAYER;
        this.targetPlayerName = targetPlayerName;
        this.timeRemaining = warmupTime;

        MessageManager.sendMessage(player, "teleporting_countdown_start", Integer.toString(this.warmupTime));
        MessageManager.sendMessage(player, "teleporting_please_stand_still");
    }

    public void begin() {
        final int[] i = {HuskHomes.getSettings().getTeleportWarmupTime()+1};
        new BukkitRunnable() {
            @Override
            public void run() {
                Player executablePlayer = Bukkit.getPlayer(player.getUniqueId());
                if (executablePlayer == null) {
                    cancel();
                    return;
                }
                if (hasMoved(executablePlayer)) {
                    cancel();
                    executablePlayer.playSound(executablePlayer.getLocation(), HuskHomes.getSettings().getTeleportCancelledSound(), 1, 1);
                    MessageManager.sendMessage(player, "teleporting_cancelled_movement");
                    MessageManager.sendActionBar(player, "teleporting_action_bar_cancelled");
                    return;
                }
                if (hasLostHealth(executablePlayer)) {
                    cancel();
                    executablePlayer.playSound(executablePlayer.getLocation(), HuskHomes.getSettings().getTeleportCancelledSound(), 1, 1);
                    MessageManager.sendMessage(player, "teleporting_cancelled_damage");
                    MessageManager.sendActionBar(player, "teleporting_action_bar_cancelled");
                    return;
                }
                i[0] = i[0] -1;
                if (i[0] == 0) {
                    cancel();
                    Connection connection = HuskHomes.getConnection();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            // Execute the teleport
                            switch (getTargetType()) {
                                case POINT:
                                    TeleportManager.teleportPlayer(player, targetPoint, connection);
                                    break;
                                case BACK:
                                    if (HuskHomes.getSettings().doEconomy()) {
                                        double backCost = HuskHomes.getSettings().getBackCost();
                                        if (backCost > 0) {
                                            if (!VaultIntegration.takeMoney(player, backCost)) {
                                                MessageManager.sendMessage(player, "error_insufficient_funds", VaultIntegration.format(backCost));
                                                break;
                                            } else {
                                                MessageManager.sendMessage(player, "back_spent_money", VaultIntegration.format(backCost));
                                            }
                                        }
                                    }
                                    TeleportManager.teleportPlayer(player, getTargetPoint(), connection);
                                    break;
                                case PLAYER:
                                    TeleportManager.teleportPlayer(player, getTargetPlayerName(), connection);
                                    break;
                                case RANDOM:
                                    RandomPoint randomPoint = new RandomPoint(player);
                                    if (randomPoint.hasFailed()) {
                                        return;
                                    }
                                    if (HuskHomes.getSettings().doEconomy()) {
                                        double rtpCost = HuskHomes.getSettings().getRtpCost();
                                        if (rtpCost > 0) {
                                            if (!VaultIntegration.takeMoney(player, rtpCost)) {
                                                MessageManager.sendMessage(player, "error_insufficient_funds", VaultIntegration.format(rtpCost));
                                                break;
                                            } else {
                                                MessageManager.sendMessage(player, "rtp_spent_money", VaultIntegration.format(rtpCost));
                                            }
                                        }
                                    }
                                    TeleportManager.teleportPlayer(player, randomPoint, connection);
                                    DataManager.updateRtpCooldown(player, connection);
                                    break;
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred carrying out a timed teleport");
                        }
                    });
                    return;
                }
                MessageManager.sendActionBar(executablePlayer, "teleporting_action_bar_countdown", Integer.toString(i[0]));
                executablePlayer.playSound(executablePlayer.getLocation(), HuskHomes.getSettings().getTeleportWarmupSound(), 1, 1);
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    public TargetType getTargetType() {
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
        return p.getHealth() < initialHealth;
    }

    // This returns if the player has moved during a timed teleport
    public boolean hasMoved(Player p) {
        Location currentLocation = p.getLocation();
        final double movementThreshold = 0.1;

        double xDiff = makePositive(initialLocation.getX() - currentLocation.getX());
        double yDiff = makePositive(initialLocation.getY() - currentLocation.getY());
        double zDiff = makePositive(initialLocation.getZ() - currentLocation.getZ());
        double totalDiff = xDiff + yDiff + zDiff;

        return totalDiff > movementThreshold;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void decrementTimeRemaining() {
        timeRemaining = timeRemaining - 1;
    }

    public enum TargetType {
        POINT,
        BACK,
        PLAYER,
        RANDOM
    }

}
