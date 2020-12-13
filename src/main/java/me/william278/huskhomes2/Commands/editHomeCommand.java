package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.editingHandler;
import me.william278.huskhomes2.messageManager;
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
                                dataManager.updateHomeLocation(p.getName(), homeName, p.getLocation());
                                messageManager.sendMessage(p, "edit_home_update_location", homeName);
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
                                    if (!newDescriptionString.matches("[a-zA-Z0-9\\d\\-_\\s]+")) {
                                        messageManager.sendMessage(p, "error_edit_home_invalid_description");
                                        return true;
                                    }

                                    // Update description
                                    dataManager.updateHomeDescription(p.getName(), homeName, newDescriptionString);

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
                                    dataManager.updateHomeName(p.getName(), homeName, newName);
                                    messageManager.sendMessage(p, "edit_home_update_name", homeName, newName);
                                } else {
                                    messageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> rename <new name>");
                                }
                                return true;
                            case "public":
                                if (!dataManager.getHome(p.getName(), homeName).isPublic()) {
                                    dataManager.updateHomePrivacy(p.getName(), homeName, true);
                                    messageManager.sendMessage(p, "edit_home_privacy_public_success", homeName);
                                } else {
                                    messageManager.sendMessage(p, "error_edit_home_privacy_already_public", homeName);
                                }
                                return true;
                            case "private":
                                if (dataManager.getHome(p.getName(), homeName).isPublic()) {
                                    dataManager.updateHomePrivacy(p.getName(), homeName, false);
                                    messageManager.sendMessage(p, "edit_home_privacy_private_success", homeName);
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
