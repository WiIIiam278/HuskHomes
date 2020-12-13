package me.william278.huskhomes2;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;

public class versionChecker {
    private static final Main plugin = Main.getInstance();

    public static void adminVersionCheck(Player p) {
        if (p.hasPermission("huskhomes.version_checker")) {
            if (!versionChecker.getVersionCheckString().contains("HuskHomes is up to date!")) {
                p.sendMessage(ChatColor.DARK_RED + "Update: " + ChatColor.RED + versionChecker.getVersionCheckString());

                // Send a link to Spigot downloads page
                ComponentBuilder componentBuilder = new ComponentBuilder();
                TextComponent projectURL = new TextComponent("[Download]");
                ClickEvent clickEvent = new ClickEvent(OPEN_URL, "https://www.spigotmc.org/resources/huskhomes.83767/updates");
                projectURL.setClickEvent(clickEvent);
                projectURL.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                componentBuilder = componentBuilder.append(TextComponent.fromLegacyText("§7Get the latest version: "));
                componentBuilder = componentBuilder.append(projectURL);
                p.spigot().sendMessage(componentBuilder.create());
            }
        }
    }

    public static String getVersionCheckString() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=83767"); // Numbers = Spigot Project ID!
            URLConnection urlConnection = url.openConnection();
            String latestVersion = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
            String pluginVersion = plugin.getDescription().getVersion();
            if (!latestVersion.equals(pluginVersion)) {
                return "An update for HuskHomes is available; v" + latestVersion + " (Currently running v" + pluginVersion + ")";
            } else {
                return "HuskHomes is up to date! (Version " + pluginVersion + ")";
            }
        } catch (IOException e) {
            return "Error retrieving version information!";
        }
    }
}
