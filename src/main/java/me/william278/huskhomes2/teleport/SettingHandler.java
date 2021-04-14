package me.william278.huskhomes2.teleport;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerDeleteHomeEvent;
import me.william278.huskhomes2.api.events.PlayerDeleteWarpEvent;
import me.william278.huskhomes2.api.events.PlayerSetHomeEvent;
import me.william278.huskhomes2.api.events.PlayerSetWarpEvent;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.commands.PublichomeCommand;
import me.william278.huskhomes2.commands.WarpCommand;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.DynMapIntegration;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import me.william278.huskhomes2.utils.RegexUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

// This class handles setting homes, warps and the spawn location.
public class SettingHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Set a home at the specified position
    public static void setHome(Location location, Player player, String name) {
        SetHomeConditions setHomeConditions = new SetHomeConditions(player, name);
        if (setHomeConditions.areConditionsMet()) {
            Home home = new Home(location, HuskHomes.getSettings().getServerID(), player, name, false);
            PlayerSetHomeEvent event = new PlayerSetHomeEvent(player, home);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            DataManager.addHome(home, player);
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
    }

    // Set a new server spawn (override existing one if exists)
    public static boolean setCrossServerSpawnWarp(Location location, Player player) {
        String spawnWarpName = HuskHomes.getSettings().getSpawnWarpName();

        // Delete the old spawn warp
        if (DataManager.warpExists(spawnWarpName)) {
            DataManager.deleteWarp(spawnWarpName);
            if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                DynMapIntegration.removeDynamicMapMarker(spawnWarpName);
            }
        }

        // Set a new warp for the spawn position
        SetWarpConditions setWarpConditions = new SetWarpConditions(spawnWarpName);
        if (setWarpConditions.areConditionsMet()) {
            Warp spawnWarp = new Warp(location, HuskHomes.getSettings().getServerID(), spawnWarpName);
            spawnWarp.setDescription(MessageManager.getRawMessage("spawn_warp_default_description"));
            DataManager.addWarp(spawnWarp);
            if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
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
        SetWarpConditions setWarpConditions = new SetWarpConditions(name);
        if (setWarpConditions.areConditionsMet()) {
            Warp warp = new Warp(location, HuskHomes.getSettings().getServerID(), name);
            PlayerSetWarpEvent event = new PlayerSetWarpEvent(player, warp);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            DataManager.addWarp(warp);
            MessageManager.sendMessage(player, "set_warp_success", name);
            if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                DynMapIntegration.addDynamicMapMarker(warp);
            }
            WarpCommand.Tab.updateWarpsTabCache();
        } else {
            MessageManager.sendMessage(player, setWarpConditions.getConditionsNotMetReason());
        }
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
        MessageManager.sendMessage(player, "delete_all_irreversible_warning");

        // Send Confirm button
        player.spigot().sendMessage(new ComponentBuilder()
                .append(new MineDown(MessageManager.getRawMessage("option_selection_prompt")).toComponent(), ComponentBuilder.FormatRetention.NONE)
                .append(deletionConfirmationButton("del" + deletionType), ComponentBuilder.FormatRetention.NONE).create());
    }

    // Delete all of a player's homes
    public static void deleteAllHomes(Player player) {
        int homesDeleted = 0;
        for (Home home : DataManager.getPlayerHomes(player.getName())) {
            if (home != null) {
                String homeName = home.getName();
                if (home.isPublic()) {
                    // Delete Dynmap marker if it exists & if the home is public
                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                        DynMapIntegration.removeDynamicMapMarker(homeName, player.getName());
                    }
                    PlayerDeleteHomeEvent event = new PlayerDeleteHomeEvent(player, home);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    DataManager.deleteHome(homeName, player);
                    PublichomeCommand.updatePublicHomeTabCache();
                } else {
                    DataManager.deleteHome(homeName, player);
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
    }

    // Delete a home
    public static void deleteHome(Player player, String homeName) {
        if (DataManager.homeExists(player, homeName)) {
            Home home = DataManager.getHome(player.getName(), homeName);
            if (home != null) {
                if (home.isPublic()) {
                    // Delete Dynmap marker if it exists & if the home is public
                    if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                        DynMapIntegration.removeDynamicMapMarker(homeName, player.getName());
                    }
                    PlayerDeleteHomeEvent event = new PlayerDeleteHomeEvent(player, home);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    DataManager.deleteHome(homeName, player);
                    PublichomeCommand.updatePublicHomeTabCache();
                } else {
                    DataManager.deleteHome(homeName, player);
                }
                HomeCommand.Tab.updatePlayerHomeCache(player);
                MessageManager.sendMessage(player, "home_deleted", homeName);
            } else {
                MessageManager.sendMessage(player, "error_home_invalid", homeName);
            }
        } else {
            if (homeName.equalsIgnoreCase("all")) {
                if (DataManager.getPlayerHomes(player.getName()).size() == 0) {
                    MessageManager.sendMessage(player, "error_no_homes_set");
                    return;
                }
                sendDeletionConfirmationWarning(player, "home");
                return;
            }
            MessageManager.sendMessage(player, "error_home_invalid", homeName);
        }
    }

    // Delete all server warps
    public static void deleteAllWarps(Player player) {
        int warpsDeleted = 0;
        for (Warp warp : DataManager.getWarps()) {
            if (warp != null) {
                String warpName = warp.getName();
                PlayerDeleteWarpEvent event = new PlayerDeleteWarpEvent(player, warp);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                DataManager.deleteWarp(warpName);
                if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
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
    }

    // Delete a warp
    public static void deleteWarp(Player player, String warpName) {
        if (DataManager.warpExists(warpName)) {
            Warp warp = DataManager.getWarp(warpName);
            PlayerDeleteWarpEvent event = new PlayerDeleteWarpEvent(player, warp);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            DataManager.deleteWarp(warpName);
            MessageManager.sendMessage(player, "warp_deleted", warpName);
            if (HuskHomes.getSettings().doDynmap() && HuskHomes.getSettings().showWarpsOnDynmap()) {
                DynMapIntegration.removeDynamicMapMarker(warpName);
            }
            WarpCommand.Tab.updateWarpsTabCache();
        } else {
            if (warpName.equalsIgnoreCase("all")) {
                if (DataManager.getWarps().size() == 0) {
                    MessageManager.sendMessage(player, "error_no_warps_set");
                    return;
                }
                sendDeletionConfirmationWarning(player, "warp");
                return;
            }
            MessageManager.sendMessage(player, "error_warp_invalid", warpName);
        }
    }

    // Set spawn location
    public static void setSpawnLocation(Location location) {
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
    }

    // Update current spawn location from config
    public static void fetchSpawnLocation() {
        TeleportManager.setSpawnLocation(getSpawnLocation());
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

        public SetWarpConditions(String warpName) {
            conditionsMet = false;
            if (DataManager.warpExists(warpName)) {
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

        public SetHomeConditions(Player player, String homeName) {
            conditionsMet = false;
            int currentHomeCount = DataManager.getPlayerHomeCount(player);
            if (currentHomeCount > (Home.getSetHomeLimit(player) - 1)) {
                conditionsNotMetReason = "error_set_home_maximum_homes";
                return;
            }
            if (DataManager.homeExists(player, homeName)) {
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
                    int currentPlayerHomeSlots = DataManager.getPlayerHomeSlots(player);
                    if (currentHomeCount > (currentPlayerHomeSlots - 1)) {
                        if (!VaultIntegration.takeMoney(player, setHomeCost)) {
                            conditionsNotMetReason = "error_insufficient_funds";
                            return;
                        } else {
                            DataManager.incrementPlayerHomeSlots(player);
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
