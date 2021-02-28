package me.william278.huskhomes2;

import me.william278.huskhomes2.objects.Home;
import me.william278.huskhomes2.objects.Warp;
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

        options.append(optionButton(messageManager.getRawMessage("edit_warp_teleport_button") + " ", net.md_5.bungee.api.ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/huskhomes:warp " + warp.getName(), messageManager.getRawMessage("edit_warp_teleport_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_warp_delete_button") + " ", net.md_5.bungee.api.ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/huskhomes:delwarp " + warp.getName(), messageManager.getRawMessage("edit_warp_delete_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_warp_relocate_button") + "\n", net.md_5.bungee.api.ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/huskhomes:editwarp " + warp.getName() + " location", messageManager.getRawMessage("edit_warp_relocate_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_warp_rename_button") + " ", net.md_5.bungee.api.ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:editwarp " + warp.getName() + " rename ", messageManager.getRawMessage("edit_warp_rename_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_warp_description_button") + " ", net.md_5.bungee.api.ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:editwarp " + warp.getName() + " description ", messageManager.getRawMessage("edit_warp_description_button_tooltip")));

        return options;
    }

    private static ComponentBuilder editHomeOptions(Player p, Home home) {
        ComponentBuilder options = new ComponentBuilder();

        options.append(optionButton(messageManager.getRawMessage("edit_home_teleport_button") + " ", net.md_5.bungee.api.ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/huskhomes:home " + home.getName(), messageManager.getRawMessage("edit_home_teleport_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_home_delete_button") + " ", net.md_5.bungee.api.ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/huskhomes:delhome " + home.getName(), messageManager.getRawMessage("edit_home_delete_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_home_relocate_button") + "\n", net.md_5.bungee.api.ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/huskhomes:edithome " + home.getName() + " location", messageManager.getRawMessage("edit_home_relocate_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_home_rename_button") + " ", net.md_5.bungee.api.ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:edithome " + home.getName() + " rename ", messageManager.getRawMessage("edit_home_rename_button_tooltip")));
        options.append(optionButton(messageManager.getRawMessage("edit_home_description_button") + " ", net.md_5.bungee.api.ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:edithome " + home.getName() + " description ", messageManager.getRawMessage("edit_home_description_button_tooltip")));

        if (p.hasPermission("huskhomes.edithome.public")) {
            if (!home.isPublic()) {
                options.append(optionButton(messageManager.getRawMessage("edit_home_make_public_button") + " ", net.md_5.bungee.api.ChatColor.DARK_GREEN, ClickEvent.Action.RUN_COMMAND, "/huskhomes:edithome " + home.getName() + " public", messageManager.getRawMessage("edit_home_make_public_button_tooltip")));
            } else {
                options.append(optionButton(messageManager.getRawMessage("edit_home_make_private_button") + " ", net.md_5.bungee.api.ChatColor.DARK_RED, ClickEvent.Action.RUN_COMMAND, "/huskhomes:edithome " + home.getName() + " private", messageManager.getRawMessage("edit_home_make_private_button_tooltip")));
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