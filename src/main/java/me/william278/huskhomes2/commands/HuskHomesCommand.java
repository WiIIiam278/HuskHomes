package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.migrators.EssentialsMigrator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HuskHomesCommand extends CommandBase implements TabCompleter {

    private static final HuskHomes plugin = HuskHomes.getInstance();
    private static final StringBuilder PLUGIN_INFORMATION = new StringBuilder()
            .append("[HuskHomes](#00fb9a bold) [| Version ").append(plugin.getDescription().getVersion()).append("](#00fb9a)\n")
            .append("[").append(plugin.getDescription().getDescription()).append("](gray)\n")
            .append("[• Author:](white) [William278](gray show_text=&7Click to pay a visit open_url=https://youtube.com/William27528)\n")
            .append("[• Contributors:](white) [imDaniX](gray show_text=&7Code, refactoring)\n")
            .append("[• Translators:](white) [RohFrenzy](gray show_text=&7German Translation, de-de), [咖波](gray show_text=&7Chinese Translation, zh-tw), [imDaniX](gray show_text=&7Russian Translation, ru), [ReferTV](gray show_text=&7Polish Translation, pl), [Villag3r_](gray show_text=&7Italian Translation, it-it) \n")
            .append("[• Help Wiki:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskHomes2/wiki/)\n")
            .append("[• Report Issues:](white) [[Link]](#00fb9a show_text=&7Click to open link open_url=https://github.com/WiIIiam278/HuskHomes2/issues)\n")
            .append("[• Support Discord:](white) [[Link]](#00fb9a show_text=&7Click to join open_url=https://discord.gg/tVYhJfyDWG)");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!p.hasPermission("huskhomes.about")) {
                    MessageManager.sendMessage(p, "error_no_permission");
                    return true;
                }
            }
            sender.spigot().sendMessage(new MineDown(PLUGIN_INFORMATION.toString()).toComponent());
            return true;
        }

        switch (args[0]) {
            case "update":
                if (sender instanceof Player) {
                    Player p = (Player) sender;
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
            case "reload":
                if (sender instanceof Player) {
                    Player p = (Player) sender;
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
            case "migrate":
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    MessageManager.sendMessage(p, "error_console_only");
                    return true;
                }
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("essentialsX")) {
                        sender.spigot().sendMessage(new MineDown("[HuskHomes](#00fb9a bold) &#00fb9a&| Starting data migration from EssentialsX...").toComponent());
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
                sender.spigot().sendMessage(new MineDown(PLUGIN_INFORMATION.toString()).toComponent());
                return true;
        }
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        // Console is fine too
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            List<String> tabOptions = new ArrayList<>();
            tabOptions.add("about");
            tabOptions.add("reload");
            tabOptions.add("update");

            StringUtil.copyPartialMatches(args[0], tabOptions, tabCompletions);

            Collections.sort(tabCompletions);

            return tabCompletions;
        } else {
            return Collections.emptyList();
        }
    }
}
