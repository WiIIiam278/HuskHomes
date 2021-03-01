package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.EditingHandler;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerChangeWarpDescriptionEvent;
import me.william278.huskhomes2.api.events.PlayerRelocateWarpEvent;
import me.william278.huskhomes2.api.events.PlayerRenameWarpEvent;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.DynMapIntegration;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditwarpCommand extends CommandBase implements TabCompleter {

    @Override
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
            return true;
        }

        String warpName = args[0];
        if (!DataManager.warpExists(warpName)) {
            MessageManager.sendMessage(p, "error_warp_invalid", warpName);
            return true;
        }

        if (args.length == 1) {
            Warp warp = DataManager.getWarp(warpName);
            EditingHandler.showEditWarpOptions(p, warp);
            return true;
        }

        switch (args[1]) {
            case "location":
                Location newLocation = p.getLocation();
                TeleportationPoint newTeleportLocation = new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID());

                // Remove old marker if on the Dynmap
                Warp locationMovedWarp = DataManager.getWarp(warpName);
                if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                    DynMapIntegration.removeDynamicMapMarker(warpName);
                }

                PlayerRelocateWarpEvent relocateWarpEvent = new PlayerRelocateWarpEvent(p, locationMovedWarp, newTeleportLocation);
                Bukkit.getPluginManager().callEvent(relocateWarpEvent);
                if (relocateWarpEvent.isCancelled()) {
                    return true;
                }

                DataManager.updateWarpLocation(warpName, p.getLocation());
                MessageManager.sendMessage(p, "edit_warp_update_location", warpName);

                // Add new updated marker if using Dynmap
                locationMovedWarp.setLocation(newLocation, HuskHomes.getSettings().getServerID());
                if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                    DynMapIntegration.addDynamicMapMarker(locationMovedWarp);
                }
                return true;
            case "description":
                if (args.length >= 3) {
                    Warp descriptionChangedWarp = DataManager.getWarp(warpName);

                    // Get the new description
                    StringBuilder newDescription = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        if (i > 2) {
                            newDescription.append(" ").append(args[i]);
                        } else {
                            newDescription = new StringBuilder(args[i]);
                        }
                    }
                    String newDescriptionString = newDescription.toString();

                    PlayerChangeWarpDescriptionEvent changeWarpDescriptionEvent = new PlayerChangeWarpDescriptionEvent(p, descriptionChangedWarp, newDescriptionString);
                    Bukkit.getPluginManager().callEvent(changeWarpDescriptionEvent);
                    if (changeWarpDescriptionEvent.isCancelled()) {
                        return true;
                    }

                    // Check the description is valid
                    if (!newDescriptionString.matches("[a-zA-Z0-9\\d\\-_\\s]+") || newDescriptionString.length() > 255) {
                        MessageManager.sendMessage(p, "error_edit_warp_invalid_description");
                        return true;
                    }

                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                        DynMapIntegration.removeDynamicMapMarker(warpName);
                    }

                    // Update description
                    DataManager.updateWarpDescription(warpName, newDescriptionString);

                    descriptionChangedWarp.setDescription(newDescriptionString);
                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                        DynMapIntegration.addDynamicMapMarker(descriptionChangedWarp);
                    }

                    // Confirmation message
                    MessageManager.sendMessage(p, "edit_warp_update_description", warpName, newDescriptionString);
                } else {
                    MessageManager.sendMessage(p, "error_invalid_syntax", "/editwarp <warp> description <new description>");
                }
                return true;
            case "rename":
                if (args.length >= 3) {
                    Warp renamedWarp = DataManager.getWarp(warpName);
                    String newName = args[2];
                    if (newName.length() > 16) {
                        MessageManager.sendMessage(p, "error_set_warp_invalid_length");
                        return true;
                    }
                    if (!newName.matches("[A-Za-z0-9_\\-]+")) {
                        MessageManager.sendMessage(p, "error_set_warp_invalid_characters");
                        return true;
                    }
                    if (DataManager.warpExists(newName)) {
                        MessageManager.sendMessage(p, "error_set_warp_name_taken");
                        return true;
                    }
                    PlayerRenameWarpEvent renameWarpEvent = new PlayerRenameWarpEvent(p, renamedWarp, newName);
                    Bukkit.getPluginManager().callEvent(renameWarpEvent);
                    if (renameWarpEvent.isCancelled()) {
                        return true;
                    }

                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                        DynMapIntegration.removeDynamicMapMarker(warpName);
                    }
                    DataManager.updateWarpName(warpName, newName);

                    renamedWarp.setName(newName);
                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                        DynMapIntegration.addDynamicMapMarker(renamedWarp);
                    }
                    WarpCommand.Tab.updateWarpsTabCache();
                    MessageManager.sendMessage(p, "edit_warp_update_name", warpName, newName);
                } else {
                    MessageManager.sendMessage(p, "error_invalid_syntax", "/editwarp <warp> rename <new warp>");
                }
                return true;
            default:
                MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("huskhomes.editwarp")) {
            return new ArrayList<>();
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
            return new ArrayList<>();
        }
    }
}
