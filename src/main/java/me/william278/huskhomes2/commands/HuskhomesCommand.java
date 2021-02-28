package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.config.ConfigManager;
import me.william278.huskhomes2.migrators.EssentialsMigrator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;

public class HuskhomesCommand extends CommandBase implements TabCompleter {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static void showAboutMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "HuskHomes" + ChatColor.RESET + " " + ChatColor.GREEN + "| Version " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + plugin.getDescription().getDescription());
        sender.sendMessage("• Author: " + ChatColor.GRAY + "William278");
        sender.sendMessage("• Translators: " + ChatColor.GRAY + "RohFrenzy (de-de), 咖波 (zh-tw), imDaniX (ru), ReferTV (pl)");
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
                            if (!HuskHomes.getVersionCheckString().contains("HuskHomes is up to date!")) {
                                p.sendMessage(org.bukkit.ChatColor.DARK_RED + "Update: " + org.bukkit.ChatColor.RED + HuskHomes.getVersionCheckString());

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
                            MessageManager.sendMessage(p, "error_no_permission");
                        }
                    } else {
                        plugin.getLogger().info(HuskHomes.getVersionCheckString());
                    }
                    return true;
                case "reload":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (p.hasPermission("huskhomes.reload")) {
                            plugin.reloadConfig();
                            ConfigManager.loadConfig();
                            MessageManager.loadMessages(HuskHomes.settings.getLanguage());
                            p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "HuskHomes" + ChatColor.RESET + "" + ChatColor.GREEN + " | Reloaded config and message files.");
                        } else {
                            MessageManager.sendMessage(p, "error_no_permission");
                        }
                        return true;
                    } else {
                        ConfigManager.loadConfig();
                        MessageManager.loadMessages(HuskHomes.settings.getLanguage());
                        plugin.getLogger().info("Reloaded config and message files.");
                    }
                    return true;
                case "migrate":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        MessageManager.sendMessage(p, "error_console_only");
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("essentialsX")) {
                            sender.sendMessage(ChatColor.GREEN + "Attempting to migrate from EssentialsX...");
                            EssentialsMigrator.migrate();
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid argument! Usage: /huskhomes migrate essentialsX");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid syntax! Usage: /huskhomes migrate essentialsX");
                    }
                    return true;
                default:
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (!p.hasPermission("huskhomes.about")) {
                            MessageManager.sendMessage(p, "error_no_permission");
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
                    MessageManager.sendMessage(p, "error_no_permission");
                    return true;
                }
            }
            showAboutMenu(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            ArrayList<String> tabOptions = new ArrayList<>();
            tabOptions.add("about");
            tabOptions.add("reload");
            tabOptions.add("update");

            StringUtil.copyPartialMatches(args[0], tabOptions, tabCompletions);

            Collections.sort(tabCompletions);

            return tabCompletions;
        } else {
            return new ArrayList<>();
        }
    }
}
