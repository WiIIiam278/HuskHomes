package me.william278.huskhomes2.listeners;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.objects.TeleportationPoint;
import me.william278.huskhomes2.teleportManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private static ComponentBuilder backButton() {
        ComponentBuilder backButton = new ComponentBuilder();
        TextComponent button = new TextComponent("[Go Back]");
        button.setColor(ChatColor.GREEN);

        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/back")));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Return to your death location with /back").color(ChatColor.GRAY).italic(true).create())));
        backButton.append("→ ").color(ChatColor.GRAY);
        backButton.append(button);
        return backButton;
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.hasPermission("huskhomes.back.death")) {
            dataManager.setPlayerLastPosition(p, new TeleportationPoint(p.getLocation(), HuskHomes.settings.getServerID()));
            messageManager.sendMessage(p, "return_by_death");
            p.spigot().sendMessage(backButton().create());
        }
    }

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
            HomeCommand.Tab.updatePlayerHomeCache(p);
        }

        // If bungee mode, check if the player joined the server from a teleport and act accordingly
        if (HuskHomes.settings.doBungee()) {
            if (dataManager.getPlayerTeleporting(p)) {
                teleportManager.teleportPlayer(p);
            }
        }
    }

}
