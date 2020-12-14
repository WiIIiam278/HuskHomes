package me.william278.huskhomes2.Objects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

    String language;
    String server;

    String storageType;
    String playerDataTable;
    String locationsDataTable;
    String homesDataTable;
    String warpsDataTable;

    int mySQLport;
    String mySQLhost;
    String mySQLdatabase;
    String mySQLusername;
    String mySQLpassword;

    int maximumHomes;
    int teleportRequestExpiryTime;
    int teleportWarmupTime;

    int rtpRange;
    int rtpCooldown;

    boolean doBungee;
    boolean doRtpCommand;
    boolean doSpawnCommand;
    boolean doWarpCommand;

    private void setSettings(FileConfiguration configFile) {
        this.language = configFile.getString("language");
        this.doBungee = configFile.getBoolean("bungee_options.enable_bungee_mode");
        this.server = configFile.getString("bungee_options.server_id");

        this.storageType = configFile.getString("data_storage_options.storage_type");

        // Resolve conflict between storage type and bungee mode
        if (storageType.equalsIgnoreCase("sqlite") && doBungee) {
            Bukkit.getLogger().warning("Bungee mode was set in config to be enabled but storage type was set to SQLite!");
            Bukkit.getLogger().warning("A mySQL Database is required to utilise Bungee mode, so bungee mode has been disabled.");
            Bukkit.getLogger().warning("To use Bungee mode and cross-server teleportation, please update your data storage settings to use \"mysql\" and update the connection details accordingly.");
            this.doBungee = false;
        }

        this.mySQLhost = configFile.getString("data_storage_options.mysql_database.host");
        this.mySQLdatabase = configFile.getString("data_storage_options.mysql_database.database");
        this.mySQLusername = configFile.getString("data_storage_options.mysql_database.username");
        this.mySQLpassword = configFile.getString("data_storage_options.mysql_database.password");
        this.mySQLport = configFile.getInt("data_storage_options.mysql_database.port");

        this.playerDataTable = configFile.getString("data_storage_options.table_names.player_data");
        this.locationsDataTable = configFile.getString("data_storage_options.table_names.locations_data");
        this.homesDataTable = configFile.getString("data_storage_options.table_names.homes_data");
        this.warpsDataTable = configFile.getString("data_storage_options.table_names.warps_data");

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

}
