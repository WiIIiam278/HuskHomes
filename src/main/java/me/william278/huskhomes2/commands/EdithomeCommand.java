package me.william278.huskhomes2.commands;

import me.william278.huskhomes2.EditingHandler;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerChangeHomeDescriptionEvent;
import me.william278.huskhomes2.api.events.PlayerMakeHomePrivateEvent;
import me.william278.huskhomes2.api.events.PlayerMakeHomePublicEvent;
import me.william278.huskhomes2.api.events.PlayerRelocateHomeEvent;
import me.william278.huskhomes2.api.events.PlayerRenameHomeEvent;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.DynMapIntegration;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
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
import java.util.Locale;

public class EdithomeCommand extends CommandBase implements TabCompleter {

    @Override
    protected boolean onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
            return true;
        }

        String homeName = args[0];
        if (!DataManager.homeExists(p.getName(), homeName)) {
            MessageManager.sendMessage(p, "error_home_invalid", homeName);
            return true;
        }

        if (args.length == 1) {
            Home home = DataManager.getHome(p.getName(), homeName);
            EditingHandler.showEditHomeOptions(p, home);
            return true;
        }

        switch (args[1].toLowerCase(Locale.ENGLISH)) {
            case "location":
                Location newLocation = p.getLocation();
                TeleportationPoint newTeleportLocation = new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID());

                // Remove old marker if on dynmap
                Home locationMovedHome = DataManager.getHome(p.getName(), homeName);
                if (locationMovedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                    DynMapIntegration.removeDynamicMapMarker(homeName, p.getName());
                }

                PlayerRelocateHomeEvent relocateHomeEvent = new PlayerRelocateHomeEvent(p, locationMovedHome, newTeleportLocation);
                Bukkit.getPluginManager().callEvent(relocateHomeEvent);
                if (relocateHomeEvent.isCancelled()) {
                    return true;
                }

                DataManager.updateHomeLocation(p.getName(), homeName, newLocation);
                MessageManager.sendMessage(p, "edit_home_update_location", homeName);

                // Add new updated marker if using dynmap
                locationMovedHome.setLocation(newLocation, HuskHomes.getSettings().getServerID());
                if (locationMovedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                    DynMapIntegration.addDynamicMapMarker(locationMovedHome);
                }
                return true;
            case "description":
                if (args.length >= 3) {
                    Home descriptionChangedHome = DataManager.getHome(p.getName(), homeName);

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
                    PlayerChangeHomeDescriptionEvent changeHomeDescriptionEvent = new PlayerChangeHomeDescriptionEvent(p, descriptionChangedHome, newDescriptionString);
                    Bukkit.getPluginManager().callEvent(changeHomeDescriptionEvent);
                    if (changeHomeDescriptionEvent.isCancelled()) {
                        return true;
                    }
                    // Check the description is valid
                    if (!newDescriptionString.matches("[a-zA-Z0-9\\d\\-_\\s]+") || newDescriptionString.length() > 255) {
                        MessageManager.sendMessage(p, "error_edit_home_invalid_description");
                        return true;
                    }

                    // Remove old marker if on the Dynmap
                    if (descriptionChangedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                        DynMapIntegration.removeDynamicMapMarker(homeName, p.getName());
                    }

                    // Update description
                    DataManager.updateHomeDescription(p.getName(), homeName, newDescriptionString);

                    // Add new marker if using Dynmap
                    descriptionChangedHome.setDescription(newDescriptionString);
                    if (descriptionChangedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                        DynMapIntegration.addDynamicMapMarker(descriptionChangedHome);
                    }

                    // Confirmation message
                    MessageManager.sendMessage(p, "edit_home_update_description", homeName, newDescriptionString);
                } else {
                    MessageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> description <new description>");
                }
                return true;
            case "rename":
                if (args.length >= 3) {
                    String newName = args[2];
                    if (newName.length() > 16) {
                        MessageManager.sendMessage(p, "error_set_home_invalid_length");
                        return true;
                    }
                    if (!newName.matches("[A-Za-z0-9_\\-]+")) {
                        MessageManager.sendMessage(p, "error_set_home_invalid_characters");
                        return true;
                    }
                    if (DataManager.homeExists(p.getName(), newName)) {
                        MessageManager.sendMessage(p, "error_set_home_name_taken");
                        return true;
                    }
                    Home renamedHome = DataManager.getHome(p.getName(), homeName);
                    PlayerRenameHomeEvent renameHomeEvent = new PlayerRenameHomeEvent(p, renamedHome, newName);
                    Bukkit.getPluginManager().callEvent(renameHomeEvent);
                    if (renameHomeEvent.isCancelled()) {
                        return true;
                    }
                    if (renamedHome.isPublic()) {
                        if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                            DynMapIntegration.removeDynamicMapMarker(homeName, p.getName());
                        }
                        DataManager.updateHomeName(p.getName(), homeName, newName);
                        PublichomeCommand.updatePublicHomeTabCache();
                    } else {
                        DataManager.updateHomeName(p.getName(), homeName, newName);
                    }
                    renamedHome.setName(newName);
                    if (renamedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                        DynMapIntegration.addDynamicMapMarker(renamedHome);
                    }
                    HomeCommand.Tab.updatePlayerHomeCache(p);
                    MessageManager.sendMessage(p, "edit_home_update_name", homeName, newName);
                } else {
                    MessageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> rename <new name>");
                }
                return true;
            case "public":
                Home privateHome = DataManager.getHome(p.getName(), homeName);
                if (!privateHome.isPublic()) {
                    if (HuskHomes.getSettings().doEconomy()) {
                        double publicHomeCost = HuskHomes.getSettings().getPublicHomeCost();
                        if (publicHomeCost > 0) {
                            if (!VaultIntegration.takeMoney(p, publicHomeCost)) {
                                MessageManager.sendMessage(p, "error_insufficient_funds", VaultIntegration.format(publicHomeCost));
                                return true;
                            } else {
                                MessageManager.sendMessage(p, "edit_home_privacy_public_success_economy", homeName, VaultIntegration.format(publicHomeCost));
                            }
                        } else {
                            MessageManager.sendMessage(p, "edit_home_privacy_public_success", homeName);
                        }
                    } else {
                        MessageManager.sendMessage(p, "edit_home_privacy_public_success", homeName);
                    }
                    PlayerMakeHomePublicEvent makeHomePublicEvent = new PlayerMakeHomePublicEvent(p, privateHome);
                    Bukkit.getPluginManager().callEvent(makeHomePublicEvent);
                    if (makeHomePublicEvent.isCancelled()) {
                        return true;
                    }
                    DataManager.updateHomePrivacy(p.getName(), homeName, true);
                    PublichomeCommand.updatePublicHomeTabCache();
                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                        DynMapIntegration.addDynamicMapMarker(privateHome);
                    }
                } else {
                    MessageManager.sendMessage(p, "error_edit_home_privacy_already_public", homeName);
                }
                return true;
            case "private":
                Home publicHome = DataManager.getHome(p.getName(), homeName);
                if (publicHome.isPublic()) {
                    PlayerMakeHomePrivateEvent makeHomePrivateEvent = new PlayerMakeHomePrivateEvent(p, publicHome);
                    Bukkit.getPluginManager().callEvent(makeHomePrivateEvent);
                    if (makeHomePrivateEvent.isCancelled()) {
                        return true;
                    }
                    DataManager.updateHomePrivacy(p.getName(), homeName, false);
                    MessageManager.sendMessage(p, "edit_home_privacy_private_success", homeName);
                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                        DynMapIntegration.removeDynamicMapMarker(publicHome.getName(), publicHome.getOwnerUsername());
                    }
                    PublichomeCommand.updatePublicHomeTabCache();
                } else {
                    MessageManager.sendMessage(p, "error_edit_home_privacy_already_private", homeName);
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
        if (!p.hasPermission("huskhomes.edithome")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], HomeCommand.Tab.homeTabCache.get(p.getUniqueId()), tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        } else if (args.length == 2) {
            List<String> editHomeOptions = new ArrayList<>();
            editHomeOptions.add("rename");
            editHomeOptions.add("location");
            editHomeOptions.add("description");
            if (sender.hasPermission("huskhomes.edithome.public")) {
                editHomeOptions.add("public");
                editHomeOptions.add("private");
            }
            return editHomeOptions;
        } else {
            return Collections.emptyList();
        }
    }
}
