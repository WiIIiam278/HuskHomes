package me.william278.huskhomes2.commands;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.EditingHandler;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerChangeHomeDescriptionEvent;
import me.william278.huskhomes2.api.events.PlayerMakeHomePrivateEvent;
import me.william278.huskhomes2.api.events.PlayerMakeHomePublicEvent;
import me.william278.huskhomes2.api.events.PlayerRelocateHomeEvent;
import me.william278.huskhomes2.api.events.PlayerRenameHomeEvent;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
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
                    assert home != null;
                    EditingHandler.showEditHomeOptions(p, home);
                    return;
                }

                switch (args[1].toLowerCase(Locale.ENGLISH)) {
                    case "location" -> editHomeLocation(p, homeName, connection);
                    case "public" -> editHomePrivacy(p, homeName, true, connection);
                    case "private" -> editHomePrivacy(p, homeName, false, connection);
                    case "description" -> {
                        if (args.length >= 3) {
                            // Get the new description
                            editHomeDescription(p, homeName, buildDescription(args), connection);
                        } else {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> description <new description>");
                        }
                    }
                    case "rename" -> {
                        if (args.length >= 3) {
                            editHomeName(p, homeName, args[2], connection);
                        } else {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> rename <new name>");
                        }
                    }
                    default -> MessageManager.sendMessage(p, "error_invalid_syntax", command.getUsage());
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home.");
            }
        });
    }

    public static String buildDescription(String[] args) {
        StringBuilder newDescription = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) {
                newDescription.append(" ").append(args[i]);
            } else {
                newDescription = new StringBuilder(args[i]);
            }
        }
        return newDescription.toString();
    }

    private void editHomeLocation(Player p, String homeName, Connection connection) throws SQLException {
        Location newLocation = p.getLocation();
        TeleportationPoint newTeleportLocation = new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID());

        Home locationMovedHome = DataManager.getHome(p.getName(), homeName, connection);
        assert locationMovedHome != null;
        PlayerRelocateHomeEvent relocateHomeEvent = new PlayerRelocateHomeEvent(p, locationMovedHome, newTeleportLocation);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(relocateHomeEvent);
            if (relocateHomeEvent.isCancelled()) {
                return;
            }
            try {
                // Remove old marker on map
                if (locationMovedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                    HuskHomes.getMap().removePublicHomeMarker(homeName, p.getName());
                }

                DataManager.updateHomeLocation(p.getName(), homeName, newLocation, connection);
                MessageManager.sendMessage(p, "edit_home_update_location", homeName);

                // Add new updated marker on map
                locationMovedHome.setLocation(newLocation, HuskHomes.getSettings().getServerID());
                if (locationMovedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                    HuskHomes.getMap().addPublicHomeMarker(locationMovedHome);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred updating a home's location.");
            }
        });
    }

    private void editHomeDescription(Player p, String homeName, String newDescriptionString, Connection connection) throws SQLException {
        Home descriptionChangedHome = DataManager.getHome(p.getName(), homeName, connection);
        assert descriptionChangedHome != null;
        PlayerChangeHomeDescriptionEvent changeHomeDescriptionEvent = new PlayerChangeHomeDescriptionEvent(p, descriptionChangedHome, newDescriptionString);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(changeHomeDescriptionEvent);
            if (changeHomeDescriptionEvent.isCancelled()) {
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    // Check the description is valid
                    if (newDescriptionString.length() > 255) {
                        MessageManager.sendMessage(p, "error_edit_home_description_length");
                        return;
                    }
                    if (!HuskHomes.getSettings().doUnicodeInDescriptions()) {
                        if (!RegexUtil.DESCRIPTION_PATTERN.matcher(newDescriptionString).matches()) {
                            MessageManager.sendMessage(p, "error_edit_home_description_characters");
                            return;
                        }
                    }

                    // Remove old marker if using a map integration
                    if (descriptionChangedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                        HuskHomes.getMap().removePublicHomeMarker(homeName, p.getName());
                    }

                    // Update description
                    DataManager.updateHomeDescription(p.getName(), homeName, newDescriptionString, connection);

                    // Add new marker if using a map integration
                    descriptionChangedHome.setDescription(newDescriptionString);
                    if (descriptionChangedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                        HuskHomes.getMap().addPublicHomeMarker(descriptionChangedHome);
                    }

                    // Confirmation message
                    MessageManager.sendMessage(p, "edit_home_update_description", homeName, MineDown.escape(newDescriptionString.replace("]", "］").replace("[", "［").replace("(", "❲").replace(")", "❳")));
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home description.");
                }
            });
        });
    }

    private void editHomeName(Player p, String homeName, String newName, Connection connection) throws SQLException {
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
        assert renamedHome != null;
        PlayerRenameHomeEvent renameHomeEvent = new PlayerRenameHomeEvent(p, renamedHome, newName);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(renameHomeEvent);
            if (renameHomeEvent.isCancelled()) {
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    if (renamedHome.isPublic()) {
                        if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                            HuskHomes.getMap().removePublicHomeMarker(homeName, p.getName());
                        }
                        DataManager.updateHomeName(p.getName(), homeName, newName, connection);
                        PublicHomeCommand.updatePublicHomeTabCache();
                    } else {
                        DataManager.updateHomeName(p.getName(), homeName, newName, connection);
                    }
                    renamedHome.setName(newName);
                    if (renamedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                        HuskHomes.getMap().addPublicHomeMarker(renamedHome);
                    }
                    HomeCommand.Tab.updatePlayerHomeCache(p);
                    MessageManager.sendMessage(p, "edit_home_update_name", homeName, newName);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home name.");
                }
            });
        });
    }

    private void editHomePrivacy(Player p, String homeName, boolean makingHomePublic, Connection connection) throws SQLException {
        Home privacyUpdatingHome = DataManager.getHome(p.getName(), homeName, connection);
        assert privacyUpdatingHome != null;
        if (makingHomePublic) {
            // Making a private home
            if (!privacyUpdatingHome.isPublic()) {
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
                PlayerMakeHomePublicEvent makeHomePublicEvent = new PlayerMakeHomePublicEvent(p, privacyUpdatingHome);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(makeHomePublicEvent);
                    if (makeHomePublicEvent.isCancelled()) {
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            DataManager.updateHomePrivacy(p.getName(), homeName, true, connection);
                            PublicHomeCommand.updatePublicHomeTabCache();
                            if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                                HuskHomes.getMap().addPublicHomeMarker(privacyUpdatingHome);
                            }
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home's privacy.");
                        }
                    });
                });
            } else {
                MessageManager.sendMessage(p, "error_edit_home_privacy_already_public", homeName);
            }
        } else {
            if (privacyUpdatingHome.isPublic()) {
                PlayerMakeHomePrivateEvent makeHomePrivateEvent = new PlayerMakeHomePrivateEvent(p, privacyUpdatingHome);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(makeHomePrivateEvent);
                    if (makeHomePrivateEvent.isCancelled()) {
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            DataManager.updateHomePrivacy(p.getName(), homeName, false, connection);
                            MessageManager.sendMessage(p, "edit_home_privacy_private_success", homeName);
                            if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                                HuskHomes.getMap().removePublicHomeMarker(privacyUpdatingHome.getName(), privacyUpdatingHome.getOwnerUsername());
                            }
                            PublicHomeCommand.updatePublicHomeTabCache();
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home's privacy.");
                        }
                    });
                });
            } else {
                MessageManager.sendMessage(p, "error_edit_home_privacy_already_private", homeName);
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        Player p = (Player) sender;
        if (!p.hasPermission("huskhomes.edithome")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            final List<String> tabCompletions = new ArrayList<>();
            final List<String> homes = HomeCommand.Tab.getHomeTabCache().get(p.getUniqueId());
            if (homes == null) {
                HomeCommand.Tab.updatePlayerHomeCache(p);
                return Collections.emptyList();
            }
            StringUtil.copyPartialMatches(args[0], homes, tabCompletions);
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
