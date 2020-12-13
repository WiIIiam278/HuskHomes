package me.william278.huskhomes2.Events;

import me.william278.huskhomes2.Main;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.messageManager;
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

public class onPlayerDeath implements Listener {

    private static final Main plugin = Main.getInstance();

    private static TextComponent backButton() {
        TextComponent button = new TextComponent("→ [Go Back]");
        button.setColor(ChatColor.GREEN);

        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/back")));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Return to your death location with /back").color(ChatColor.GRAY).italic(true).create())));
        return button;
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.hasPermission("huskhomes.back.death")) {
            dataManager.setPlayerLastPosition(p, new TeleportationPoint(p.getLocation(), Main.settings.getServerID()));
            messageManager.sendMessage(p, "return_by_death");
            p.spigot().sendMessage(new ComponentBuilder(backButton()).create());
        }
    }

}
