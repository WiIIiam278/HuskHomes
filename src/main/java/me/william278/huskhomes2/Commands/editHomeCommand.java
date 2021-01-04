package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.*;
import me.william278.huskhomes2.API.Events.*;
import me.william278.huskhomes2.Commands.TabCompleters.homeTabCompleter;
import me.william278.huskhomes2.Commands.TabCompleters.publicHomeTabCompleter;
import me.william278.huskhomes2.Integrations.dynamicMap;
import me.william278.huskhomes2.Integrations.economy;
import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class editHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length >= 1) {
                String homeName = args[0];
                if (dataManager.homeExists(p.getName(), homeName)) {
                    if (args.length >= 2) {
                        switch (args[1]) {
                            case "location":
                                Location newLocation = p.getLocation();
                                TeleportationPoint newTeleportLocation = new TeleportationPoint(newLocation, HuskHomes.settings.getServerID());

                                // Remove old marker if on dynmap
                                Home locationMovedHome = dataManager.getHome(p.getName(), homeName);
                                if (locationMovedHome.isPublic() && HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                    dynamicMap.removeDynamicMapMarker(homeName, p.getName());
                                }

                                PlayerRelocateHomeEvent relocateHomeEvent = new PlayerRelocateHomeEvent(p, locationMovedHome, newTeleportLocation);
                                if (relocateHomeEvent.isCancelled()) {
                                    return true;
                                }

                                dataManager.updateHomeLocation(p.getName(), homeName, newLocation);
                                messageManager.sendMessage(p, "edit_home_update_location", homeName);

                                // Add new updated marker if using dynmap
                                locationMovedHome.setLocation(newLocation, HuskHomes.settings.getServerID());
                                if (locationMovedHome.isPublic() && HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                    dynamicMap.addDynamicMapMarker(locationMovedHome);
                                }
                                return true;
                            case "description":
                                if (args.length >= 3) {
                                    Home descriptionChangedHome = dataManager.getHome(p.getName(), homeName);

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
                                    if (changeHomeDescriptionEvent.isCancelled()) {
                                        return true;
                                    }
                                    // Check the description is valid
                                    if (!newDescriptionString.matches("[a-zA-Z0-9\\d\\-_\\s]+") || newDescriptionString.length() > 255) {
                                        messageManager.sendMessage(p, "error_edit_home_invalid_description");
                                        return true;
                                    }

                                    // Remove old marker if on the Dynmap
                                    if (descriptionChangedHome.isPublic() && HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                        dynamicMap.removeDynamicMapMarker(homeName, p.getName());
                                    }

                                    // Update description
                                    dataManager.updateHomeDescription(p.getName(), homeName, newDescriptionString);

                                    // Add new marker if using Dynmap
                                    descriptionChangedHome.setDescription(newDescriptionString);
                                    if (descriptionChangedHome.isPublic() && HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                        dynamicMap.addDynamicMapMarker(descriptionChangedHome);
                                    }

                                    // Confirmation message
                                    messageManager.sendMessage(p, "edit_home_update_description", homeName, newDescriptionString);
                                } else {
                                    messageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> description <new description>");
                                }
                                return true;
                            case "rename":
                                if (args.length >= 3) {
                                    String newName = args[2];
                                    if (newName.length() > 16) {
                                        messageManager.sendMessage(p, "error_set_home_invalid_length");
                                        return true;
                                    }
                                    if (!newName.matches("[A-Za-z0-9_\\-]+")) {
                                        messageManager.sendMessage(p, "error_set_home_invalid_characters");
                                        return true;
                                    }
                                    if (dataManager.homeExists(p.getName(), newName)) {
                                        messageManager.sendMessage(p, "error_set_home_name_taken");
                                        return true;
                                    }
                                    Home renamedHome = dataManager.getHome(p.getName(), homeName);
                                    PlayerRenameHomeEvent renameHomeEvent = new PlayerRenameHomeEvent(p, renamedHome, newName);
                                    if (renameHomeEvent.isCancelled()) {
                                        return true;
                                    }
                                    if (renamedHome.isPublic()) {
                                        if (HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                            dynamicMap.removeDynamicMapMarker(homeName, p.getName());
                                        }
                                        dataManager.updateHomeName(p.getName(), homeName, newName);
                                        publicHomeTabCompleter.updatePublicHomeTabCache();
                                    } else {
                                        dataManager.updateHomeName(p.getName(), homeName, newName);
                                    }
                                    renamedHome.setName(newName);
                                    if (renamedHome.isPublic() && HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                        dynamicMap.addDynamicMapMarker(renamedHome);
                                    }
                                    homeTabCompleter.updatePlayerHomeCache(p);
                                    messageManager.sendMessage(p, "edit_home_update_name", homeName, newName);
                                } else {
                                    messageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> rename <new name>");
                                }
                                return true;
                            case "public":
                                Home privateHome = dataManager.getHome(p.getName(), homeName);
                                if (!privateHome.isPublic()) {
                                    if (HuskHomes.settings.doEconomy()) {
                                        double publicHomeCost = HuskHomes.settings.getPublicHomeCost();
                                        if (publicHomeCost > 0) {
                                            if (!economy.takeMoney(p, publicHomeCost)) {
                                                messageManager.sendMessage(p, "error_insufficient_funds", economy.format(publicHomeCost));
                                                return true;
                                            } else {
                                                messageManager.sendMessage(p, "edit_home_privacy_public_success_economy", homeName, economy.format(publicHomeCost));
                                            }
                                        } else {
                                            messageManager.sendMessage(p, "edit_home_privacy_public_success", homeName);
                                        }
                                    } else {
                                        messageManager.sendMessage(p, "edit_home_privacy_public_success", homeName);
                                    }
                                    PlayerMakeHomePublicEvent makeHomePublicEvent = new PlayerMakeHomePublicEvent(p, privateHome);
                                    if (makeHomePublicEvent.isCancelled()) {
                                        return true;
                                    }
                                    dataManager.updateHomePrivacy(p.getName(), homeName, true);
                                    publicHomeTabCompleter.updatePublicHomeTabCache();
                                    if (HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                        dynamicMap.addDynamicMapMarker(privateHome);
                                    }
                                } else {
                                    messageManager.sendMessage(p, "error_edit_home_privacy_already_public", homeName);
                                }
                                return true;
                            case "private":
                                Home publicHome = dataManager.getHome(p.getName(), homeName);
                                if (publicHome.isPublic()) {
                                    PlayerMakeHomePrivateEvent makeHomePrivateEvent = new PlayerMakeHomePrivateEvent(p, publicHome);
                                    if (makeHomePrivateEvent.isCancelled()) {
                                        return true;
                                    }
                                    dataManager.updateHomePrivacy(p.getName(), homeName, false);
                                    messageManager.sendMessage(p, "edit_home_privacy_private_success", homeName);
                                    if (HuskHomes.settings.doDynmap() && HuskHomes.settings.showPublicHomesOnDynmap()) {
                                        dynamicMap.removeDynamicMapMarker(publicHome.getName(), publicHome.getOwnerUsername());
                                    }
                                    publicHomeTabCompleter.updatePublicHomeTabCache();
                                } else {
                                    messageManager.sendMessage(p, "error_edit_home_privacy_already_private", homeName);
                                }
                                return true;
                            default:
                                messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                                return true;
                        }
                    } else {
                        Home home = dataManager.getHome(p.getName(), homeName);
                        editingHandler.showEditHomeOptions(p, home);
                        return true;
                    }
                } else {
                    messageManager.sendMessage(p, "error_home_invalid", homeName);
                }
            } else {
                messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
            }
            return true;
        }
        return false;
    }
}
