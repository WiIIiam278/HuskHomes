package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.*;
import me.william278.huskhomes2.Objects.Home;
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

                                // Remove old marker if on dynmap
                                Home locationMovedHome = dataManager.getHome(p.getName(), homeName);
                                if (locationMovedHome.isPublic() && Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                    dynamicMapHandler.removeDynamicMapMarker(homeName, p.getName());
                                }

                                dataManager.updateHomeLocation(p.getName(), homeName, newLocation);
                                messageManager.sendMessage(p, "edit_home_update_location", homeName);

                                // Add new updated marker if using dynmap
                                locationMovedHome.setLocation(newLocation, Main.settings.getServerID());
                                if (locationMovedHome.isPublic() && Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                    dynamicMapHandler.addDynamicMapMarker(locationMovedHome);
                                }
                                return true;
                            case "description":
                                if (args.length >= 3) {
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

                                    // Check the description is valid
                                    if (!newDescriptionString.matches("[a-zA-Z0-9\\d\\-_\\s]+") && newDescriptionString.length() > 255) {
                                        messageManager.sendMessage(p, "error_edit_home_invalid_description");
                                        return true;
                                    }

                                    // Remove old marker if on dynmap
                                    Home descriptionChangedHome = dataManager.getHome(p.getName(), homeName);
                                    if (descriptionChangedHome.isPublic() && Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                        dynamicMapHandler.removeDynamicMapMarker(homeName, p.getName());
                                    }

                                    // Update description
                                    dataManager.updateHomeDescription(p.getName(), homeName, newDescriptionString);

                                    // Add new marker if on dynmap
                                    descriptionChangedHome.setDescription(newDescriptionString);
                                    if (descriptionChangedHome.isPublic() && Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                        dynamicMapHandler.addDynamicMapMarker(descriptionChangedHome);
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
                                    if (renamedHome.isPublic() && Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                        dynamicMapHandler.removeDynamicMapMarker(homeName, p.getName());
                                    }
                                    dataManager.updateHomeName(p.getName(), homeName, newName);
                                    renamedHome.setName(newName);
                                    if (renamedHome.isPublic() && Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                        dynamicMapHandler.addDynamicMapMarker(renamedHome);
                                    }
                                    messageManager.sendMessage(p, "edit_home_update_name", homeName, newName);
                                } else {
                                    messageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> rename <new name>");
                                }
                                return true;
                            case "public":
                                Home privateHome = dataManager.getHome(p.getName(), homeName);
                                if (!privateHome.isPublic()) {
                                    dataManager.updateHomePrivacy(p.getName(), homeName, true);
                                    messageManager.sendMessage(p, "edit_home_privacy_public_success", homeName);
                                    if (Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                        dynamicMapHandler.addDynamicMapMarker(privateHome);
                                    }
                                } else {
                                    messageManager.sendMessage(p, "error_edit_home_privacy_already_public", homeName);
                                }
                                return true;
                            case "private":
                                Home publicHome = dataManager.getHome(p.getName(), homeName);
                                if (publicHome.isPublic()) {
                                    dataManager.updateHomePrivacy(p.getName(), homeName, false);
                                    messageManager.sendMessage(p, "edit_home_privacy_private_success", homeName);
                                    if (Main.settings.doDynmap() && Main.settings.isDynmapPublicHomes()) {
                                        dynamicMapHandler.removeDynamicMapMarker(publicHome.getName(), publicHome.getOwnerUsername());
                                    }
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
