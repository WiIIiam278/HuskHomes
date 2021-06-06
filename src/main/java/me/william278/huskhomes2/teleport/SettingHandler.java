package me.william278.huskhomes2.teleport;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerDeleteHomeEvent;
import me.william278.huskhomes2.api.events.PlayerDeleteWarpEvent;
import me.william278.huskhomes2.api.events.PlayerSetHomeEvent;
import me.william278.huskhomes2.api.events.PlayerSetWarpEvent;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.commands.PublicHomeCommand;
import me.william278.huskhomes2.commands.WarpCommand;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.DynMapIntegration;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import me.william278.huskhomes2.util.RegexUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

// This class handles setting homes, warps and the spawn location.
public class SettingHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Set a home at the specified position
    public static void setHome(Location location, Player player, String name) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SetHomeConditions setHomeConditions = new SetHomeConditions(player, name, connection);
                if (setHomeConditions.areConditionsMet()) {
                    Home home = new Home(location, HuskHomes.getSettings().getServerID(), player, name, false);
                    PlayerSetHomeEvent event = new PlayerSetHomeEvent(player, home);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(event);
                    });
                    DataManager.addHome(home, player, connection);
                    MessageManager.sendMessage(player, "set_home_success", name);
                    HomeCommand.Tab.updatePlayerHomeCache(player);
                } else {
                    switch (setHomeConditions.getConditionsNotMetReason()) {
                        case "error_set_home_maximum_homes":
                            MessageManager.sendMessage(player, "error_set_home_maximum_homes", Integer.toString(HuskHomes.getSettings().getMaximumHomes()));
                            return;
                        case "error_insufficient_funds":
                            MessageManager.sendMessage(player, "error_insufficient_funds", VaultIntegration.format(HuskHomes.getSettings().getSetHomeCost()));
                            return;
                        default:
                            MessageManager.sendMessage(player, setHomeConditions.getConditionsNotMetReason());
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    public static void updateCrossServerSpawnWarp(Location location, Player p) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (SettingHandler.setCrossServerSpawnWarp(p.getLocation(), p, connection)) {
                    SettingHandler.setSpawnLocation(p.getLocation());
                    p.getLocation().getWorld().setSpawnLocation(p.getLocation());
                    MessageManager.sendMessage(p, "set_spawn_success");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    // Set a new server spawn (override existing one if exists)
    public static boolean setCrossServerSpawnWarp(Location location, Player player, Connection connection) throws SQLException {
        String spawnWarpName = HuskHomes.getSettings().getSpawnWarpName();

        // Delete the old spawn warp
        if (DataManager.warpExists(spawnWarpName, connection)) {
            DataManager.deleteWarp(spawnWarpName, connection);
            if (HuskHomes.getSettings().doDynMap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                DynMapIntegration.removeDynamicMapMarker(spawnWarpName);
            }
        }

        // Set a new warp for the spawn position
        SetWarpConditions setWarpConditions = new SetWarpConditions(spawnWarpName, connection);
        if (setWarpConditions.areConditionsMet()) {
            Warp spawnWarp = new Warp(location, HuskHomes.getSettings().getServerID(), spawnWarpName);
            spawnWarp.setDescription(MessageManager.getRawMessage("spawn_warp_default_description"));
            DataManager.addWarp(spawnWarp, connection);
            if (HuskHomes.getSettings().doDynMap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                DynMapIntegration.addDynamicMapMarker(spawnWarp);
            }
            WarpCommand.Tab.updateWarpsTabCache();
        } else {
            MessageManager.sendMessage(player, setWarpConditions.getConditionsNotMetReason());
            return false;
        }
        return true;
    }

    // Set a warp at the specified position
    public static void setWarp(Location location, Player player, String name) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SetWarpConditions setWarpConditions = new SetWarpConditions(name, connection);
                if (setWarpConditions.areConditionsMet()) {
                    Warp warp = new Warp(location, HuskHomes.getSettings().getServerID(), name);
                    PlayerSetWarpEvent event = new PlayerSetWarpEvent(player, warp);
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
                    DataManager.addWarp(warp, connection);
                    MessageManager.sendMessage(player, "set_warp_success", name);
                    if (HuskHomes.getSettings().doDynMap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                        DynMapIntegration.addDynamicMapMarker(warp);
                    }
                    WarpCommand.Tab.updateWarpsTabCache();
                } else {
                    MessageManager.sendMessage(player, setWarpConditions.getConditionsNotMetReason());
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    private static BaseComponent[] deletionConfirmationButton(String commandType) {
        BaseComponent[] buttonComponents = new MineDown(MessageManager.getRawMessage("delete_confirmation_button")).urlDetection(false).toComponent();
        for (BaseComponent baseComponent : buttonComponents) {
            baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/huskhomes:" + commandType + " all confirm")));
            baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(MessageManager.getRawMessage("delete_confirmation_button_tooltip")).color(ChatColor.RED).italic(false).create())));
        }
        return buttonComponents;
    }

    private static void sendDeletionConfirmationWarning(Player player, String deletionType) {
        // Send the delete-all confirmation warning
        MessageManager.sendMessage(player, "delete_all_" + deletionType + "s_confirm");
}

    // Delete all of a player's homes
    public static void deleteAllHomes(Player player) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int homesDeleted = 0;
                for (Home home : DataManager.getPlayerHomes(player.getName(), connection)) {
                    if (home != null) {
                        String homeName = home.getName();
                        if (home.isPublic()) {
                            // Delete Dynmap marker if it exists & if the home is public
                            if (HuskHomes.getSettings().doDynMap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                                DynMapIntegration.removeDynamicMapMarker(homeName, player.getName());
                            }
                            PlayerDeleteHomeEvent event = new PlayerDeleteHomeEvent(player, home);
                            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
                            DataManager.deleteHome(homeName, player, connection);
                            PublicHomeCommand.updatePublicHomeTabCache();
                        } else {
                            DataManager.deleteHome(homeName, player, connection);
                        }
                    }
                    homesDeleted++;
                }
                if (homesDeleted == 0) {
                    MessageManager.sendMessage(player, "error_no_homes_set");
                    return;
                }

                HomeCommand.Tab.updatePlayerHomeCache(player);
                MessageManager.sendMessage(player, "delete_all_homes_success", Integer.toString(homesDeleted));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    // Delete a home
    public static void deleteHome(Player player, String homeName) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (DataManager.homeExists(player, homeName, connection)) {
                    Home home = DataManager.getHome(player.getName(), homeName, connection);
                    if (home != null) {
                        if (home.isPublic()) {
                            // Delete Dynmap marker if it exists & if the home is public
                            if (HuskHomes.getSettings().doDynMap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                                DynMapIntegration.removeDynamicMapMarker(homeName, player.getName());
                            }
                            PlayerDeleteHomeEvent event = new PlayerDeleteHomeEvent(player, home);
                            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
                            DataManager.deleteHome(homeName, player, connection);
                            PublicHomeCommand.updatePublicHomeTabCache();
                        } else {
                            DataManager.deleteHome(homeName, player, connection);
                        }
                        HomeCommand.Tab.updatePlayerHomeCache(player);
                        MessageManager.sendMessage(player, "home_deleted", homeName);
                    } else {
                        MessageManager.sendMessage(player, "error_home_invalid", homeName);
                    }
                } else {
                    if (homeName.equalsIgnoreCase("all")) {
                        if (DataManager.getPlayerHomes(player.getName(), connection).size() == 0) {
                            MessageManager.sendMessage(player, "error_no_homes_set");
                            return;
                        }
                        sendDeletionConfirmationWarning(player, "home");
                        return;
                    }
                    MessageManager.sendMessage(player, "error_home_invalid", homeName);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    // Delete all server warps
    public static void deleteAllWarps(Player player) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int warpsDeleted = 0;
                for (Warp warp : DataManager.getWarps(connection)) {
                    if (warp != null) {
                        String warpName = warp.getName();
                        PlayerDeleteWarpEvent event = new PlayerDeleteWarpEvent(player, warp);
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
                        DataManager.deleteWarp(warpName, connection);
                        if (HuskHomes.getSettings().doDynMap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                            DynMapIntegration.removeDynamicMapMarker(warpName);
                        }
                        WarpCommand.Tab.updateWarpsTabCache();
                    }
                    warpsDeleted++;
                }
                if (warpsDeleted == 0) {
                    MessageManager.sendMessage(player, "error_no_warps_set");
                    return;
                }

                HomeCommand.Tab.updatePlayerHomeCache(player);
                MessageManager.sendMessage(player, "delete_all_warps_success", Integer.toString(warpsDeleted));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    // Delete a warp
    public static void deleteWarp(Player player, String warpName) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (DataManager.warpExists(warpName, connection)) {
                    Warp warp = DataManager.getWarp(warpName, connection);
                    PlayerDeleteWarpEvent event = new PlayerDeleteWarpEvent(player, warp);
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
                    DataManager.deleteWarp(warpName, connection);
                    MessageManager.sendMessage(player, "warp_deleted", warpName);
                    if (HuskHomes.getSettings().doDynMap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                        DynMapIntegration.removeDynamicMapMarker(warpName);
                    }
                    WarpCommand.Tab.updateWarpsTabCache();
                } else {
                    if (warpName.equalsIgnoreCase("all")) {
                        if (DataManager.getWarps(connection).size() == 0) {
                            MessageManager.sendMessage(player, "error_no_warps_set");
                            return;
                        }
                        sendDeletionConfirmationWarning(player, "warp");
                        return;
                    }
                    MessageManager.sendMessage(player, "error_warp_invalid", warpName);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    // Set spawn location
    public static void setSpawnLocation(Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Write the new location to config
            FileConfiguration config = plugin.getConfig();
            config.set("spawn_command.position.world", location.getWorld().getName());
            config.set("spawn_command.position.x", location.getX());
            config.set("spawn_command.position.y", location.getY());
            config.set("spawn_command.position.z", location.getZ());
            config.set("spawn_command.position.yaw", (double) location.getYaw());
            config.set("spawn_command.position.pitch", (double) location.getPitch());
            plugin.saveConfig();

            // Update the current spawn location
            TeleportManager.setSpawnLocation(new TeleportationPoint(location, HuskHomes.getSettings().getServerID()));
        });
    }

    // Update current spawn location from config
    public static void fetchSpawnLocation() {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TeleportManager.setSpawnLocation(getSpawnLocation());
        });
    }

    // Get spawn location from config
    private static TeleportationPoint getSpawnLocation() {
        String server = HuskHomes.getSettings().getServerID();
        try {
            FileConfiguration config = plugin.getConfig();
            String worldName = (config.getString("spawn_command.position.world"));
            if (worldName == null || worldName.equals("")) {
                return null;
            }
            double x = (config.getDouble("spawn_command.position.x"));
            double y = (config.getDouble("spawn_command.position.y"));
            double z = (config.getDouble("spawn_command.position.z"));
            float yaw = (float) (config.getDouble("spawn_command.position.yaw"));
            float pitch = (float) (config.getDouble("spawn_command.position.pitch"));
            return new TeleportationPoint(worldName, x, y, z, yaw, pitch, server);
        } catch (Exception e) {
            return null;
        }
    }

    private static class SetWarpConditions {

        private boolean conditionsMet;
        private String conditionsNotMetReason;

        public SetWarpConditions(String warpName, Connection connection) throws SQLException {
            conditionsMet = false;
            if (DataManager.warpExists(warpName, connection)) {
                conditionsNotMetReason = "error_set_warp_name_taken";
                return;
            }
            if (warpName.length() > 16) {
                conditionsNotMetReason = "error_set_warp_invalid_length";
                return;
            }
            if (!RegexUtil.NAME_PATTERN.matcher(warpName).matches()) {
                conditionsNotMetReason = "error_set_warp_invalid_characters";
                return;
            }
            conditionsMet = true;
        }

        public boolean areConditionsMet() {
            return conditionsMet;
        }

        public String getConditionsNotMetReason() {
            return conditionsNotMetReason;
        }
    }

    private static class SetHomeConditions {

        private boolean conditionsMet;
        private String conditionsNotMetReason;

        public SetHomeConditions(Player player, String homeName, Connection connection) throws SQLException {
            conditionsMet = false;
            int currentHomeCount = DataManager.getPlayerHomeCount(player, connection);
            if (currentHomeCount > (Home.getSetHomeLimit(player) - 1)) {
                conditionsNotMetReason = "error_set_home_maximum_homes";
                return;
            }
            if (DataManager.homeExists(player, homeName, connection)) {
                conditionsNotMetReason = "error_set_home_name_taken";
                return;
            }
            if (homeName.length() > 16) {
                conditionsNotMetReason = "error_set_home_invalid_length";
                return;
            }
            if (!RegexUtil.NAME_PATTERN.matcher(homeName).matches()) {
                conditionsNotMetReason = "error_set_home_invalid_characters";
                return;
            }
            if (HuskHomes.getSettings().doEconomy()) {
                double setHomeCost = HuskHomes.getSettings().getSetHomeCost();
                if (setHomeCost > 0) {
                    int currentPlayerHomeSlots = DataManager.getPlayerHomeSlots(player, connection);
                    if (currentHomeCount > (currentPlayerHomeSlots - 1)) {
                        if (!VaultIntegration.takeMoney(player, setHomeCost)) {
                            conditionsNotMetReason = "error_insufficient_funds";
                            return;
                        } else {
                            DataManager.incrementPlayerHomeSlots(player, connection);
                            MessageManager.sendMessage(player, "set_home_spent_money", VaultIntegration.format(setHomeCost));
                        }
                    } else if (currentHomeCount == (currentPlayerHomeSlots - 1)) {
                        MessageManager.sendMessage(player, "set_home_used_free_slots", Integer.toString(Home.getFreeHomes(player)), VaultIntegration.format(setHomeCost));
                    }
                }
            }
            conditionsMet = true;
        }

        public boolean areConditionsMet() {
            return conditionsMet;
        }

        public String getConditionsNotMetReason() {
            return conditionsNotMetReason;
        }
    }
}
