package me.william278.huskhomes2.Commands;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.Warp;
import me.william278.huskhomes2.dataManager;
import me.william278.huskhomes2.editingHandler;
import me.william278.huskhomes2.messageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class editWarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length >= 1) {
                String warpName = args[0];
                if (dataManager.warpExists(warpName)) {
                    if (args.length >= 2) {
                        switch (args[1]) {
                            case "location":
                                dataManager.updateWarpLocation(warpName, p.getLocation());
                                messageManager.sendMessage(p, "edit_warp_update_location", warpName);
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
                                        messageManager.sendMessage(p, "error_edit_warp_invalid_description");
                                        return true;
                                    }

                                    // Update description
                                    dataManager.updateWarpDescription(warpName, newDescriptionString);

                                    // Confirmation message
                                    messageManager.sendMessage(p, "edit_warp_update_description", warpName, newDescriptionString);
                                } else {
                                    messageManager.sendMessage(p, "error_invalid_syntax", "/editwarp <warp> description <new description>");
                                }
                                return true;
                            case "rename":
                                if (args.length >= 3) {
                                    String newName = args[2];
                                    if (newName.length() > 16) {
                                        messageManager.sendMessage(p, "error_set_warp_invalid_length");
                                        return true;
                                    }
                                    if (!newName.matches("[A-Za-z0-9_\\-]+")) {
                                        messageManager.sendMessage(p, "error_set_warp_invalid_characters");
                                        return true;
                                    }
                                    if (dataManager.warpExists(newName)) {
                                        messageManager.sendMessage(p, "error_set_warp_name_taken");
                                        return true;
                                    }
                                    dataManager.updateWarpName(warpName, newName);
                                    messageManager.sendMessage(p, "edit_warp_update_name", warpName, newName);
                                } else {
                                    messageManager.sendMessage(p, "error_invalid_syntax", "/editwarp <warp> rename <new warp>");
                                }
                                return true;
                            default:
                                messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                                return true;
                        }
                    } else {
                        Warp warp = dataManager.getWarp(warpName);
                        editingHandler.showEditWarpOptions(p, warp);
                        return true;
                    }
                } else {
                    messageManager.sendMessage(p, "error_warp_invalid", warpName);
                }
            } else {
                messageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
            }
            return true;
        }
        return false;
    }
}
