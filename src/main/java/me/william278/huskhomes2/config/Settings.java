package me.william278.huskhomes2.config;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Settings {

    private final Plugin plugin;

    // Message language setting
    private String language;

    // Automatically send update HuskHomes reminders
    private boolean updateReminders;

    // Bungee settings
    private boolean doBungee;
    private int clusterID;
    private String server;

    // Data storage settings
    private String storageType;
    private String playerDataTable;
    private String locationsDataTable;
    private String homesDataTable;
    private String warpsDataTable;

    // MySQL connection settings
    private int mySQLport;
    private String mySQLhost;
    private String mySQLdatabase;
    private String mySQLusername;
    private String mySQLpassword;

    // Dynmap integration settings
    private boolean doDynmap;
    private boolean dynmapPublicHomes;
    private boolean dynmapWarps;
    private String dynmapPublicHomeMarkerIconID;
    private String dynmapWarpMarkerIconID;
    private String dynmapPublicHomeMarkerSet;
    private String dynmapWarpMarkerSet;

    // Economy (Vault) integration settings
    private boolean doEconomy;
    private int freeHomeSlots;
    private double setHomeCost;
    private double publicHomeCost;
    private double rtpCost;
    private double backCost;

    // Vanished player checks
    private boolean checkVanishedPlayers;

    // Time and maximum home settings
    private int maximumHomes;
    private int teleportRequestExpiryTime;
    private int teleportWarmupTime;

    // Sounds
    private Sound teleportationCompleteSound;
    private Sound teleportWarmupSound;
    private Sound teleportCancelledSound;

    // Number of items per page on lists
    private int privateHomesPerPage;
    private int publicHomesPerPage;
    private int warpsPerPage;

    // RTP command settings
    private boolean doRtpCommand;
    private int rtpRange;
    private int rtpCooldown;

    // Command toggle settings
    private boolean doSpawnCommand;
    private boolean doWarpCommand;

    public Settings(Plugin plugin) {
        this.plugin = plugin;
    }

    // (Re-)Load the config file
    public void reload() {
        plugin.reloadConfig();
        reloadFromFile(plugin.getConfig());
    }

    public void reloadFromFile(FileConfiguration config) {
        try {
            this.language = config.getString("language");

            this.updateReminders = config.getBoolean("check_for_updates");

            this.doBungee = config.getBoolean("bungee_options.enable_bungee_mode");
            this.server = config.getString("bungee_options.server_id");
            this.clusterID = config.getInt("bungee_options.cluster_id");

            this.storageType = config.getString("data_storage_options.storage_type");
            this.playerDataTable = config.getString("data_storage_options.table_names.player_data");
            this.locationsDataTable = config.getString("data_storage_options.table_names.locations_data");
            this.homesDataTable = config.getString("data_storage_options.table_names.homes_data");
            this.warpsDataTable = config.getString("data_storage_options.table_names.warps_data");

            // Resolve conflict between storage type and bungee mode
            if (storageType.equalsIgnoreCase("sqlite") && doBungee) {
                Bukkit.getLogger().warning("Bungee mode was set in config to be enabled but storage type was set to SQLite!");
                Bukkit.getLogger().warning("A mySQL Database is required to utilise Bungee mode, so bungee mode has been disabled.");
                Bukkit.getLogger().warning("To use Bungee mode and cross-server teleportation, please update your data storage settings to use \"mysql\" and update the connection credentials accordingly.");
                this.doBungee = false;
            }

            this.mySQLhost = config.getString("data_storage_options.mysql_credentials.host");
            this.mySQLdatabase = config.getString("data_storage_options.mysql_credentials.database");
            this.mySQLusername = config.getString("data_storage_options.mysql_credentials.username");
            this.mySQLpassword = config.getString("data_storage_options.mysql_credentials.password");
            this.mySQLport = config.getInt("data_storage_options.mysql_credentials.port");

            this.doDynmap = config.getBoolean("dynmap_integration.enabled");

            if (this.doDynmap) {
                PluginManager pluginManager = HuskHomes.getInstance().getServer().getPluginManager();
                Plugin dynmap = pluginManager.getPlugin("dynmap");
                if (dynmap == null) {
                    Bukkit.getLogger().warning("Dynmap integration was enabled in config, but the Dynmap plugin could not be found!");
                    Bukkit.getLogger().warning("The Dynmap setting has been disabled. Please ensure Dynmap is installed and restart the server.");
                    this.doDynmap = false;
                }
            }

            this.dynmapPublicHomes = config.getBoolean("dynmap_integration.markers.public_homes.show");
            this.dynmapWarps = config.getBoolean("dynmap_integration.markers.warps.show");
            this.dynmapPublicHomeMarkerIconID = config.getString("dynmap_integration.markers.public_homes.icon_id");
            this.dynmapWarpMarkerIconID = config.getString("dynmap_integration.markers.warps.icon_id");
            this.dynmapPublicHomeMarkerSet = config.getString("dynmap_integration.markers.public_homes.set_name");
            this.dynmapWarpMarkerSet = config.getString("dynmap_integration.markers.warps.set_name");

            this.doEconomy = config.getBoolean("economy_integration.enabled");

            if (this.doEconomy) {
                PluginManager pluginManager = HuskHomes.getInstance().getServer().getPluginManager();
                Plugin vault = pluginManager.getPlugin("Vault");
                if (vault == null) {
                    Bukkit.getLogger().warning("Economy integration was enabled in config, but the Vault plugin could not be found!");
                    Bukkit.getLogger().warning("Economy features have been disabled. Please ensure both Vault and an economy plugin are installed and restart the server.");
                    this.doEconomy = false;
                }
            }

            this.freeHomeSlots = config.getInt("economy_integration.free_home_slots");
            this.setHomeCost = config.getDouble("economy_integration.costs.additional_home_slot");
            this.publicHomeCost = config.getDouble("economy_integration.costs.make_home_public");
            this.rtpCost = config.getDouble("economy_integration.costs.random_teleport");
            this.backCost = config.getDouble("economy_integration.costs.back");

            // Retrieve sounds used in plugin; if invalid, use defaults.
            try {
                this.teleportationCompleteSound = Sound.valueOf(config.getString("general.sounds.teleportation_complete"));
                this.teleportWarmupSound = Sound.valueOf(config.getString("general.sounds.teleportation_warmup"));
                this.teleportCancelledSound = Sound.valueOf(config.getString("general.sounds.teleportation_cancelled"));
            } catch (IllegalArgumentException exception) {
                Bukkit.getLogger().severe("Invalid sound specified in config.yml; using default sounds instead.");
                this.teleportationCompleteSound = Sound.ENTITY_ENDERMAN_TELEPORT;
                this.teleportWarmupSound = Sound.BLOCK_NOTE_BLOCK_BANJO;
                this.teleportCancelledSound = Sound.ENTITY_ITEM_BREAK;
            }


            this.checkVanishedPlayers = config.getBoolean("handle_vanished_players");

            this.doSpawnCommand = config.getBoolean("spawn_command.enabled");
            this.doWarpCommand = config.getBoolean("enable_warp_command");

            this.doRtpCommand = config.getBoolean("random_teleport_command.enabled");
            this.rtpRange = config.getInt("random_teleport_command.range");
            this.rtpCooldown = config.getInt("random_teleport_command.cooldown");

            this.privateHomesPerPage = config.getInt("general.lists.private_homes_per_page");
            this.publicHomesPerPage = config.getInt("general.lists.public_homes_per_page");
            this.warpsPerPage = config.getInt("general.lists.warps_per_page");

            this.maximumHomes = config.getInt("general.max_sethomes");
            this.teleportRequestExpiryTime = config.getInt("general.teleport_request_expiry_time");
            this.teleportWarmupTime = config.getInt("general.teleport_warmup_time");

        } catch (Exception e) {
            HuskHomes.disablePlugin("An error occurred loading the HuskHomes config (" + e.getCause() + ")");
            e.printStackTrace();
        }
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

    public double getBackCost() {
        return backCost;
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
