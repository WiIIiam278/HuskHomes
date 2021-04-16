package me.william278.huskhomes2.teleport;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

import java.util.List;

public class ListHandler {

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

    private static BaseComponent[] clickablePrivateHome(Home home) {
        BaseComponent[] clickableHome = new MineDown(MessageManager.getRawMessage("home_list_item", home.getName())).urlDetection(false).toComponent();
        for (BaseComponent b : clickableHome) {
            b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/huskhomes:edithome " + home.getName())));
            b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(home.getDescription()).color(net.md_5.bungee.api.ChatColor.GRAY).italic(false).create())));
        }
        return clickableHome;
    }

    private static BaseComponent[] clickablePublicHome(Home home) {
        BaseComponent[] clickableHome = new MineDown(MessageManager.getRawMessage("public_home_list_item", home.getName())).urlDetection(false).toComponent();
        for (BaseComponent b : clickableHome) {
            b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/huskhomes:phome " + home.getOwnerUsername() + "." + home.getName())));
            b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(home.getOwnerUsername()).color(ChatColor.GREEN).italic(false).append(": ").append(home.getDescription()).color(net.md_5.bungee.api.ChatColor.GRAY).italic(false).create())));
        }
        return clickableHome;
    }

    private static BaseComponent[] clickableWarp(Warp warp) {
        BaseComponent[] clickableWarp = new MineDown(MessageManager.getRawMessage("warp_list_item", warp.getName())).urlDetection(false).toComponent();
        for (BaseComponent b : clickableWarp) {
            b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/huskhomes:warp " + warp.getName())));
            b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(warp.getDescription()).color(ChatColor.GRAY).italic(false).create())));
        }
        return clickableWarp;
    }

    private static void displayPageButtons(Player p, int pageNumber, int homeListSize, int homeUpperBound, String command, int itemsPerPage) {
        ComponentBuilder pageButtons = new ComponentBuilder();
        TextComponent nextPageButton = pageButton(MessageManager.getRawMessage("list_button_next_page"), net.md_5.bungee.api.ChatColor.GREEN, command + " " + (pageNumber + 1), MessageManager.getRawMessage("list_button_next_page_tooltip", Integer.toString(itemsPerPage)));
        TextComponent previousPageButton = pageButton(MessageManager.getRawMessage("list_button_previous_page"), net.md_5.bungee.api.ChatColor.RED, command + " " + (pageNumber - 1), MessageManager.getRawMessage("list_button_previous_page_tooltip", Integer.toString(itemsPerPage)));
        BaseComponent[] divider = new MineDown(MessageManager.getRawMessage("list_item_divider")).urlDetection(false).toComponent();

        if (pageNumber != 1) {
            pageButtons.append(previousPageButton);
            if (homeListSize > homeUpperBound) {
                pageButtons.append(divider, ComponentBuilder.FormatRetention.NONE);
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
        List<Home> homes = DataManager.getPlayerHomes(player.getName());
        if (homes == null || homes.isEmpty()) {
            MessageManager.sendMessage(player, "error_no_homes_set");
            return;
        }
        int itemsPerPage = HuskHomes.getSettings().getPrivateHomesPerPage();
        int homeLowerBound = (pageNumber - 1) * itemsPerPage;
        int homeUpperBound = pageNumber * itemsPerPage;
        if (homeUpperBound > homes.size()) {
            homeUpperBound = homes.size();
        }

        player.sendMessage("");
        MessageManager.sendMessage(player, "private_home_list_page_top", player.getName(), Integer.toString(homeLowerBound + 1), Integer.toString(homeUpperBound), Integer.toString(homes.size()));

        // Display list
        int itemsOnPage = 0;
        for (int i = homeLowerBound; i < homeUpperBound; i++) {
            try {
                Home home = homes.get(i);
                if (i != homeLowerBound) {
                    homeList.append(divider(), ComponentBuilder.FormatRetention.NONE);
                }
                homeList.append(clickablePrivateHome(home), ComponentBuilder.FormatRetention.NONE);
                itemsOnPage = itemsOnPage + 1;
            } catch (IndexOutOfBoundsException e) {
                if (i == homeLowerBound) {
                    MessageManager.sendMessage(player, "home_list_page_empty");
                }
                i = homeUpperBound;
            }
        }
        if (itemsOnPage == 0) {
            MessageManager.sendMessage(player, "home_list_page_empty");
        } else {
            player.spigot().sendMessage(homeList.create());
        }

        // Display page buttons
        displayPageButtons(player, pageNumber, homes.size(), homeUpperBound, "/huskhomes:homelist", itemsPerPage);
    }

    public static void displayPublicHomeList(Player player, int pageNumber) {
        ComponentBuilder homeList = new ComponentBuilder();
        List<Home> homes = DataManager.getPublicHomes();
        if (homes == null || homes.isEmpty()) {
            MessageManager.sendMessage(player, "error_no_public_homes_set");
            return;
        }
        int itemsPerPage = HuskHomes.getSettings().getPublicHomesPerPage();
        int homeLowerBound = (pageNumber - 1) * itemsPerPage;
        int homeUpperBound = pageNumber * itemsPerPage;
        if (homeUpperBound > homes.size()) {
            homeUpperBound = homes.size();
        }

        player.sendMessage("");
        MessageManager.sendMessage(player, "public_home_list_page_top", Integer.toString(homeLowerBound + 1), Integer.toString(homeUpperBound), Integer.toString(homes.size()));

        // List homes
        int itemsOnPage = 0;
        for (int i = homeLowerBound; i < homeUpperBound; i++) {
            try {
                Home home = homes.get(i);
                if (i != homeLowerBound) {
                    homeList.append(divider(), ComponentBuilder.FormatRetention.NONE);
                }
                homeList.append(clickablePublicHome(home), ComponentBuilder.FormatRetention.NONE);
                itemsOnPage = itemsOnPage + 1;
            } catch (IndexOutOfBoundsException e) {
                if (i == homeLowerBound) {
                    MessageManager.sendMessage(player, "home_list_page_empty");
                }
                i = homeUpperBound;
            }
        }
        if (itemsOnPage == 0) {
            MessageManager.sendMessage(player, "home_list_page_empty");
        } else {
            player.spigot().sendMessage(homeList.create());
        }

        // Display page buttons
        displayPageButtons(player, pageNumber, homes.size(), homeUpperBound, "/huskhomes:publichomelist", itemsPerPage);
    }

    public static void displayWarpList(Player player, int pageNumber) {
        ComponentBuilder warpList = new ComponentBuilder();
        List<Warp> warps = DataManager.getWarps();
        if (warps == null || warps.isEmpty()) {
            MessageManager.sendMessage(player, "error_no_warps_set");
            return;
        }
        int itemsPerPage = HuskHomes.getSettings().getWarpsPerPage();
        int warpsLowerBound = (pageNumber - 1) * itemsPerPage;
        int warpsUpperBound = pageNumber * itemsPerPage;
        if (warpsUpperBound > warps.size()) {
            warpsUpperBound = warps.size();
        }

        player.sendMessage("");
        MessageManager.sendMessage(player, "warp_list_page_top", Integer.toString(warpsLowerBound + 1), Integer.toString(warpsUpperBound), Integer.toString(warps.size()));

        int itemsOnPage = 0;
        for (int i = warpsLowerBound; i < warpsUpperBound; i++) {
            try {
                Warp warp = warps.get(i);
                if (HuskHomes.getSettings().doPermissionRestrictedWarps() && HuskHomes.getSettings().doHideRestrictedWarps()) { // Don't show inaccessible warps if hiding restricted warps is on.
                    if (!Warp.getWarpCanUse(player, warp.getName())) { continue; }
                }
                if (i != warpsLowerBound) {
                    warpList.append(divider(), ComponentBuilder.FormatRetention.NONE);
                }
                warpList.append(clickableWarp(warp), ComponentBuilder.FormatRetention.NONE);
                itemsOnPage = itemsOnPage + 1;
            } catch (IndexOutOfBoundsException e) {
                if (i == warpsLowerBound) {
                    MessageManager.sendMessage(player, "warp_list_page_empty");
                }
                i = warpsUpperBound;
            }

        }
        if (itemsOnPage == 0) {
            MessageManager.sendMessage(player, "warp_list_page_empty");
        } else {
            player.spigot().sendMessage(warpList.create());
        }

        // Display page buttons
        displayPageButtons(player, pageNumber, warps.size(), warpsUpperBound, "/huskhomes:warplist", itemsPerPage);
    }

}