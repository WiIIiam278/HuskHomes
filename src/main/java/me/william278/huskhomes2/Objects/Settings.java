package me.william278.huskhomes2.Objects;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

    String language;
    String server;

    String storageType;

    int maximumHomes;
    int teleportRequestExpiryTime;
    int teleportWarmupTime;
    int rtpRange;

    boolean doBungee;
    boolean doRtpCommand;
    boolean doSpawnCommand;

    private void setSettings(FileConfiguration configFile) {
        this.language = configFile.getString("language");
        this.doBungee = configFile.getBoolean("bungee_options.enable_bungee_mode");
        this.server = configFile.getString("bungee_options.server_id");

        this.storageType = configFile.getString("data_storage_options.storage_type");

        this.doSpawnCommand = configFile.getBoolean("spawn_command.enabled");

        this.doRtpCommand = configFile.getBoolean("rtp_command.enabled");
        this.rtpRange = configFile.getInt("rtp_command.range");

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

    public int getMaximumHomes() { return maximumHomes; }

    public int getTeleportRequestExpiryTime() { return teleportRequestExpiryTime; }

    public int getRtpRange() {
        return rtpRange;
    }

    public boolean doRtpCommand() {
        return doRtpCommand;
    }

}
