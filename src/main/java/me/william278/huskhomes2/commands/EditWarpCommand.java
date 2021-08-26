package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.EditingHandler;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerChangeWarpDescriptionEvent;
import me.william278.huskhomes2.api.events.PlayerRelocateWarpEvent;
import me.william278.huskhomes2.api.events.PlayerRenameWarpEvent;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import me.william278.huskhomes2.util.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class EditWarpCommand extends CommandBase implements TabCompleter {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
            return;
        }

        String warpName = args[0];
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!DataManager.warpExists(warpName, connection)) {
                    MessageManager.sendMessage(p, "error_warp_invalid", warpName);
                    return;
                }

                if (args.length == 1) {
                    final Warp warp = DataManager.getWarp(warpName, connection);
                    assert warp != null;
                    EditingHandler.showEditWarpOptions(p, warp);
                    return;
                }

                switch (args[1].toLowerCase(Locale.ENGLISH)) {
                    case "location" -> editWarpLocation(p, warpName, connection);
                    case "description" -> {
                        if (args.length >= 3) {
                            // Get the new description
                            editWarpDescription(p, warpName, EditHomeCommand.buildDescription(args), connection);
                        } else {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/editwarp <warp> description <new description>");
                        }
                    }
                    case "rename" -> {
                        if (args.length >= 3) {
                            editWarpName(p, warpName, args[2], connection);
                        } else {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/editwarp <warp> rename <new name>");
                        }
                    }
                    default -> MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a warp.");
            }
        });
    }

    private void editWarpLocation(Player p, String warpName, Connection connection) throws SQLException {
        Location newLocation = p.getLocation();
        TeleportationPoint newTeleportLocation = new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID());

        Warp locationMovedWarp = DataManager.getWarp(warpName, connection);
        assert locationMovedWarp != null;
        PlayerRelocateWarpEvent relocateWarpEvent = new PlayerRelocateWarpEvent(p, locationMovedWarp, newTeleportLocation);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(relocateWarpEvent);
            if (relocateWarpEvent.isCancelled()) {
                return;
            }
            try {
                // Remove old marker on map
                if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showWarpsOnMap()) {
                    HuskHomes.getMap().removeWarpMarker(warpName);
                }

                DataManager.updateWarpLocation(warpName, newLocation, connection);
                MessageManager.sendMessage(p, "edit_warp_update_location", warpName);

                // Add new updated marker on map
                locationMovedWarp.setLocation(newLocation, HuskHomes.getSettings().getServerID());
                if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showWarpsOnMap()) {
                    HuskHomes.getMap().addWarpMarker(locationMovedWarp);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred updating a warp's location.");
            }
        });
    }

    private void editWarpDescription(Player p, String warpName, String newDescriptionString, Connection connection) throws SQLException {
        Warp descriptionChangedWarp = DataManager.getWarp(warpName, connection);
        assert descriptionChangedWarp != null;
        PlayerChangeWarpDescriptionEvent changeWarpDescriptionEvent = new PlayerChangeWarpDescriptionEvent(p, descriptionChangedWarp, newDescriptionString);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(changeWarpDescriptionEvent);
            if (changeWarpDescriptionEvent.isCancelled()) {
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    // Check the description is valid
                    if (newDescriptionString.length() > 255) {
                        MessageManager.sendMessage(p, "error_edit_warp_description_length");
                        return;
                    }
                    if (!HuskHomes.getSettings().doUnicodeInDescriptions()) {
                        if (!RegexUtil.DESCRIPTION_PATTERN.matcher(newDescriptionString).matches()) {
                            MessageManager.sendMessage(p, "error_edit_warp_description_characters");
                            return;
                        }
                    }

                    // Remove old marker if on map
                    if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showWarpsOnMap()) {
                        HuskHomes.getMap().removeWarpMarker(warpName);
                    }

                    // Update description
                    DataManager.updateWarpDescription(warpName, newDescriptionString, connection);

                    // Add new marker to map if applicable
                    descriptionChangedWarp.setDescription(newDescriptionString);
                    if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showWarpsOnMap()) {
                        HuskHomes.getMap().addWarpMarker(descriptionChangedWarp);
                    }

                    // Confirmation message
                    MessageManager.sendMessage(p, "edit_warp_update_description", warpName, MineDown.escape(newDescriptionString.replace("]", "］").replace("[", "［").replace("(", "❲").replace(")", "❳")));
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a warp's description.");
                }
            });
        });
    }

    private void editWarpName(Player p, String warpName, String newName, Connection connection) throws SQLException {
        if (newName.length() > 16) {
            MessageManager.sendMessage(p, "error_set_warp_invalid_length");
            return;
        }
        if (!RegexUtil.NAME_PATTERN.matcher(newName).matches()) {
            MessageManager.sendMessage(p, "error_set_warp_invalid_characters");
            return;
        }
        if (DataManager.warpExists(newName, connection)) {
            MessageManager.sendMessage(p, "error_set_warp_name_taken");
            return;
        }
        Warp renamedWarp = DataManager.getWarp(warpName, connection);
        assert renamedWarp != null;
        PlayerRenameWarpEvent renameWarpEvent = new PlayerRenameWarpEvent(p, renamedWarp, newName);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(renameWarpEvent);
            if (renameWarpEvent.isCancelled()) {
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showWarpsOnMap()) {
                        HuskHomes.getMap().removeWarpMarker(warpName);
                    }
                    DataManager.updateWarpName(warpName, newName, connection);
                    WarpCommand.Tab.updateWarpsTabCache();
                    renamedWarp.setName(newName);
                    if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showWarpsOnMap()) {
                        HuskHomes.getMap().addWarpMarker(renamedWarp);
                    }
                    MessageManager.sendMessage(p, "edit_warp_update_name", warpName, newName);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a warp name.");
                }
            });
        });
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("huskhomes.editwarp")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], WarpCommand.Tab.warpsTabCache, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        } else if (args.length == 2) {
            List<String> editWarpOptions = new ArrayList<>();
            editWarpOptions.add("rename");
            editWarpOptions.add("location");
            editWarpOptions.add("description");
            return editWarpOptions;
        } else {
            return Collections.emptyList();
        }
    }
}
