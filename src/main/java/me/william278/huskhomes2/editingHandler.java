package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.Warp;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

public class editingHandler {

    private static TextComponent optionButton(String buttonText, ChatColor color, ClickEvent.Action actionType, String command, String hoverMessage, net.md_5.bungee.api.ChatColor hoverMessageColor, Boolean hoverMessageItalic) {
        TextComponent button = new TextComponent(buttonText);
        button.setColor(color);

        button.setClickEvent(new ClickEvent(actionType, (command)));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(hoverMessage).color(hoverMessageColor).italic(hoverMessageItalic).create())));
        return button;
    }

    private static ComponentBuilder editWarpOptions(Warp warp) {
        ComponentBuilder options = new ComponentBuilder();

        options.append(optionButton("[Teleport] ", net.md_5.bungee.api.ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName(), "Teleport to this warp", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Delete] ", net.md_5.bungee.api.ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/delwarp " + warp.getName(), "Delete this warp", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Relocate]\n", net.md_5.bungee.api.ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/editwarp " + warp.getName() + " location", "Update this warp's location", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Rename] ", net.md_5.bungee.api.ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/editwarp " + warp.getName() + " rename ", "Change the name of this warp", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Edit Description] ", net.md_5.bungee.api.ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/editwarp " + warp.getName() + " description ", "Update this warp's description", net.md_5.bungee.api.ChatColor.GRAY, true));

        return options;
    }

    private static ComponentBuilder editHomeOptions(Player p, Home home) {
        ComponentBuilder options = new ComponentBuilder();

        options.append(optionButton("[Teleport] ", net.md_5.bungee.api.ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/home " + home.getName(), "Teleport to your home", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Delete] ", net.md_5.bungee.api.ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/delhome " + home.getName(), "Delete your home", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Relocate]\n", net.md_5.bungee.api.ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/edithome " + home.getName() + " location", "Update your home's location", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Rename] ", net.md_5.bungee.api.ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/edithome " + home.getName() + " rename ", "Change the name of your home", net.md_5.bungee.api.ChatColor.GRAY, true));
        options.append(optionButton("[Edit Description] ", net.md_5.bungee.api.ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/edithome " + home.getName() + " description ", "Update the description of your home", net.md_5.bungee.api.ChatColor.GRAY, true));

        if (p.hasPermission("huskhomes.edithome.public")) {
            if (home.isPublic()) {
                options.append(optionButton("[Make Public] ", net.md_5.bungee.api.ChatColor.DARK_GREEN, ClickEvent.Action.RUN_COMMAND, "/edithome " + home.getName() + " public", "Open your home to the public", net.md_5.bungee.api.ChatColor.GRAY, true));
            } else {
                options.append(optionButton("[Make Private] ", net.md_5.bungee.api.ChatColor.DARK_RED, ClickEvent.Action.RUN_COMMAND, "/edithome " + home.getName() + " private", "Make your home private", net.md_5.bungee.api.ChatColor.GRAY, true));
            }
        }
        return options;
    }

    public static void showEditHomeOptions(Player p, Home home) {
        p.sendMessage("");
        messageManager.sendMessage(p, "edit_home_title", home.getName());
        messageManager.sendMessage(p, "edit_home_title", home.getDescription());
        messageManager.sendMessage(p, "edit_home_location",
                Double.toString(home.getX()),
                Double.toString(home.getY()),
                Double.toString(home.getZ()));
        messageManager.sendMessage(p, "edit_home_world", home.getWorldName());
        if (Main.settings.doBungee()) {
            messageManager.sendMessage(p, "edit_home_server", home.getServer());
        }
        if (home.isPublic()) {
            messageManager.sendMessage(p, "edit_home_privacy_public");
        } else {
            messageManager.sendMessage(p, "edit_home_privacy_private");
        }
        p.sendMessage("");
        p.spigot().sendMessage(editHomeOptions(p, home).create());
        p.sendMessage("");
    }

    public static void showEditWarpOptions(Player p, Warp warp) {
        p.sendMessage("");
        messageManager.sendMessage(p, "edit_warp_title", warp.getName());
        messageManager.sendMessage(p, "edit_warp_title", warp.getDescription());
        messageManager.sendMessage(p, "edit_warp_location",
                Double.toString(warp.getX()),
                Double.toString(warp.getY()),
                Double.toString(warp.getZ()));
        messageManager.sendMessage(p, "edit_warp_world", warp.getWorldName());
        if (Main.settings.doBungee()) {
            messageManager.sendMessage(p, "edit_warp_server", warp.getServer());
        }
        p.sendMessage("");
        p.spigot().sendMessage(editWarpOptions(warp).create());
        p.sendMessage("");
    }

}