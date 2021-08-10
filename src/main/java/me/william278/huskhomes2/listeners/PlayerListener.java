package me.william278.huskhomes2.listeners;

import io.papermc.lib.PaperLib;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerListener implements Listener {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.hasPermission("huskhomes.back.death")) {
            Connection connection = HuskHomes.getConnection();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    DataManager.setPlayerLastPosition(p, new TeleportationPoint(p.getLocation(), HuskHomes.getSettings().getServerID()), connection);
                    MessageManager.sendMessage(p, "return_by_death");
                } catch (SQLException sqlException) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL error occurred updating the last position of a player when they died", sqlException);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Create player on SQL if they don't exist already
        try {
            Connection connection = HuskHomes.getConnection();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    if (!DataManager.playerExists(p, connection)) {
                        DataManager.createPlayer(p, connection);
                        if (TeleportManager.getSpawnLocation() != null) {
                            Bukkit.getScheduler().runTask(plugin, () -> PaperLib.teleportAsync(p, TeleportManager.getSpawnLocation().getLocation()));
                            return;
                        }
                    } else {
                        // Check if they've changed their name and update if so
                        DataManager.checkPlayerNameChange(p, connection);

                        // Update their TAB cache for /home command
                        HomeCommand.Tab.updatePlayerHomeCache(p);
                    }

                    final Boolean isIgnoringRequests = DataManager.isPlayerIgnoringRequests(p.getUniqueId(), connection);
                    if (isIgnoringRequests != null) {
                        if (isIgnoringRequests) {
                            HuskHomes.setIgnoringTeleportRequests(p.getUniqueId());
                            MessageManager.sendMessage(p, "tpignore_on_reminder");
                        }
                    }

                    // If bungee mode, check if the player joined the server from a teleport and act accordingly
                    if (HuskHomes.getSettings().doBungee()) {
                        final Boolean isTeleporting = DataManager.isPlayerTeleporting(p.getUniqueId(), connection);
                        if (isTeleporting != null) {
                            if (isTeleporting) {
                                TeleportManager.teleportPlayer(p);
                            } else {
                                if (HuskHomes.getSettings().doForceSpawnOnLogin()) {
                                    if (TeleportManager.getSpawnLocation() != null) {
                                        Bukkit.getScheduler().runTask(plugin, () -> PaperLib.teleportAsync(p, TeleportManager.getSpawnLocation().getLocation()));
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException sqlException) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL error handling a joining player", sqlException);
                }
            });
        } catch (NullPointerException ignored) {
        } // Ignore NullPointerExceptions from players that execute this event and return null (e.g Citizens).

        // Update the player list
        if (Bukkit.getOnlinePlayers().size() == 1) {
            HuskHomes.getPlayerList().updateList(p);
        } else {
            HuskHomes.getPlayerList().addPlayer(p.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();
        if (!HuskHomes.isTeleporting(uuid)) {
            final Location logOutLocation = p.getLocation();
            Connection connection = HuskHomes.getConnection();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    DataManager.setPlayerOfflinePosition(uuid,
                            new TeleportationPoint(logOutLocation, HuskHomes.getSettings().getServerID()),
                            connection);
                } catch (SQLException ex) {
                    Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a warp exists from the table.");
                }
            });
        } else {
            HuskHomes.setNotTeleporting(uuid);
        }
    }
}
