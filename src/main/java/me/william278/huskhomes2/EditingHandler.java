package me.william278.huskhomes2;

import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

public class EditingHandler {

    private static TextComponent optionButton(String buttonText, ChatColor color, ClickEvent.Action actionType, String command, String hoverMessage) {
        TextComponent button = new TextComponent(buttonText);
        button.setColor(color);

        button.setClickEvent(new ClickEvent(actionType, (command)));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(hoverMessage).color(ChatColor.GRAY).italic(true).create())));
        return button;
    }

    private static ComponentBuilder editWarpOptions(Warp warp) {
        ComponentBuilder options = new ComponentBuilder();

        options.append(optionButton(MessageManager.getRawMessage("edit_warp_teleport_button") + " ", ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/huskhomes:warp " + warp.getName(), MessageManager.getRawMessage("edit_warp_teleport_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_warp_delete_button") + " ", ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/huskhomes:delwarp " + warp.getName(), MessageManager.getRawMessage("edit_warp_delete_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_warp_relocate_button") + "\n", ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/huskhomes:editwarp " + warp.getName() + " location", MessageManager.getRawMessage("edit_warp_relocate_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_warp_rename_button") + " ", ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:editwarp " + warp.getName() + " rename ", MessageManager.getRawMessage("edit_warp_rename_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_warp_description_button") + " ", ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:editwarp " + warp.getName() + " description ", MessageManager.getRawMessage("edit_warp_description_button_tooltip")));

        return options;
    }

    private static ComponentBuilder editHomeOptions(Player p, Home home) {
        ComponentBuilder options = new ComponentBuilder();

        options.append(optionButton(MessageManager.getRawMessage("edit_home_teleport_button") + " ", ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/huskhomes:home " + home.getName(), MessageManager.getRawMessage("edit_home_teleport_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_home_delete_button") + " ", ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/huskhomes:delhome " + home.getName(), MessageManager.getRawMessage("edit_home_delete_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_home_relocate_button") + "\n", ChatColor.BLUE, ClickEvent.Action.RUN_COMMAND, "/huskhomes:edithome " + home.getName() + " location", MessageManager.getRawMessage("edit_home_relocate_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_home_rename_button") + " ", ChatColor.YELLOW, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:edithome " + home.getName() + " rename ", MessageManager.getRawMessage("edit_home_rename_button_tooltip")));
        options.append(optionButton(MessageManager.getRawMessage("edit_home_description_button") + " ", ChatColor.GOLD, ClickEvent.Action.SUGGEST_COMMAND, "/huskhomes:edithome " + home.getName() + " description ", MessageManager.getRawMessage("edit_home_description_button_tooltip")));

        if (p.hasPermission("huskhomes.edithome.public")) {
            if (!home.isPublic()) {
                options.append(optionButton(MessageManager.getRawMessage("edit_home_make_public_button") + " ", ChatColor.DARK_GREEN, ClickEvent.Action.RUN_COMMAND, "/huskhomes:edithome " + home.getName() + " public", MessageManager.getRawMessage("edit_home_make_public_button_tooltip")));
            } else {
                options.append(optionButton(MessageManager.getRawMessage("edit_home_make_private_button") + " ", ChatColor.DARK_RED, ClickEvent.Action.RUN_COMMAND, "/huskhomes:edithome " + home.getName() + " private", MessageManager.getRawMessage("edit_home_make_private_button_tooltip")));
            }
        }
        return options;
    }

    public static void showEditHomeOptions(Player p, Home home) {
        p.sendMessage("");
        MessageManager.sendMessage(p, "edit_home_title", home.getName());
        MessageManager.sendMessage(p, "edit_description", home.getDescription());
        MessageManager.sendMessage(p, "edit_location",
                Integer.toString((int) home.getX()),
                Integer.toString((int) home.getY()),
                Integer.toString((int) home.getZ()));
        MessageManager.sendMessage(p, "edit_world", home.getWorldName());
        if (HuskHomes.settings.doBungee()) {
            MessageManager.sendMessage(p, "edit_server", home.getServer());
        }
        if (!home.isPublic()) {
            MessageManager.sendMessage(p, "edit_home_privacy_private");
        } else {
            MessageManager.sendMessage(p, "edit_home_privacy_public");
        }
        p.sendMessage("");
        p.spigot().sendMessage(editHomeOptions(p, home).create());
        p.sendMessage("");
    }

    public static void showEditWarpOptions(Player p, Warp warp) {
        p.sendMessage("");
        MessageManager.sendMessage(p, "edit_warp_title", warp.getName());
        MessageManager.sendMessage(p, "edit_description", warp.getDescription());
        MessageManager.sendMessage(p, "edit_location",
                Integer.toString((int) warp.getX()),
                Integer.toString((int) warp.getY()),
                Integer.toString((int) warp.getZ()));
        MessageManager.sendMessage(p, "edit_world", warp.getWorldName());
        if (HuskHomes.settings.doBungee()) {
            MessageManager.sendMessage(p, "edit_server", warp.getServer());
        }
        p.sendMessage("");
        p.spigot().sendMessage(editWarpOptions(warp).create());
        p.sendMessage("");
    }

}