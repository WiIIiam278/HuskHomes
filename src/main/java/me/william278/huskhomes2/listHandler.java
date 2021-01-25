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

import java.util.ArrayList;

public class listHandler {

    private static TextComponent pageButton(String buttonText, ChatColor color, String command, String hoverMessage) {
        TextComponent button = new TextComponent(buttonText);
        button.setColor(color);

        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, (command)));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(hoverMessage).color(ChatColor.GRAY).italic(true).create())));
        return button;
    }

    private static TextComponent divider() {
        TextComponent divider = new TextComponent(" • ");
        divider.setColor(ChatColor.GRAY);
        return divider;
    }

    private static TextComponent clickablePrivateHome(Home home) {
        TextComponent clickableHome = new TextComponent("[" + home.getName() + "]");
        clickableHome.setColor(ChatColor.ITALIC);
        clickableHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/huskhomes:edithome " + home.getName())));
        clickableHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(home.getDescription()).color(net.md_5.bungee.api.ChatColor.GRAY).italic(false).create())));
        return clickableHome;
    }

    private static TextComponent clickablePublicHome(Home home) {
        TextComponent clickableHome = new TextComponent("[" + home.getName() + "]");
        clickableHome.setColor(ChatColor.ITALIC);
        clickableHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/huskhomes:phome " + home.getOwnerUsername() + "." + home.getName())));
        clickableHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(home.getOwnerUsername()).color(ChatColor.GREEN).italic(false).append(": ").append(home.getDescription()).color(net.md_5.bungee.api.ChatColor.GRAY).italic(false).create())));
        return clickableHome;
    }

    private static TextComponent clickableWarp(Warp warp) {
        TextComponent clickableHome = new TextComponent("[" + warp.getName() + "]");
        clickableHome.setColor(ChatColor.ITALIC);
        clickableHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/huskhomes:warp " + warp.getName())));
        clickableHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(warp.getDescription()).color(net.md_5.bungee.api.ChatColor.GRAY).italic(false).create())));
        return clickableHome;
    }

    private static void displayPageButtons(Player p, int pageNumber, int homeListSize, int homeUpperBound, String command) {
        ComponentBuilder pageButtons = new ComponentBuilder();
        TextComponent nextPageButton = pageButton(messageManager.getRawMessage("list_button_next_page"), net.md_5.bungee.api.ChatColor.GREEN, command + " " + (pageNumber + 1), messageManager.getRawMessage("list_button_next_page_tooltip"));
        TextComponent previousPageButton = pageButton(messageManager.getRawMessage("list_button_previous_page"), net.md_5.bungee.api.ChatColor.RED, command + " " + (pageNumber - 1), messageManager.getRawMessage("list_button_previous_page_tooltip"));
        TextComponent divider = new TextComponent(messageManager.getRawMessage("list_item_divider"));
        divider.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        if (pageNumber != 1) {
            pageButtons.append(previousPageButton);
            if (homeListSize > homeUpperBound) {
                pageButtons.append(divider);
                pageButtons.append(nextPageButton);
            }
            p.sendMessage("");
        } else {
            if (homeListSize > homeUpperBound) {
                pageButtons.append(nextPageButton);
                p.sendMessage("");
            }
        }
        p.spigot().sendMessage(pageButtons.create());
    }

    public static void displayPlayerHomeList(Player player, int pageNumber) {
        ComponentBuilder homeList = new ComponentBuilder();
        ArrayList<Home> homes = dataManager.getPlayerHomes(player.getName());
        if (homes == null || homes.isEmpty()) {
            messageManager.sendMessage(player, "error_no_homes_set");
            return;
        }
        int homeLowerBound = (pageNumber - 1) * 10;
        int homeUpperBound = pageNumber * 10;
        if (homeUpperBound > homes.size()) {
            homeUpperBound = homes.size();
        }

        player.sendMessage("");
        messageManager.sendMessage(player, "private_home_list_page_top", player.getName(), Integer.toString(homeLowerBound + 1), Integer.toString(homeUpperBound), Integer.toString(homes.size()));

        // Display list
        int itemsOnPage = 0;
        for (int i = homeLowerBound; i < homeUpperBound; i++) {
            try {
                Home home = homes.get(i);
                if (i != homeLowerBound) {
                    homeList.append(divider());
                }
                homeList.append(clickablePrivateHome(home));
                itemsOnPage = itemsOnPage + 1;
            } catch (IndexOutOfBoundsException e) {
                if (i == homeLowerBound) {
                    messageManager.sendMessage(player, "home_list_page_empty");
                }
                i = homeUpperBound;
            }
        }
        if (itemsOnPage == 0) {
            messageManager.sendMessage(player, "home_list_page_empty");
        } else {
            player.spigot().sendMessage(homeList.create());
        }

        // Display page buttons
        displayPageButtons(player, pageNumber, homes.size(), homeUpperBound, "/huskhomes:homelist");
    }

    public static void displayPublicHomeList(Player player, int pageNumber) {
        ComponentBuilder homeList = new ComponentBuilder();
        ArrayList<Home> homes = dataManager.getPublicHomes();
        if (homes == null || homes.isEmpty()) {
            messageManager.sendMessage(player, "error_no_public_homes_set");
            return;
        }
        int homeLowerBound = (pageNumber - 1) * 10;
        int homeUpperBound = pageNumber * 10;
        if (homeUpperBound > homes.size()) {
            homeUpperBound = homes.size();
        }

        player.sendMessage("");
        messageManager.sendMessage(player, "public_home_list_page_top", Integer.toString(homeLowerBound + 1), Integer.toString(homeUpperBound), Integer.toString(homes.size()));

        // List homes
        int itemsOnPage = 0;
        for (int i = homeLowerBound; i < homeUpperBound; i++) {
            try {
                Home home = homes.get(i);
                if (i != homeLowerBound) {
                    homeList.append(divider());
                }
                homeList.append(clickablePublicHome(home));
                itemsOnPage = itemsOnPage + 1;
            } catch (IndexOutOfBoundsException e) {
                if (i == homeLowerBound) {
                    messageManager.sendMessage(player, "home_list_page_empty");
                }
                i = homeUpperBound;
            }
        }
        if (itemsOnPage == 0) {
            messageManager.sendMessage(player, "warp_list_page_empty");
        } else {
            player.spigot().sendMessage(homeList.create());
        }

        // Display page buttons
        displayPageButtons(player, pageNumber, homes.size(), homeUpperBound, "/huskhomes:publichomelist");
    }

    public static void displayWarpList(Player player, int pageNumber) {
        ComponentBuilder warpList = new ComponentBuilder();
        ArrayList<Warp> warps = dataManager.getWarps();
        if (warps == null || warps.isEmpty()) {
            messageManager.sendMessage(player, "error_no_warps_set");
            return;
        }
        int warpsLowerBound = (pageNumber - 1) * 10;
        int warpsUpperBound = pageNumber * 10;
        if (warpsUpperBound > warps.size()) {
            warpsUpperBound = warps.size();
        }

        player.sendMessage("");
        messageManager.sendMessage(player, "warp_list_page_top", Integer.toString(warpsLowerBound + 1), Integer.toString(warpsUpperBound), Integer.toString(warps.size()));

        int itemsOnPage = 0;
        for (int i = warpsLowerBound; i < warpsUpperBound; i++) {
            try {
                Warp warp = warps.get(i);
                if (i != warpsLowerBound) {
                    warpList.append(divider());
                }
                warpList.append(clickableWarp(warp));
                itemsOnPage = itemsOnPage + 1;
            } catch (IndexOutOfBoundsException e) {
                if (i == warpsLowerBound) {
                    messageManager.sendMessage(player, "warp_list_page_empty");
                }
                i = warpsUpperBound;
            }

        }
        if (itemsOnPage == 0) {
            messageManager.sendMessage(player, "warp_list_page_empty");
        } else {
            player.spigot().sendMessage(warpList.create());
        }

        // Display page buttons
        displayPageButtons(player, pageNumber, warps.size(), warpsUpperBound, "/huskhomes:warplist");
    }

}