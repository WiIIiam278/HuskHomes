package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.migrators.EssentialsMigrator;
import me.william278.huskhomes2.util.ChatList;
import net.md_5.bungee.api.ChatColor;
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
            .append("[• Author:](white) [William278](gray show_text=&7Click to pay a visit open_url=https://youtube.com/William27528)\n")
            .append("[• Contributors:](white) [imDaniX](gray show_text=&7Code, refactoring), [Log1x](gray show_text=&7Code)\n")
            .append("[• Translators:](white) [SnivyJ](gray show_text=&7Simplified Chinese, zh-cn), [Villag3r_](gray show_text=&7Italian, it-it), [ReferTV](gray show_text=&7Polish, pl) \n")
            .append("[• Help Wiki:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskHomes2/wiki/)\n")
            .append("[• Report Issues:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskHomes2/issues)\n")
            .append("[• Support Discord:](white) [[Link]](#00fb9a show_text=&7Click to join open_url=https://discord.gg/tVYhJfyDWG)");

    // Show users a list of available commands
    public static void showHelpMenu(Player player, int pageNumber) {
        ArrayList<String> commandDisplay = new ArrayList<>();
        for (String command : plugin.getDescription().getCommands().keySet()) {
            if (HuskHomes.getSettings().hideCommandsFromHelpMenuWithoutPermission()) {
                String permission = (String) plugin.getDescription().getCommands().get(command).get("permission");
                if (permission != null) {
                    if (!player.hasPermission(permission)) {
                        continue;
                    }
                }
            }
            if (command.equals("huskhomes") && HuskHomes.getSettings().hideHuskHomesCommandFromHelpMenu()) {
                continue;
            }
            String commandDescription = (String) plugin.getDescription().getCommands().get(command).get("description");
            String commandUsage = (String) plugin.getDescription().getCommands().get(command).get("usage");
            commandDisplay.add(MessageManager.getRawMessage("command_list_item", command, commandUsage, commandDescription));
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
                if (sender instanceof Player p) {
                    if (!p.hasPermission("huskhomes.about")) {
                        MessageManager.sendMessage(p, "error_no_permission");
                        return true;
                    }
                }
                sender.spigot().sendMessage(new MineDown(PLUGIN_INFORMATION.toString()).toComponent());
                return true;
            }
            case "update" -> {
                if (sender instanceof Player p) {
                    if (p.hasPermission("huskhomes.version_checker")) {
                        if (!HuskHomes.getVersionCheckString().contains("HuskHomes is up to date!")) {
                            String updateMessage = "[Update:](dark_red) [" +
                                    HuskHomes.getVersionCheckString() + "](red)\n" + "[Get the latest version:](gray) " +
                                    "[[Download]](#00fb9a show_text=&7Click to visit webpage open_url=https://www.spigotmc.org/resources/huskhomes.83767/updates)";
                            sender.spigot().sendMessage(new MineDown(updateMessage).toComponent());

                        } else {
                            sender.spigot().sendMessage(new MineDown("[HuskHomes](#00fb9a bold) &#00fb9a&| HuskHomes is up-to-date! (Version " + plugin.getDescription().getVersion() + ")").toComponent());
                        }
                    } else {
                        MessageManager.sendMessage(p, "error_no_permission");
                    }
                } else {
                    plugin.getLogger().info(HuskHomes.getVersionCheckString());
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
