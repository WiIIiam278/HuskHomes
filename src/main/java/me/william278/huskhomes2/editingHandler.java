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

    private static TextComponent optionButton(String buttonText, ChatColor color, ClickEvent.Action actionType, String command, String hoverMessage) {
        TextComponent button = new TextComponent(buttonText);
        button.setColor(color);

        button.setClickEvent(new ClickEvent(actionType, (command)));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(hoverMessage).color(ChatColor.GRAY).italic(true).create())));
        return button;
    }

    private static ComponentBuilder editWarpOptions(Warp warp) {
        ComponentBuilder options = new ComponentBuilder();

        options.append(optionButton("[Teleport] ", net.md_5.bungee.api.ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName(), "Teleport to this warp"));
        options.append(optionButton("[Delete] ", net.md_5.bungee.api.ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/delwarp " + warp.getName(), "Delete this warp"));
        options.append(optionButton("[Relocate]\n", net.md_5.bungee.api.ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/editwarp " + warp.getName() + " location", "Update this warp's location"));
        options.append(optionButton("[Rename] ", net.md_5.bungee.api.ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/editwarp " + warp.getName() + " rename ", "Change the name of this warp"));
        options.append(optionButton("[Edit Description] ", net.md_5.bungee.api.ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/editwarp " + warp.getName() + " description ", "Update this warp's description"));

        return options;
    }

    private static ComponentBuilder editHomeOptions(Player p, Home home) {
        ComponentBuilder options = new ComponentBuilder();

        options.append(optionButton("[Teleport] ", net.md_5.bungee.api.ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/home " + home.getName(), "Teleport to your home"));
        options.append(optionButton("[Delete] ", net.md_5.bungee.api.ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/delhome " + home.getName(), "Delete your home"));
        options.append(optionButton("[Relocate]\n", net.md_5.bungee.api.ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/edithome " + home.getName() + " location", "Update your home's location"));
        options.append(optionButton("[Rename] ", net.md_5.bungee.api.ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/edithome " + home.getName() + " rename ", "Change the name of your home"));
        options.append(optionButton("[Edit Description] ", net.md_5.bungee.api.ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/edithome " + home.getName() + " description ", "Update the description of your home"));

        if (p.hasPermission("huskhomes.edithome.public")) {
            if (!home.isPublic()) {
                options.append(optionButton("[Make Public] ", net.md_5.bungee.api.ChatColor.DARK_GREEN, ClickEvent.Action.RUN_COMMAND, "/edithome " + home.getName() + " public", "Open your home to the public"));
            } else {
                options.append(optionButton("[Make Private] ", net.md_5.bungee.api.ChatColor.DARK_RED, ClickEvent.Action.RUN_COMMAND, "/edithome " + home.getName() + " private", "Make your home private"));
            }
        }
        return options;
    }

    public static void showEditHomeOptions(Player p, Home home) {
        p.sendMessage("");
        messageManager.sendMessage(p, "edit_home_title", home.getName());
        messageManager.sendMessage(p, "edit_description", home.getDescription());
        messageManager.sendMessage(p, "edit_location",
                Integer.toString((int) home.getX()),
                Integer.toString((int) home.getY()),
                Integer.toString((int) home.getZ()));
        messageManager.sendMessage(p, "edit_world", home.getWorldName());
        if (HuskHomes.settings.doBungee()) {
            messageManager.sendMessage(p, "edit_server", home.getServer());
        }
        if (!home.isPublic()) {
            messageManager.sendMessage(p, "edit_home_privacy_private");
        } else {
            messageManager.sendMessage(p, "edit_home_privacy_public");
        }
        p.sendMessage("");
        p.spigot().sendMessage(editHomeOptions(p, home).create());
        p.sendMessage("");
    }

    public static void showEditWarpOptions(Player p, Warp warp) {
        p.sendMessage("");
        messageManager.sendMessage(p, "edit_warp_title", warp.getName());
        messageManager.sendMessage(p, "edit_description", warp.getDescription());
        messageManager.sendMessage(p, "edit_location",
                Integer.toString((int) warp.getX()),
                Integer.toString((int) warp.getY()),
                Integer.toString((int) warp.getZ()));
        messageManager.sendMessage(p, "edit_world", warp.getWorldName());
        if (HuskHomes.settings.doBungee()) {
            messageManager.sendMessage(p, "edit_server", warp.getServer());
        }
        p.sendMessage("");
        p.spigot().sendMessage(editWarpOptions(warp).create());
        p.sendMessage("");
    }

}