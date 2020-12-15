package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Settings {

    // Message language setting
    String language;

    // Bungee settings
    boolean doBungee;
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

    // Dynmap Settings
    boolean doDynmap;
    boolean dynmapPublicHomes;
    boolean dynmapWarps;
    String dynmapPublicHomeMarkerIconID;
    String dynmapWarpMarkerIconID;
    String dynmapPublicHomeMarkerSet;
    String dynmapWarpMarkerSet;

    // Time and maximum home settings
    int maximumHomes;
    int teleportRequestExpiryTime;
    int teleportWarmupTime;

    // RTP command settings
    boolean doRtpCommand;
    int rtpRange;
    int rtpCooldown;

    // Command toggle settings
    boolean doSpawnCommand;
    boolean doWarpCommand;

    private void setSettings(FileConfiguration configFile) {
        this.language = configFile.getString("language");

        this.doBungee = configFile.getBoolean("bungee_options.enable_bungee_mode");
        this.server = configFile.getString("bungee_options.server_id");

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
            PluginManager pluginManager = Main.getInstance().getServer().getPluginManager();
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

        this.doSpawnCommand = configFile.getBoolean("spawn_command.enabled");
        this.doWarpCommand = configFile.getBoolean("enable_warp_command");

        this.doRtpCommand = configFile.getBoolean("rtp_command.enabled");
        this.rtpRange = configFile.getInt("rtp_command.range");
        this.rtpCooldown = configFile.getInt("rtp_command.cooldown");

        this.maximumHomes = configFile.getInt("general.max_sethomes");
        this.teleportRequestExpiryTime = configFile.getInt("general.teleport_request_expiry_time");
        this.teleportWarmupTime = configFile.getInt("general.teleport_warmup_time");

    }

    public Settings(FileConfiguration configFile) {
        setSettings(configFile);
    }

    public boolean isDynmapPublicHomes() {
        return dynmapPublicHomes;
    }

    public boolean isDynmapWarps() {
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

    public void reloadSettings(FileConfiguration configFile) {
        setSettings(configFile);
    }

    public String getStorageType() {
        return storageType;
    }

    public String getLanguage() {
        return language;
    }

    public String getServerID() {
        return server;
    }

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

}
