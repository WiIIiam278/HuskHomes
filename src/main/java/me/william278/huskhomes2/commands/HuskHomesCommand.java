package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import me.william278.huskhomes2.migrators.EssentialsMigrator;
import me.william278.huskhomes2.util.ChatList;
import me.william278.huskhomes2.util.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HuskHomesCommand extends CommandBase implements TabCompleter {

    private static final HuskHomes plugin = HuskHomes.getInstance();
    private static final StringBuilder PLUGIN_INFORMATION = new StringBuilder()
            .append("[HuskHomes](#00fb9a bold) [| Version ").append(plugin.getDescription().getVersion()).append("](#00fb9a)\n")
            .append("[").append(plugin.getDescription().getDescription()).append("](gray)\n")
            .append("[• Author:](white) [William278](gray show_text=&7Click to visit website open_url=https://william278.net)\n")
            .append("[• Contributors:](white) [imDaniX](gray show_text=&7Code, refactoring), [Log1x](gray show_text=&7Code)\n")
            .append("[• Translators:](white) [SnivyJ](gray show_text=&7Simplified Chinese, zh-cn), [TonyPak](gray show_text=&7Traditional Chinese, zh-tw), [Villag3r_](gray show_text=&7Italian, it-it), [ReferTV](gray show_text=&7Polish, pl) \n")
            .append("[• Help Wiki:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskHomes2/wiki/)\n")
            .append("[• Report Issues:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskHomes2/issues)\n")
            .append("[• Support Discord:](white) [[Link]](#00fb9a show_text=&7Click to join open_url=https://discord.gg/tVYhJfyDWG)");

    // Show users a list of available commands
    public static void showHelpMenu(Player player, int pageNumber) {
        ArrayList<String> commandDisplay = new ArrayList<>();
        for (String commandName : plugin.getDescription().getCommands().keySet()) {
            Command command = plugin.getCommand(commandName);
            if (command == null) {
                continue;
            }
            if (commandName.equals("huskhomes") && HuskHomes.getSettings().hideHuskHomesCommandFromHelpMenu()) {
                continue;
            }
            if (HuskHomes.getSettings().hideCommandsFromHelpMenuWithoutPermission()) {
                String permission = command.getPermission();
                if (permission != null) {
                    if (!player.hasPermission(permission)) {
                        continue;
                    }
                }
            }
            commandDisplay.add(MessageManager.getRawMessage("command_list_item", commandName,
                    command.getUsage(), command.getDescription()));
        }

        MessageManager.sendMessage(player, "command_list_header");
        ChatList helpList = new ChatList(commandDisplay, 10, "/huskhomes help", "\n", false);
        if (helpList.doesNotContainPage(pageNumber)) {
            MessageManager.sendMessage(player, "error_invalid_page_number");
            return;
        }
        player.spigot().sendMessage(helpList.getPage(pageNumber));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                showHelpMenu((Player) sender, 1);
            } else {
                sender.spigot().sendMessage(new MineDown(PLUGIN_INFORMATION.toString()).toComponent());
            }
            return true;
        }

        switch (args[0]) {
            case "about", "info" -> {
                if (sender.hasPermission("huskhomes.about")) {
                    sender.spigot().sendMessage(new MineDown(PLUGIN_INFORMATION.toString()).toComponent());
                } else {
                    sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("error_no_permission")).toComponent());
                }
                return true;
            }
            case "update" -> {
                if (sender.hasPermission("huskhomes.version_checker")) {
                    sender.spigot().sendMessage(new MineDown("[Checking for HuskHomes updates...](gray)").toComponent());
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        UpdateChecker updateChecker = new UpdateChecker(plugin);
                        if (updateChecker.isUpToDate()) {
                            sender.spigot().sendMessage(new MineDown("[HuskHomes](#00fb9a bold) [| HuskHomes is up-to-date, running Version " + updateChecker.getLatestVersion() + "](#00fb9a)").toComponent());
                        } else {
                            sender.spigot().sendMessage(
                                    new MineDown("[HuskHomes](#00fb9a bold) [| A new update is available:](#00fb9a) [HuskHomes " + updateChecker.getLatestVersion() + "](#00fb9a bold)" +
                                            "\n[•](white) [Currently running:](#00fb9a) [Version " + updateChecker.getCurrentVersion() + "](gray)" +
                                            "\n[•](white) [Download links:](#00fb9a) [[⏩ Spigot]](gray open_url=https://www.spigotmc.org/resources/huskhomes.83767/updates) [•](#262626) [[⏩ Polymart]](gray open_url=https://polymart.org/resource/huskhomes.284/updates)").toComponent());
                        }
                    });
                } else {
                    sender.spigot().sendMessage(new MineDown(MessageManager.getRawMessage("error_no_permission")).toComponent());
                }
                return true;
            }
            case "reload" -> {
                if (sender instanceof Player p) {
                    if (p.hasPermission("huskhomes.reload")) {
                        HuskHomes.getSettings().reload();
                        MessageManager.loadMessages(HuskHomes.getSettings().getLanguage());
                        sender.spigot().sendMessage(new MineDown("[HuskHomes](#00fb9a bold) &#00fb9a&| Reloaded config & message files.").toComponent());
                    } else {
                        MessageManager.sendMessage(p, "error_no_permission");
                    }
                    return true;
                } else {
                    HuskHomes.getSettings().reload();
                    MessageManager.loadMessages(HuskHomes.getSettings().getLanguage());
                    plugin.getLogger().info("Reloaded config and message files.");
                }
                return true;
            }
            case "migrate" -> {
                if (sender instanceof Player p) {
                    MessageManager.sendMessage(p, "error_console_only");
                    return true;
                }
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("essentialsx")) {
                        sender.spigot().sendMessage(new MineDown("[HuskHomes](#00fb9a bold) &#00fb9a&| Starting data migration from EssentialsX...").toComponent());
                        if (args.length == 4) {
                            EssentialsMigrator.migrate(args[2].toLowerCase(), args[3].toLowerCase());
                        } else if (args.length == 2) {
                            EssentialsMigrator.migrate();
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid syntax! Usage: /huskhomes migrate essentialsX [world filter] [target server]");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid argument! Usage: /huskhomes migrate essentialsX");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid syntax! Usage: /huskhomes migrate essentialsX");
                }
                return true;
            }
            case "help" -> {
                if (sender instanceof Player p) {
                    if (args.length == 2) {
                        try {
                            int pageNo = Integer.parseInt(args[1]);
                            showHelpMenu(p, pageNo);
                        } catch (NumberFormatException ex) {
                            MessageManager.sendMessage(p, "error_invalid_page_number");
                        }
                    } else {
                        showHelpMenu(p, 1);
                    }
                }
                return true;
            }
            default -> {
                if (sender instanceof Player) {
                    showHelpMenu((Player) sender, 1);
                } else {
                    sender.spigot().sendMessage(new MineDown(PLUGIN_INFORMATION.toString()).toComponent());
                }
                return true;
            }
        }
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        // Console is fine too
    }

    final static String[] COMMAND_TAB_ARGS = {"help", "about", "update", "reload"};

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        Player p = (Player) sender;
        if (command.getPermission() != null) {
            if (!p.hasPermission(command.getPermission())) {
                return Collections.emptyList();
            }
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMAND_TAB_ARGS), tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        } else {
            return Collections.emptyList();
        }
    }
}
