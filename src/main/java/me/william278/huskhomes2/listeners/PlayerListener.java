package me.william278.huskhomes2.listeners;

import me.william278.huskhomes2.CrossServerListHandler;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.PluginMessageHandler;
import me.william278.huskhomes2.api.events.PlayerRtpEvent;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.World;
import org.bukkit.entity.Husk;
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
    public void onRtp(PlayerRtpEvent rte){
        if(!rte.isCancelled())
        if (HuskHomes.getSettings().doRtpCommand()) {
            if (rte.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {
                TeleportManager.queueRandomTeleport(rte.getPlayer());
            } else {
                MessageManager.sendMessage(rte.getPlayer(), "error_rtp_invalid_dimension");
            }
        } else {
            MessageManager.sendMessage(rte.getPlayer(), "error_command_disabled");
        }
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.hasPermission("huskhomes.back.death")) {
            DataManager.setPlayerLastPosition(p, new TeleportationPoint(p.getLocation(), HuskHomes.getSettings().getServerID()));
            MessageManager.sendMessage(p, "return_by_death");
            p.spigot().sendMessage(backButton().create());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Create player on SQL if they don't exist already
        try {
            if (!DataManager.playerExists(p)) {
                DataManager.createPlayer(p);
                if (TeleportManager.getSpawnLocation() != null) {
                    p.teleport(TeleportManager.getSpawnLocation().getLocation());
                }
            } else {
                // Check if they've changed their name and update if so
                DataManager.checkPlayerNameChange(p);

                // Update their TAB cache for /home command
                HomeCommand.Tab.updatePlayerHomeCache(p);
            }

            // If bungee mode, check if the player joined the server from a teleport and act accordingly
            if (HuskHomes.getSettings().doBungee()) {
                if (DataManager.getPlayerTeleporting(p)) {
                    TeleportManager.teleportPlayer(p);
                }

                // Update player lists globally
                if (HuskHomes.getSettings().doCrossServerTabCompletion()) {
                    CrossServerListHandler.updatePlayerList(p);
                    PluginMessageHandler.broadcastPlayerChange(p);
                }
            }
        } catch (NullPointerException ignored) { } // Ignore NullPointerExceptions from players that execute this event and return null (e.g Citizens).
    }
}
