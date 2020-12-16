package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.configManager;
import me.william278.huskhomes2.messageManager;
import me.william278.huskhomes2.versionChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;

public class huskHomesCommand implements CommandExecutor {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static void showAboutMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "HuskHomes" + ChatColor.RESET + " " + ChatColor.GREEN + "| Version " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + plugin.getDescription().getDescription());
        sender.sendMessage("• Author: " + ChatColor.GRAY + "William278");
        sender.sendMessage("• Help Wiki: " + ChatColor.GRAY + "https://github.com/WiIIiam278/HuskHomes2/wiki/");
        sender.sendMessage("• Report a bug: " + ChatColor.GRAY + "https://github.com/WiIIiam278/HuskHomes2/issues");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "update":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (p.hasPermission("huskhomes.version_checker")) {
                            if (!versionChecker.getVersionCheckString().contains("HuskHomes is up to date!")) {
                                p.sendMessage(org.bukkit.ChatColor.DARK_RED + "Update: " + org.bukkit.ChatColor.RED + versionChecker.getVersionCheckString());

                                // Send a link to Spigot downloads page
                                ComponentBuilder componentBuilder = new ComponentBuilder();
                                TextComponent projectURL = new TextComponent("[Download]");
                                ClickEvent clickEvent = new ClickEvent(OPEN_URL, "https://www.spigotmc.org/resources/huskhomes.83767/updates");
                                projectURL.setClickEvent(clickEvent);
                                projectURL.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                                componentBuilder = componentBuilder.append(TextComponent.fromLegacyText("§7Get the latest version: "));
                                componentBuilder = componentBuilder.append(projectURL);
                                p.spigot().sendMessage(componentBuilder.create());
                            } else {
                                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "HuskHomes" + ChatColor.RESET + "" + ChatColor.GREEN + " | HuskHomes is up-to-date! (Version " + plugin.getDescription().getVersion() + ")");
                            }
                        } else {
                            messageManager.sendMessage(p, "error_no_permission");
                        }
                    } else {
                        plugin.getLogger().info(versionChecker.getVersionCheckString());
                    }
                    return true;
                case "reload":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (p.hasPermission("huskhomes.reload")) {
                            configManager.loadConfig();
                            messageManager.loadMessages(HuskHomes.settings.getLanguage());
                            p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "HuskHomes" + ChatColor.RESET + "" + ChatColor.GREEN + " | Reloaded config and message files.");
                        } else {
                            messageManager.sendMessage(p, "error_no_permission");
                        }
                        return true;
                    } else {
                        configManager.loadConfig();
                        messageManager.loadMessages(HuskHomes.settings.getLanguage());
                        plugin.getLogger().info("Reloaded config and message files.");
                    }
                    return true;
                default:
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (!p.hasPermission("huskhomes.about")) {
                            messageManager.sendMessage(p, "error_no_permission");
                            return true;
                        }
                    }
                    showAboutMenu(sender);
                    return true;
            }
        } else {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!p.hasPermission("huskhomes.about")) {
                    messageManager.sendMessage(p, "error_no_permission");
                    return true;
                }
            }
            showAboutMenu(sender);
        }
        return true;
    }
}
