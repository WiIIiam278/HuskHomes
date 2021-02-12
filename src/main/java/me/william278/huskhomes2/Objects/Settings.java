package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Settings {

    // Message language setting
    String language;

    // Automatically send update HuskHomes reminders
    boolean updateReminders;

    // Bungee settings
    boolean doBungee;
    int clusterID;
    String server;

    // Data storage settings
    String storageType;
    String playerDataTable;
    String locationsDataTable;
    String homesDataTable;
    String warpsDataTable;

    // MySQL connection settings
    int mySQLport;
    String mySQLhost;
    String mySQLdatabase;
    String mySQLusername;
    String mySQLpassword;

    // Dynmap integration settings
    boolean doDynmap;
    boolean dynmapPublicHomes;
    boolean dynmapWarps;
    String dynmapPublicHomeMarkerIconID;
    String dynmapWarpMarkerIconID;
    String dynmapPublicHomeMarkerSet;
    String dynmapWarpMarkerSet;

    // Economy (Vault) integration settings
    boolean doEconomy;
    int freeHomeSlots;
    double setHomeCost;
    double publicHomeCost;
    double rtpCost;

    // Vanished player checks
    boolean checkVanishedPlayers;

    // Time and maximum home settings
    int maximumHomes;
    int teleportRequestExpiryTime;
    int teleportWarmupTime;

    // Sounds
    Sound teleportationCompleteSound;
    Sound teleportWarmupSound;
    Sound teleportCancelledSound;

    // Number of items per page on lists
    int privateHomesPerPage;
    int publicHomesPerPage;
    int warpsPerPage;

    // RTP command settings
    boolean doRtpCommand;
    int rtpRange;
    int rtpCooldown;

    // Command toggle settings
    boolean doSpawnCommand;
    boolean doWarpCommand;

    private void setSettings(FileConfiguration configFile) {
        try {
            this.language = configFile.getString("language");

            this.updateReminders = configFile.getBoolean("check_for_updates");

            this.doBungee = configFile.getBoolean("bungee_options.enable_bungee_mode");
            this.server = configFile.getString("bungee_options.server_id");
            this.clusterID = configFile.getInt("bungee_options.cluster_id");

            this.storageType = configFile.getString("data_storage_options.storage_type");
            this.playerDataTable = configFile.getString("data_storage_options.table_names.player_data");
            this.locationsDataTable = configFile.getString("data_storage_options.table_names.locations_data");
            this.homesDataTable = configFile.getString("data_storage_options.table_names.homes_data");
            this.warpsDataTable = configFile.getString("data_storage_options.table_names.warps_data");

            // Resolve conflict between storage type and bungee mode
            if (storageType.equalsIgnoreCase("sqlite") && doBungee) {
                Bukkit.getLogger().warning("Bungee mode was set in config to be enabled but storage type was set to SQLite!");
                Bukkit.getLogger().warning("A mySQL Database is required to utilise Bungee mode, so bungee mode has been disabled.");
                Bukkit.getLogger().warning("To use Bungee mode and cross-server teleportation, please update your data storage settings to use \"mysql\" and update the connection credentials accordingly.");
                this.doBungee = false;
            }

            this.mySQLhost = configFile.getString("data_storage_options.mysql_credentials.host");
            this.mySQLdatabase = configFile.getString("data_storage_options.mysql_credentials.database");
            this.mySQLusername = configFile.getString("data_storage_options.mysql_credentials.username");
            this.mySQLpassword = configFile.getString("data_storage_options.mysql_credentials.password");
            this.mySQLport = configFile.getInt("data_storage_options.mysql_credentials.port");

            this.doDynmap = configFile.getBoolean("dynmap_integration.enabled");

            if (this.doDynmap) {
                PluginManager pluginManager = HuskHomes.getInstance().getServer().getPluginManager();
                Plugin dynmap = pluginManager.getPlugin("dynmap");
                if (dynmap == null) {
                    Bukkit.getLogger().warning("Dynmap integration was enabled in config, but the Dynmap plugin could not be found!");
                    Bukkit.getLogger().warning("The Dynmap setting has been disabled. Please ensure Dynmap is installed and restart the server.");
                    this.doDynmap = false;
                }
            }

            this.dynmapPublicHomes = configFile.getBoolean("dynmap_integration.markers.public_homes.show");
            this.dynmapWarps = configFile.getBoolean("dynmap_integration.markers.warps.show");
            this.dynmapPublicHomeMarkerIconID = configFile.getString("dynmap_integration.markers.public_homes.icon_id");
            this.dynmapWarpMarkerIconID = configFile.getString("dynmap_integration.markers.warps.icon_id");
            this.dynmapPublicHomeMarkerSet = configFile.getString("dynmap_integration.markers.public_homes.set_name");
            this.dynmapWarpMarkerSet = configFile.getString("dynmap_integration.markers.warps.set_name");

            this.doEconomy = configFile.getBoolean("economy_integration.enabled");

            if (this.doEconomy) {
                PluginManager pluginManager = HuskHomes.getInstance().getServer().getPluginManager();
                Plugin vault = pluginManager.getPlugin("Vault");
                if (vault == null) {
                    Bukkit.getLogger().warning("Economy integration was enabled in config, but the Vault plugin could not be found!");
                    Bukkit.getLogger().warning("Economy features have been disabled. Please ensure both Vault and an economy plugin are installed and restart the server.");
                    this.doEconomy = false;
                }
            }

            this.freeHomeSlots = configFile.getInt("economy_integration.free_home_slots");
            this.setHomeCost = configFile.getDouble("economy_integration.costs.additional_home_slot");
            this.publicHomeCost = configFile.getDouble("economy_integration.costs.make_home_public");
            this.rtpCost = configFile.getDouble("economy_integration.costs.random_teleport");

            // Retrieve sounds used in plugin; if invalid, use defaults.
            try {
                this.teleportationCompleteSound = Sound.valueOf(configFile.getString("general.sounds.teleportation_complete"));
                this.teleportWarmupSound = Sound.valueOf(configFile.getString("general.sounds.teleportation_warmup"));
                this.teleportCancelledSound = Sound.valueOf(configFile.getString("general.sounds.teleportation_cancelled"));
            } catch (IllegalArgumentException exception) {
                Bukkit.getLogger().severe("Invalid sound specified in config.yml; using default sounds instead.");
                this.teleportationCompleteSound = Sound.ENTITY_ENDERMAN_TELEPORT;
                this.teleportWarmupSound = Sound.BLOCK_NOTE_BLOCK_BANJO;
                this.teleportCancelledSound = Sound.ENTITY_ITEM_BREAK;
            }


            this.checkVanishedPlayers = configFile.getBoolean("handle_vanished_players");

            this.doSpawnCommand = configFile.getBoolean("spawn_command.enabled");
            this.doWarpCommand = configFile.getBoolean("enable_warp_command");

            this.doRtpCommand = configFile.getBoolean("random_teleport_command.enabled");
            this.rtpRange = configFile.getInt("random_teleport_command.range");
            this.rtpCooldown = configFile.getInt("random_teleport_command.cooldown");

            this.privateHomesPerPage = configFile.getInt("general.lists.private_homes_per_page");
            this.publicHomesPerPage = configFile.getInt("general.lists.public_homes_per_page");
            this.warpsPerPage = configFile.getInt("general.lists.warps_per_page");

            this.maximumHomes = configFile.getInt("general.max_sethomes");
            this.teleportRequestExpiryTime = configFile.getInt("general.teleport_request_expiry_time");
            this.teleportWarmupTime = configFile.getInt("general.teleport_warmup_time");

        } catch (Exception e) {
            HuskHomes.disablePlugin("An error occurred loading the HuskHomes config (" + e.getCause() + ")");
            e.printStackTrace();
        }
    }

    public void reloadSettings(FileConfiguration configFile) {
        setSettings(configFile);
    }

    public Settings(FileConfiguration configFile) {
        setSettings(configFile);
    }

    public boolean showPublicHomesOnDynmap() {
        return dynmapPublicHomes;
    }

    public boolean showWarpsOnDynmap() {
        return dynmapWarps;
    }

    public String getDynmapPublicHomeMarkerIconID() {
        return dynmapPublicHomeMarkerIconID;
    }

    public String getDynmapWarpMarkerIconID() {
        return dynmapWarpMarkerIconID;
    }

    public String getDynmapPublicHomeMarkerSet() {
        return dynmapPublicHomeMarkerSet;
    }

    public String getDynmapWarpMarkerSet() {
        return dynmapWarpMarkerSet;
    }

    public boolean doDynmap() {
        return doDynmap;
    }

    public int getTeleportWarmupTime() {
        return teleportWarmupTime;
    }

    public String getStorageType() {
        return storageType;
    }

    public boolean isCheckVanishedPlayers() {
        return checkVanishedPlayers;
    }

    public String getLanguage() {
        return language;
    }

    public String getServerID() {
        return server;
    }

    public int getServerClusterID() { return clusterID; }

    public boolean doBungee() {
        return doBungee;
    }

    public int getMaximumHomes() {
        return maximumHomes;
    }

    public int getRtpCooldown() {
        return rtpCooldown;
    }

    public int getTeleportRequestExpiryTime() {
        return teleportRequestExpiryTime;
    }

    public int getRtpRange() {
        return rtpRange;
    }

    public boolean doRtpCommand() {
        return doRtpCommand;
    }

    public String getPlayerDataTable() {
        return playerDataTable;
    }

    public String getLocationsDataTable() {
        return locationsDataTable;
    }

    public String getHomesDataTable() {
        return homesDataTable;
    }

    public String getWarpsDataTable() {
        return warpsDataTable;
    }

    public int getMySQLport() {
        return mySQLport;
    }

    public String getMySQLhost() {
        return mySQLhost;
    }

    public String getMySQLdatabase() {
        return mySQLdatabase;
    }

    public String getMySQLusername() {
        return mySQLusername;
    }

    public String getMySQLpassword() {
        return mySQLpassword;
    }

    public boolean doSpawnCommand() {
        return doSpawnCommand;
    }

    public boolean doWarpCommand() {
        return doWarpCommand;
    }

    public boolean doEconomy() {
        return doEconomy;
    }

    public int getFreeHomeSlots() {
        return freeHomeSlots;
    }

    public double getSetHomeCost() {
        return setHomeCost;
    }

    public double getPublicHomeCost() {
        return publicHomeCost;
    }

    public double getRtpCost() {
        return rtpCost;
    }

    public int getPrivateHomesPerPage() {
        return privateHomesPerPage;
    }

    public int getPublicHomesPerPage() {
        return publicHomesPerPage;
    }

    public int getWarpsPerPage() {
        return warpsPerPage;
    }

    public Sound getTeleportationCompleteSound() {
        return teleportationCompleteSound;
    }

    public Sound getTeleportWarmupSound() {
        return teleportWarmupSound;
    }

    public Sound getTeleportCancelledSound() {
        return teleportCancelledSound;
    }

    public boolean doUpdateChecks() {
        return updateReminders;
    }
}
