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
import me.william278.huskhomes2.utils.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class EditHomeCommand extends CommandBase implements TabCompleter {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    @Override
    protected void onCommand(Player p, Command command, String label, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
            return;
        }

        String homeName = args[0];
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!DataManager.homeExists(p.getName(), homeName, connection)) {
                    MessageManager.sendMessage(p, "error_home_invalid", homeName);
                    return;
                }

                if (args.length == 1) {
                    Home home = DataManager.getHome(p.getName(), homeName, connection);
                    EditingHandler.showEditHomeOptions(p, home);
                    return;
                }

                switch (args[1].toLowerCase(Locale.ENGLISH)) {
                    case "location":
                        Location newLocation = p.getLocation();
                        TeleportationPoint newTeleportLocation = new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID());

                        // Remove old marker if on dynmap
                        Home locationMovedHome = DataManager.getHome(p.getName(), homeName, connection);
                        if (locationMovedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                            DynMapIntegration.removeDynamicMapMarker(homeName, p.getName());
                        }

                        PlayerRelocateHomeEvent relocateHomeEvent = new PlayerRelocateHomeEvent(p, locationMovedHome, newTeleportLocation);
                        Bukkit.getPluginManager().callEvent(relocateHomeEvent);
                        if (relocateHomeEvent.isCancelled()) {
                            return;
                        }

                        DataManager.updateHomeLocation(p.getName(), homeName, newLocation, connection);
                        MessageManager.sendMessage(p, "edit_home_update_location", homeName);

                        // Add new updated marker if using dynmap
                        locationMovedHome.setLocation(newLocation, HuskHomes.getSettings().getServerID());
                        if (locationMovedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                            DynMapIntegration.addDynamicMapMarker(locationMovedHome);
                        }
                        return;
                    case "description":
                        if (args.length >= 3) {
                            Home descriptionChangedHome = DataManager.getHome(p.getName(), homeName, connection);

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
                                return;
                            }
                            // Check the description is valid
                            if (newDescriptionString.length() > 255 || !RegexUtil.DESCRIPTION_PATTERN.matcher(newDescriptionString).matches()) {
                                MessageManager.sendMessage(p, "error_edit_home_invalid_description");
                                return;
                            }

                            // Remove old marker if on the Dynmap
                            if (descriptionChangedHome.isPublic() && HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                                DynMapIntegration.removeDynamicMapMarker(homeName, p.getName());
                            }

                            // Update description
                            DataManager.updateHomeDescription(p.getName(), homeName, newDescriptionString, connection);

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
                        return;
                    case "rename":
                        if (args.length >= 3) {
                            String newName = args[2];
                            if (newName.length() > 16) {
                                MessageManager.sendMessage(p, "error_set_home_invalid_length");
                                return;
                            }
                            if (!RegexUtil.NAME_PATTERN.matcher(newName).matches()) {
                                MessageManager.sendMessage(p, "error_set_home_invalid_characters");
                                return;
                            }
                            if (DataManager.homeExists(p.getName(), newName, connection)) {
                                MessageManager.sendMessage(p, "error_set_home_name_taken");
                                return;
                            }
                            Home renamedHome = DataManager.getHome(p.getName(), homeName, connection);
                            PlayerRenameHomeEvent renameHomeEvent = new PlayerRenameHomeEvent(p, renamedHome, newName);
                            Bukkit.getPluginManager().callEvent(renameHomeEvent);
                            if (renameHomeEvent.isCancelled()) {
                                return;
                            }
                            if (renamedHome.isPublic()) {
                                if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                                    DynMapIntegration.removeDynamicMapMarker(homeName, p.getName());
                                }
                                DataManager.updateHomeName(p.getName(), homeName, newName, connection);
                                PublicHomeCommand.updatePublicHomeTabCache();
                            } else {
                                DataManager.updateHomeName(p.getName(), homeName, newName, connection);
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
                        return;
                    case "public":
                        Home privateHome = DataManager.getHome(p.getName(), homeName, connection);
                        if (!privateHome.isPublic()) {
                            if (HuskHomes.getSettings().doEconomy()) {
                                double publicHomeCost = HuskHomes.getSettings().getPublicHomeCost();
                                if (publicHomeCost > 0) {
                                    if (!VaultIntegration.takeMoney(p, publicHomeCost)) {
                                        MessageManager.sendMessage(p, "error_insufficient_funds", VaultIntegration.format(publicHomeCost));
                                        return;
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
                                return;
                            }
                            DataManager.updateHomePrivacy(p.getName(), homeName, true, connection);
                            PublicHomeCommand.updatePublicHomeTabCache();
                            if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                                DynMapIntegration.addDynamicMapMarker(privateHome);
                            }
                        } else {
                            MessageManager.sendMessage(p, "error_edit_home_privacy_already_public", homeName);
                        }
                        return;
                    case "private":
                        Home publicHome = DataManager.getHome(p.getName(), homeName, connection);
                        if (publicHome.isPublic()) {
                            PlayerMakeHomePrivateEvent makeHomePrivateEvent = new PlayerMakeHomePrivateEvent(p, publicHome);
                            Bukkit.getPluginManager().callEvent(makeHomePrivateEvent);
                            if (makeHomePrivateEvent.isCancelled()) {
                                return;
                            }
                            DataManager.updateHomePrivacy(p.getName(), homeName, false, connection);
                            MessageManager.sendMessage(p, "edit_home_privacy_private_success", homeName);
                            if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                                DynMapIntegration.removeDynamicMapMarker(publicHome.getName(), publicHome.getOwnerUsername());
                            }
                            PublicHomeCommand.updatePublicHomeTabCache();
                        } else {
                            MessageManager.sendMessage(p, "error_edit_home_privacy_already_private", homeName);
                        }
                        return;
                    default:
                        MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home.");
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("huskhomes.edithome")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], HomeCommand.Tab.getHomeTabCache().get(p.getUniqueId()), tabCompletions);
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
