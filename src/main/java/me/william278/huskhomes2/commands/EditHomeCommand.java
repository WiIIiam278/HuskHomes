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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String homeName = args[0];
            String ownerName = p.getName();
            if (RegexUtil.OWNER_NAME_PATTERN.matcher(args[0]).matches()) {
                ownerName = args[0].split("\\.")[0];
                homeName = args[0].split("\\.")[1];
            }
            if (!ownerName.equalsIgnoreCase(p.getName())) {
                if (!p.hasPermission("huskhomes.edithome.other")) {
                    MessageManager.sendMessage(p, "error_no_permission");
                    return;
                }
            }

            try (Connection connection = HuskHomes.getConnection()) {
                if (!DataManager.homeExists(ownerName, homeName, connection)) {
                    if (!ownerName.equalsIgnoreCase(p.getName())) {
                        MessageManager.sendMessage(p, "error_home_invalid_other", ownerName, homeName);
                    } else {
                        MessageManager.sendMessage(p, "error_home_invalid", homeName);
                    }
                    return;
                }
                Home home = DataManager.getHome(ownerName, homeName, connection);
                assert home != null;

                if (args.length == 1) {
                    EditingHandler.showEditHomeOptions(p, home);
                    return;
                }

                switch (args[1].toLowerCase(Locale.ENGLISH)) {
                    case "location" -> editHomeLocation(p, home);
                    case "public" -> editHomePrivacy(p, home, true);
                    case "private" -> editHomePrivacy(p, home, false);
                    case "description" -> {
                        if (args.length >= 3) {
                            // Get the new description
                            editHomeDescription(p, home, buildDescription(args));
                        } else {
                            MessageManager.sendMessage(p, "error_invalid_syntax", "/edithome <home> description <new description>");
                        }
                    }
                    case "rename" -> {
                        if (args.length >= 3) {
                            editHomeName(p, home, args[2], connection);
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

    private void editHomeLocation(Player p, Home locationMovedHome) throws SQLException {
        Location newLocation = p.getLocation();
        TeleportationPoint newTeleportLocation = new TeleportationPoint(newLocation, HuskHomes.getSettings().getServerID());

        assert locationMovedHome != null;
        PlayerRelocateHomeEvent relocateHomeEvent = new PlayerRelocateHomeEvent(p, locationMovedHome, newTeleportLocation);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(relocateHomeEvent);
            if (relocateHomeEvent.isCancelled()) {
                return;
            }
            try (Connection connection = HuskHomes.getConnection()) {
                // Remove old marker on map
                if (locationMovedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                    HuskHomes.getMap().removePublicHomeMarker(locationMovedHome.getName(), locationMovedHome.getOwnerUsername());
                }

                DataManager.updateHomeLocation(locationMovedHome.getOwnerUsername(), locationMovedHome.getName(), newLocation, connection);
                MessageManager.sendMessage(p, "edit_home_update_location", locationMovedHome.getName());

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

    private void editHomeDescription(Player p, Home descriptionChangedHome, String newDescriptionString) throws SQLException {
        assert descriptionChangedHome != null;
        PlayerChangeHomeDescriptionEvent changeHomeDescriptionEvent = new PlayerChangeHomeDescriptionEvent(p, descriptionChangedHome, newDescriptionString);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(changeHomeDescriptionEvent);
            if (changeHomeDescriptionEvent.isCancelled()) {
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
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
                        HuskHomes.getMap().removePublicHomeMarker(descriptionChangedHome.getName(), descriptionChangedHome.getOwnerUsername());
                    }

                    // Update description
                    DataManager.updateHomeDescription(descriptionChangedHome.getOwnerUsername(), descriptionChangedHome.getName(), newDescriptionString, connection);

                    // Add new marker if using a map integration
                    descriptionChangedHome.setDescription(newDescriptionString);
                    if (descriptionChangedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                        HuskHomes.getMap().addPublicHomeMarker(descriptionChangedHome);
                    }

                    // Confirmation message
                    MessageManager.sendMessage(p, "edit_home_update_description", descriptionChangedHome.getName(), MineDown.escape(newDescriptionString.replace("]", "\\]").replace("[", "\\[").replace("(", "\\(").replace(")", "\\)")));
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home description.");
                }
            });
        });
    }

    private void editHomeName(Player p, Home renamedHome, String newName, Connection connection) throws SQLException {
        if (newName.length() > 16) {
            MessageManager.sendMessage(p, "error_set_home_invalid_length");
            return;
        }
        if (!RegexUtil.NAME_PATTERN.matcher(newName).matches()) {
            MessageManager.sendMessage(p, "error_set_home_invalid_characters");
            return;
        }
        if (DataManager.homeExists(renamedHome.getOwnerUsername(), newName, connection)) {
            MessageManager.sendMessage(p, "error_set_home_name_taken");
            return;
        }
        final String oldHomeName = renamedHome.getName();
        PlayerRenameHomeEvent renameHomeEvent = new PlayerRenameHomeEvent(p, renamedHome, newName);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(renameHomeEvent);
            if (renameHomeEvent.isCancelled()) {
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection1 = HuskHomes.getConnection()) {
                    if (renamedHome.isPublic()) {
                        if (HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                            HuskHomes.getMap().removePublicHomeMarker(oldHomeName, renamedHome.getOwnerUsername());
                        }
                        DataManager.updateHomeName(renamedHome.getOwnerUsername(), oldHomeName, newName, connection1);
                        PublicHomeCommand.updatePublicHomeTabCache();
                    } else {
                        DataManager.updateHomeName(renamedHome.getOwnerUsername(), oldHomeName, newName, connection1);
                    }
                    renamedHome.setName(newName);
                    if (renamedHome.isPublic() && HuskHomes.getSettings().doMapIntegration() && HuskHomes.getSettings().showPublicHomesOnMap()) {
                        HuskHomes.getMap().addPublicHomeMarker(renamedHome);
                    }
                    HomeCommand.Tab.updatePlayerHomeCache(p);
                    MessageManager.sendMessage(p, "edit_home_update_name", oldHomeName, newName);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred editing a home name.");
                }
            });
        });
    }

    private void editHomePrivacy(Player p, Home privacyUpdatingHome, boolean makingHomePublic) throws SQLException {
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
                            MessageManager.sendMessage(p, "edit_home_privacy_public_success_economy", privacyUpdatingHome.getName(), VaultIntegration.format(publicHomeCost));
                        }
                    } else {
                        MessageManager.sendMessage(p, "edit_home_privacy_public_success", privacyUpdatingHome.getName());
                    }
                } else {
                    MessageManager.sendMessage(p, "edit_home_privacy_public_success", privacyUpdatingHome.getName());
                }
                PlayerMakeHomePublicEvent makeHomePublicEvent = new PlayerMakeHomePublicEvent(p, privacyUpdatingHome);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(makeHomePublicEvent);
                    if (makeHomePublicEvent.isCancelled()) {
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try (Connection connection = HuskHomes.getConnection()) {
                            DataManager.updateHomePrivacy(privacyUpdatingHome.getOwnerUsername(), privacyUpdatingHome.getName(), true, connection);
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
                MessageManager.sendMessage(p, "error_edit_home_privacy_already_public", privacyUpdatingHome.getName());
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
                        try (Connection connection = HuskHomes.getConnection()) {
                            DataManager.updateHomePrivacy(privacyUpdatingHome.getOwnerUsername(), privacyUpdatingHome.getName(), false, connection);
                            MessageManager.sendMessage(p, "edit_home_privacy_private_success", privacyUpdatingHome.getName());
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
                MessageManager.sendMessage(p, "error_edit_home_privacy_already_private", privacyUpdatingHome.getName());
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
