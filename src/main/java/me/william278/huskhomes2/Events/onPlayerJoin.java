package me.william278.huskhomes2.Events;

import me.william278.huskhomes2.Main;
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
            p.teleport(teleportManager.spawnLocation.getLocation());
        } else {
            dataManager.checkPlayerNameChange(p);
        }

        // If bungee mode, check if the player joined the server from a teleport and act accordingly
        if (Main.settings.doBungee()) {
            if (dataManager.getPlayerTeleporting(p)) {
                teleportManager.teleportPlayer(p);
            }
        }
    }

}
