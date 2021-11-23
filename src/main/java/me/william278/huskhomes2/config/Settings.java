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

    // Automatically check for updates on startup
    private boolean doStartupUpdateChecks;

    // Bungee settings
    private boolean doBungee;
    private int clusterId;
    private String server;
    private boolean doCrossServerTabCompletion;
    private int crossServerTabUpdateDelay;
    private String messengerType;

    // Data storage settings
    private String storageType;
    private String playerDataTable;
    private String locationsDataTable;
    private String homesDataTable;
    private String warpsDataTable;

    // Connection settings for MySQL
    private int mySQLPort;
    private String mySQLHost;
    private String mySQLDatabase;
    private String mySQLUsername;
    private String mySQLPassword;
    private String mySQLParams;

    // Hikari connection pool options
    private int hikariMaximumPoolSize;
    private int hikariMinimumIdle;
    private long hikariMaximumLifetime;
    private long hikariKeepAliveTime;
    private long hikariConnectionTimeOut;

    // Redis connection settings
    private int redisPort;
    private String redisHost;
    private String redisPassword;

    // Map plugin integration settings
    private boolean doMapIntegration;
    private String mapPlugin;
    private boolean mapPublicHomes;
    private boolean mapWarps;
    private String mapPublicHomeMarkerSet;
    private String mapWarpMarkerSet;

    // Economy (Vault) integration settings
    private boolean doEconomy;
    private int freeHomeSlots;
    private double setHomeCost;
    private double publicHomeCost;
    private double rtpCost;
    private double backCost;

    // Allow UTF-8 (Unicode) characters in descriptions?
    private boolean allowUnicodeInDescriptions;

    // Vanished player checks
    private boolean checkVanishedPlayers;

    // Time and maximum home settings
    private int maximumHomes;
    private int teleportRequestExpiryTime;
    private int teleportWarmupTime;
    private DisplayStyle teleportWarmupDisplayStyle;

    // Sounds
    private Sound teleportationCompleteSound;
    private Sound teleportWarmupSound;
    private Sound teleportCancelledSound;

    // Number of items per page on lists
    private int privateHomesPerPage;
    private int publicHomesPerPage;
    private int warpsPerPage;

    // Help menu options
    private boolean hideCommandsFromHelpMenuWithoutPermission;
    private boolean hideHuskHomesCommandFromHelpMenu;

    // Whether to autocomplete player names for tpa (e.g /tpa William will assume you mean /tpa William278)
    private boolean autoCompletePlayerNames;

    // Whether to stack limit permissions (add them) if a user has multiple. By default, the highest one will be returned
    private boolean homeLimitPermissionStacking;

    // RTP command settings
    private boolean doRtpCommand;
    private int rtpRange;
    private int rtpCoolDown;

    // Spawn command settings
    private boolean doSpawnCommand;
    private boolean doCrossServerSpawn;
    private String crossServerSpawnWarpName;
    private boolean forceSpawnOnLogin;

    // Warp command settings
    private boolean doWarpCommand;
    private boolean doPermissionRestrictedWarps;
    private boolean hideRestrictedWarps;
    private String warpRestrictionPermissionFormat;

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
            this.language = config.getString("language", "en-gb");

            this.doStartupUpdateChecks = config.getBoolean("check_for_updates", true);

            this.doBungee = config.getBoolean("bungee_options.enable_bungee_mode", false);
            this.server = config.getString("bungee_options.server_id", "server");
            this.clusterId = config.getInt("bungee_options.cluster_id", 0);
            this.doCrossServerTabCompletion = config.getBoolean("bungee_options.tab_complete_cross_server.enabled", true);
            this.crossServerTabUpdateDelay = config.getInt("bungee_options.tab_complete_cross_server.delay", 60);
            this.messengerType = config.getString("bungee_options.messenger_type", "pluginmessage");

            this.storageType = config.getString("data_storage_options.storage_type", "SQLite");
            this.playerDataTable = config.getString("data_storage_options.table_names.player_data", "huskhomes_player_data");
            this.locationsDataTable = config.getString("data_storage_options.table_names.locations_data", "huskhomes_location_data");
            this.homesDataTable = config.getString("data_storage_options.table_names.homes_data", "huskhomes_home_data");
            this.warpsDataTable = config.getString("data_storage_options.table_names.warps_data", "huskhomes_warp_data");

            // Resolve conflict between storage type and bungee mode
            if (storageType.equalsIgnoreCase("SQLite") && doBungee) {
                Bukkit.getLogger().warning("Bungee mode was set in config to be enabled but storage type was set to SQLite!");
                Bukkit.getLogger().warning("A mySQL Database is required to utilise Bungee mode, so bungee mode has been disabled.");
                Bukkit.getLogger().warning("To use Bungee mode and cross-server teleportation, please update your data storage settings to use \"mysql\" and update the connection credentials accordingly.");
                this.doBungee = false;
            }

            this.mySQLHost = config.getString("data_storage_options.mysql_credentials.host", "localhost");
            this.mySQLDatabase = config.getString("data_storage_options.mysql_credentials.database", "HuskHomes");
            this.mySQLUsername = config.getString("data_storage_options.mysql_credentials.username", "root");
            this.mySQLPassword = config.getString("data_storage_options.mysql_credentials.password", "pa55w0rd");
            this.mySQLPort = config.getInt("data_storage_options.mysql_credentials.port", 3306);
            this.mySQLParams = config.getString("data_storage_options.mysql_credentials.params", "?autoReconnect=true&useSSL=false");

            this.hikariMaximumPoolSize = config.getInt("data_storage_options.connection_pool_options.maximum_pool_size", 10);
            this.hikariMinimumIdle = config.getInt("data_storage_options.connection_pool_options.minimum_idle", 10);
            this.hikariMaximumLifetime = config.getLong("data_storage_options.connection_pool_options.maximum_lifetime", 1800000);
            this.hikariKeepAliveTime = config.getLong("data_storage_options.connection_pool_options.keepalive_time", 0);
            this.hikariConnectionTimeOut = config.getLong("data_storage_options.connection_pool_options.connection_timeout", 5000);

            this.redisHost = config.getString("bungee_options.redis_credentials.host", "localhost");
            this.redisPort = config.getInt("bungee_options.redis_credentials.port", 6379);
            this.redisPassword = config.getString("bungee_options.redis_credentials.password", "");

            this.doMapIntegration = config.getBoolean("map_integration.enabled", false);
            this.mapPlugin = config.getString("map_integration.plugin", "dynmap");

            if (this.doMapIntegration) {
                PluginManager pluginManager = HuskHomes.getInstance().getServer().getPluginManager();
                switch (mapPlugin.toLowerCase()) {
                    case "dynmap", "bluemap", "pl3xmap" -> {
                        Plugin mapSpigotPlugin = pluginManager.getPlugin(mapPlugin.toLowerCase());
                        if (mapSpigotPlugin == null) {
                            Bukkit.getLogger().warning(mapPlugin + " integration was enabled in config, but the " + mapPlugin + " plugin could not be found!");
                            Bukkit.getLogger().warning("The map integration setting has been disabled. Please ensure " + mapPlugin + " is installed and restart the server.");
                            this.doMapIntegration = false;
                            plugin.getConfig().set("map_integration.enabled", false);
                            plugin.saveConfig();
                        }
                    }
                    default -> {
                        Bukkit.getLogger().warning("Map integration was enabled in config, but the map plugin type was invalid.");
                        Bukkit.getLogger().warning("The map integration setting has been disabled. Please ensure you specify a valid map plugin type.");
                        this.doMapIntegration = false;
                        plugin.getConfig().set("map_integration.enabled", false);
                        plugin.saveConfig();
                    }
                }
            }

            this.mapPublicHomes = config.getBoolean("map_integration.markers.public_homes.show", true);
            this.mapWarps = config.getBoolean("map_integration.markers.warps.show", true);
            this.mapPublicHomeMarkerSet = config.getString("map_integration.markers.public_homes.set_name", "Public Homes");
            this.mapWarpMarkerSet = config.getString("map_integration.markers.warps.set_name", "Warps");

            this.doEconomy = config.getBoolean("economy_integration.enabled", false);

            if (this.doEconomy) {
                PluginManager pluginManager = HuskHomes.getInstance().getServer().getPluginManager();
                Plugin vault = pluginManager.getPlugin("Vault");
                if (vault == null) {
                    Bukkit.getLogger().warning("Economy integration was enabled in config, but the Vault plugin could not be found!");
                    Bukkit.getLogger().warning("Economy features have been disabled. Please ensure both Vault and an economy plugin are installed and restart the server.");
                    this.doEconomy = false;
                }
            }

            this.freeHomeSlots = config.getInt("economy_integration.free_home_slots", 5);
            this.setHomeCost = config.getDouble("economy_integration.costs.additional_home_slot", 100D);
            this.publicHomeCost = config.getDouble("economy_integration.costs.make_home_public", 50D);
            this.rtpCost = config.getDouble("economy_integration.costs.random_teleport", 20D);
            this.backCost = config.getDouble("economy_integration.costs.back", 0D);

            // Retrieve sounds used in plugin; if invalid, use defaults.
            try {
                this.teleportationCompleteSound = Sound.valueOf(config.getString("general.sounds.teleportation_complete", "ENTITY_ENDERMAN_TELEPORT"));
                this.teleportWarmupSound = Sound.valueOf(config.getString("general.sounds.teleportation_warmup", "BLOCK_NOTE_BLOCK_BANJO"));
                this.teleportCancelledSound = Sound.valueOf(config.getString("general.sounds.teleportation_cancelled", "ENTITY_ITEM_BREAK"));
            } catch (IllegalArgumentException exception) {
                Bukkit.getLogger().severe("Invalid sound specified in config.yml; using default sounds instead.");
                this.teleportationCompleteSound = Sound.ENTITY_ENDERMAN_TELEPORT;
                this.teleportWarmupSound = Sound.BLOCK_NOTE_BLOCK_BANJO;
                this.teleportCancelledSound = Sound.ENTITY_ITEM_BREAK;
            }

            this.allowUnicodeInDescriptions = config.getBoolean("allow_unicode_descriptions", true);

            this.checkVanishedPlayers = config.getBoolean("handle_vanished_players", true);

            this.doSpawnCommand = config.getBoolean("spawn_command.enabled", true);
            this.doCrossServerSpawn = config.getBoolean("spawn_command.bungee_network_spawn.enabled", false);
            this.crossServerSpawnWarpName = config.getString("spawn_command.bungee_network_spawn.warp_name", "Spawn");
            this.forceSpawnOnLogin = config.getBoolean("spawn_command.force_teleport_on_login", false);

            this.doWarpCommand = config.getBoolean("warp_command.enabled", true);
            this.doPermissionRestrictedWarps = config.getBoolean("warp_command.permission_restrictions.require_permission", false);
            this.warpRestrictionPermissionFormat = config.getString("warp_command.permission_restrictions.format", "huskhomes.warp.");
            this.hideRestrictedWarps = config.getBoolean("warp_command.permission_restrictions.hide_restricted_warps", true);

            this.autoCompletePlayerNames = config.getBoolean("general.auto_complete_player_names", true);

            this.homeLimitPermissionStacking = config.getBoolean("general.home_limit_permission_stacking", false);

            this.doRtpCommand = config.getBoolean("random_teleport_command.enabled", true);
            this.rtpRange = config.getInt("random_teleport_command.range", 5000);
            this.rtpCoolDown = config.getInt("random_teleport_command.cooldown", 30);

            this.privateHomesPerPage = config.getInt("general.lists.private_homes_per_page", 10);
            this.publicHomesPerPage = config.getInt("general.lists.public_homes_per_page", 10);
            this.warpsPerPage = config.getInt("general.lists.warps_per_page", 10);

            this.hideCommandsFromHelpMenuWithoutPermission = config.getBoolean("general.help_menu.hide_commands_without_permission", true);
            this.hideHuskHomesCommandFromHelpMenu = config.getBoolean("general.help_menu.hide_huskhomes_command", false);

            this.maximumHomes = config.getInt("general.max_sethomes", 10);
            this.teleportRequestExpiryTime = config.getInt("general.teleport_request_expiry_time", 60);
            this.teleportWarmupTime = config.getInt("general.teleport_warmup_time", 5);
            this.teleportWarmupDisplayStyle = DisplayStyle.valueOf(config.getString("general.teleport_warmup_display", "ACTION_BAR"));

        } catch (Exception e) {
            HuskHomes.disablePlugin("An error occurred loading the HuskHomes config (" + e.getCause() + ")");
            e.printStackTrace();
        }
    }

    public String getMapPlugin() {
        return mapPlugin;
    }

    public boolean showPublicHomesOnMap() {
        return mapPublicHomes;
    }

    public boolean showWarpsOnMap() {
        return mapWarps;
    }

    public String getMapPublicHomeMarkerSet() {
        return mapPublicHomeMarkerSet;
    }

    public String getMapWarpMarkerSet() {
        return mapWarpMarkerSet;
    }

    public boolean doMapIntegration() {
        return doMapIntegration;
    }

    public int getTeleportWarmupTime() {
        return teleportWarmupTime;
    }

    public String getDatabaseType() {
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

    public int getClusterId() {
        return clusterId;
    }

    public boolean doBungee() {
        return doBungee;
    }

    public int getMaximumHomes() {
        return maximumHomes;
    }

    public int getRtpCoolDown() {
        return rtpCoolDown;
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

    public int getMySQLPort() {
        return mySQLPort;
    }

    public String getMySQLHost() {
        return mySQLHost;
    }

    public String getMySQLDatabase() {
        return mySQLDatabase;
    }

    public String getMySQLUsername() {
        return mySQLUsername;
    }

    public String getMySQLPassword() {
        return mySQLPassword;
    }

    public String getMySQLParams() {
        return mySQLParams;
    }

    public boolean doSpawnCommand() {
        return doSpawnCommand;
    }

    public boolean doCrossServerSpawn() {
        return doCrossServerSpawn;
    }

    public String getSpawnWarpName() {
        return crossServerSpawnWarpName;
    }

    public boolean doForceSpawnOnLogin() {
        return forceSpawnOnLogin;
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

    public boolean doStartupUpdateChecks() {
        return doStartupUpdateChecks;
    }

    public boolean doCrossServerTabCompletion() {
        return doCrossServerTabCompletion;
    }

    public int getCrossServerTabUpdateDelay() {
        return crossServerTabUpdateDelay;
    }

    public boolean doWarpCommand() {
        return doWarpCommand;
    }

    public boolean doPermissionRestrictedWarps() {
        return doPermissionRestrictedWarps;
    }

    public String getWarpRestrictionPermissionFormat() {
        return warpRestrictionPermissionFormat;
    }

    public boolean doHideRestrictedWarps() {
        return hideRestrictedWarps;
    }

    public boolean hideCommandsFromHelpMenuWithoutPermission() {
        return hideCommandsFromHelpMenuWithoutPermission;
    }

    public boolean hideHuskHomesCommandFromHelpMenu() {
        return hideHuskHomesCommandFromHelpMenu;
    }

    public boolean doUnicodeInDescriptions() {
        return allowUnicodeInDescriptions;
    }

    public String getMessengerType() {
        return messengerType;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getHikariMaximumPoolSize() {
        return hikariMaximumPoolSize;
    }

    public int getHikariMinimumIdle() {
        return hikariMinimumIdle;
    }

    public long getHikariMaximumLifetime() {
        return hikariMaximumLifetime;
    }

    public long getHikariKeepAliveTime() {
        return hikariKeepAliveTime;
    }

    public long getHikariConnectionTimeOut() {
        return hikariConnectionTimeOut;
    }

    public boolean doAutoCompletePlayerNames() {
        return autoCompletePlayerNames;
    }

    public boolean doHomeLimitPermissionStacking() {
        return homeLimitPermissionStacking;
    }

    public DisplayStyle getWarmupDisplayStyle() {
        return teleportWarmupDisplayStyle;
    }

    public enum DisplayStyle {
        ACTION_BAR,
        TITLE,
        SUBTITLE,
        CHAT
    }
}
