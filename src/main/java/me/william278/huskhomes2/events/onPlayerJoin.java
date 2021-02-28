package me.william278.huskhomes2.events;

import me.william278.huskhomes2.commands.tab.homeTabCompleter;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.teleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class onPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Create player on SQL if they don't exist already
        if (!dataManager.playerExists(p)) {
            dataManager.createPlayer(p);
            if (teleportManager.spawnLocation != null) {
                p.teleport(teleportManager.spawnLocation.getLocation());
            }
        } else {
            // Check if they've changed their name and update if so
            dataManager.checkPlayerNameChange(p);

            // Update their TAB cache for /home command
            homeTabCompleter.updatePlayerHomeCache(p);
        }

        // If bungee mode, check if the player joined the server from a teleport and act accordingly
        if (HuskHomes.settings.doBungee()) {
            if (dataManager.getPlayerTeleporting(p)) {
                teleportManager.teleportPlayer(p);
            }
        }
    }

}
