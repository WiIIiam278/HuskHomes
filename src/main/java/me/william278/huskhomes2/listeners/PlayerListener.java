package me.william278.huskhomes2.listeners;

import io.papermc.lib.PaperLib;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.SQLException;
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
                        }
                    } else {
                        // Check if they've changed their name and update if so
                        DataManager.checkPlayerNameChange(p, connection);

                        // Update their TAB cache for /home command
                        HomeCommand.Tab.updatePlayerHomeCache(p);
                    }

                    // If bungee mode, check if the player joined the server from a teleport and act accordingly
                    if (HuskHomes.getSettings().doBungee()) {
                        final Boolean isTeleporting = DataManager.getPlayerTeleporting(p, connection);
                        if (isTeleporting != null) {
                            if (isTeleporting) {
                                TeleportManager.teleportPlayer(p);
                            }
                        }
                    }
                } catch (SQLException sqlException) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL error handling a joining player", sqlException);
                }
            });
        } catch (NullPointerException ignored) {
        } // Ignore NullPointerExceptions from players that execute this event and return null (e.g Citizens).
    }
}
