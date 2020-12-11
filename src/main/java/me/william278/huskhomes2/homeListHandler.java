package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.Home;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class homeListHandler {

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
        clickableHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/edithome " + home.getName())));
        clickableHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(home.getOwnerUsername()).color(ChatColor.GREEN).italic(false).append(": ").append(home.getDescription()).color(net.md_5.bungee.api.ChatColor.GRAY).italic(false).create())));
        return clickableHome;
    }

    private static TextComponent clickablePublicHome(Home home) {
        TextComponent clickableHome = new TextComponent("[" + home.getName() + "]");
        clickableHome.setColor(ChatColor.ITALIC);
        clickableHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/phome " + home.getOwnerUsername() + "." + home.getName())));
        clickableHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(home.getDescription()).color(net.md_5.bungee.api.ChatColor.GRAY).italic(false).create())));
        return clickableHome;
    }

    private static void displayPageButtons(Player p, int pageNumber, int homeListSize, int homeUpperBound) {
        ComponentBuilder pageButtons = new ComponentBuilder();
        TextComponent nextPageButton = pageButton("[Next Page →]", net.md_5.bungee.api.ChatColor.GREEN, "/homelist " + (pageNumber + 1), "Click to view next 10");
        TextComponent previousPageButton = pageButton("[← Last Page]", net.md_5.bungee.api.ChatColor.RED, "/homelist " + (pageNumber - 1), "Click to view previous 10");
        TextComponent divider = new TextComponent(" • ");
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

        for (int i = homeLowerBound; i < homeUpperBound; i++) {
            try {
                Home home = homes.get(i);
                if (i != homeLowerBound) {
                    homeList.append(divider());
                }
                homeList.append(clickablePrivateHome(home));
            } catch (IndexOutOfBoundsException e) {
                if (i == homeLowerBound) {
                    messageManager.sendMessage(player, "home_list_page_empty");
                }
                i = homeUpperBound;
            }
        }

        player.spigot().sendMessage(homeList.create());

        // Display page buttons
        displayPageButtons(player, pageNumber, homes.size(), homeUpperBound);
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
        for (int i = homeLowerBound; i < homeUpperBound; i++) {
            try {
                Home home = homes.get(i);
                if (i != homeLowerBound) {
                    homeList.append(divider());
                }
                homeList.append(clickablePublicHome(home));
            } catch (IndexOutOfBoundsException e) {
                if (i == homeLowerBound) {
                    messageManager.sendMessage(player, "home_list_page_empty");
                }
                i = homeUpperBound;
            }
        }
        player.spigot().sendMessage(homeList.create());

        // Display page buttons
        displayPageButtons(player, pageNumber, homes.size(), homeUpperBound);
    }

}