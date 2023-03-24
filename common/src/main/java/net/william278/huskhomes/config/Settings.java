package net.william278.huskhomes.config;

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.network.Broker;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Plugin settings, read from config.yml
 */
@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃       HuskHomes Config       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://william278.net/project/huskhomes
        ┗╸ Documentation: https://william278.net/docs/huskhomes""",

        versionField = "config_version", versionNumber = 11)
public class Settings {

    // Top-level settings
    @YamlKey("language")
    private String language = "en-gb";

    @YamlKey("check_for_updates")
    private boolean checkForUpdates = true;

    // Database settings
    @YamlComment("Database connection settings")
    @YamlKey("database.type")
    private Database.Type databaseType = Database.Type.SQLITE;

    @YamlKey("database.mysql.credentials.host")
    private String mySqlHost = "localhost";

    @YamlKey("database.mysql.credentials.port")
    private int mySqlPort = 3306;

    @YamlKey("database.mysql.credentials.database")
    private String mySqlDatabase = "HuskHomes";

    @YamlKey("database.mysql.credentials.username")
    private String mySqlUsername = "root";

    @YamlKey("database.mysql.credentials.password")
    private String mySqlPassword = "pa55w0rd";

    @YamlKey("database.mysql.credentials.parameters")
    private String mySqlConnectionParameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    @YamlComment("MySQL connection pool properties")
    @YamlKey("database.mysql.connection_pool.size")
    private int mySqlConnectionPoolSize = 12;

    @YamlKey("database.mysql.connection_pool.idle")
    private int mySqlConnectionPoolIdle = 12;

    @YamlKey("database.mysql.connection_pool.lifetime")
    private long mySqlConnectionPoolLifetime = 1800000;

    @YamlKey("database.mysql.connection_pool.keepalive")
    private long mySqlConnectionPoolKeepAlive = 30000;

    @YamlKey("database.mysql.connection_pool.timeout")
    private long mySqlConnectionPoolTimeout = 20000;

    @YamlKey("database.table_names")
    private Map<String, String> tableNames = Map.of(
            Database.Table.PLAYER_DATA.name().toLowerCase(), Database.Table.PLAYER_DATA.getDefaultName(),
            Database.Table.POSITION_DATA.name().toLowerCase(), Database.Table.POSITION_DATA.getDefaultName(),
            Database.Table.SAVED_POSITION_DATA.name().toLowerCase(), Database.Table.SAVED_POSITION_DATA.getDefaultName(),
            Database.Table.HOME_DATA.name().toLowerCase(), Database.Table.HOME_DATA.getDefaultName(),
            Database.Table.WARP_DATA.name().toLowerCase(), Database.Table.WARP_DATA.getDefaultName(),
            Database.Table.TELEPORT_DATA.name().toLowerCase(), Database.Table.TELEPORT_DATA.getDefaultName()
    );

    @NotNull
    public String getTableName(@NotNull Database.Table table) {
        return Optional.ofNullable(getTableNames().get(table.name().toLowerCase())).orElse(table.getDefaultName());
    }


    // General settings
    @YamlComment("General plugin settings")
    @YamlKey("general.max_homes")
    private int maxHomes = 10;

    @YamlKey("general.max_public_homes")
    private int maxPublicHomes = 10;

    @YamlKey("general.stack_permission_limits")
    private boolean stackPermissionLimits = false;

    @YamlKey("general.permission_restrict_warps")
    private boolean permissionRestrictWarps = false;

    @YamlKey("general.overwrite_existing_homes_warps")
    private boolean overwriteExistingHomesWarps = true;

    @YamlKey("general.teleport_warmup_time")
    private int teleportWarmupTime = 5;

    @YamlKey("general.teleport_warmup_display")
    private Locales.DisplaySlot teleportWarmupDisplay = Locales.DisplaySlot.ACTION_BAR;

    @YamlKey("general.teleport_request_expiry_time")
    private int teleportRequestExpiryTime = 60;
    @YamlKey("general.strict_tpa_here_requests")
    private boolean strictTpaHereRequests = true;

    @YamlKey("general.allow_unicode_names")
    private boolean allowUnicodeNames = false;

    @YamlKey("general.allow_unicode_descriptions")
    private boolean allowUnicodeDescriptions = true;

    @YamlKey("general.back_command_return_by_death")
    private boolean backCommandReturnByDeath = true;

    @YamlKey("general.back_command_save_teleport_event")
    private boolean backCommandSaveOnTeleportEvent = false;

    @YamlKey("general.list_items_per_page")
    private int listItemsPerPage = 12;

    @YamlKey("general.asynchronous_teleports")
    private boolean asynchronousTeleports = true;

    @YamlKey("general.play_sound_effects")
    private boolean playSoundEffects = true;

    @YamlKey("general.sound_effects")
    private Map<String, String> soundEffects = Map.of(
            SoundEffectAction.TELEPORTATION_COMPLETE.name().toLowerCase(), SoundEffectAction.TELEPORTATION_COMPLETE.defaultSoundEffect,
            SoundEffectAction.TELEPORTATION_WARMUP.name().toLowerCase(), SoundEffectAction.TELEPORTATION_WARMUP.defaultSoundEffect,
            SoundEffectAction.TELEPORTATION_CANCELLED.name().toLowerCase(), SoundEffectAction.TELEPORTATION_CANCELLED.defaultSoundEffect
    );

    @YamlKey("general.brigadier_tab_completion")
    private boolean brigadierTabCompletion = true;


    // Cross-server settings
    @YamlComment("Enable teleporting across proxied servers. Requires MySQL")
    @YamlKey("cross_server.enabled")
    private boolean crossServer = false;

    @YamlKey("cross_server.messenger_type")
    private Broker.Type messageBrokerType = Broker.Type.PLUGIN_MESSAGE;

    @YamlKey("cross_server.cluster_id")
    private String clusterId = "";

    @YamlKey("cross_server.global_spawn.enabled")
    private boolean globalSpawn = false;

    @YamlKey("cross_server.global_spawn.warp_name")
    private String globalSpawnName = "Spawn";

    @YamlKey("cross_server.global_respawning")
    private boolean globalRespawning = false;

    @YamlKey("cross_server.redis_credentials.host")
    private String redisHost = "localhost";

    @YamlKey("cross_server.redis_credentials.port")
    private int redisPort = 6379;

    @YamlKey("cross_server.redis_credentials.password")
    private String redisPassword = "";

    @YamlKey("cross_server.redis_credentials.use_ssl")
    private boolean redisUseSsl = false;


    // Rtp command settings
    @YamlComment("Random teleport (/rtp) command settings")
    @YamlKey("rtp.cooldown_length")
    private int rtpCooldownLength = 10;

    @YamlKey("rtp.radius")
    private int rtpRadius = 5000;

    @YamlKey("rtp.spawn_radius")
    private int rtpSpawnRadius = 500;

    @YamlKey("rtp.distribution_mean")
    private float rtpDistributionMean = 0.75f;

    @YamlKey("rtp.distribution_deviation")
    private float rtpDistributionStandardDeviation = 2f;

    @YamlKey("rtp.restricted_worlds")
    private List<String> rtpRestrictedWorlds = List.of("world_nether", "world_the_end");


    // Economy settings
    @YamlComment("Charge for certain actions (requires Vault)")
    @YamlKey("economy.enabled")
    private boolean economy = false;

    @YamlComment("Use this currency for payments (works only with RedisEconomy), defaults to Vault currency")
    @YamlKey("economy.redis_economy_name")
    private String redisEconomyName = "vault";

    @YamlKey("economy.free_home_slots")
    private int freeHomeSlots = 5;

    @YamlKey("economy.costs")
    private Map<String, Double> economyCosts = Map.of(
            EconomyHook.Action.ADDITIONAL_HOME_SLOT.name().toLowerCase(), EconomyHook.Action.ADDITIONAL_HOME_SLOT.getDefaultCost(),
            EconomyHook.Action.MAKE_HOME_PUBLIC.name().toLowerCase(), EconomyHook.Action.MAKE_HOME_PUBLIC.getDefaultCost(),
            EconomyHook.Action.RANDOM_TELEPORT.name().toLowerCase(), EconomyHook.Action.RANDOM_TELEPORT.getDefaultCost(),
            EconomyHook.Action.BACK_COMMAND.name().toLowerCase(), EconomyHook.Action.BACK_COMMAND.getDefaultCost()
    );

    // Mapping plugins
    @YamlComment("Display public homes/warps on your web map (supports Dynmap and BlueMap)")
    @YamlKey("map_hook.enabled")
    private boolean doMapHook = true;

    @YamlKey("map_hook.show_public_homes")
    private boolean publicHomesOnMap = true;

    @YamlKey("map_hook.show_warps")
    private boolean warpsOnMap = true;


    // Disabled commands
    @YamlComment("Disabled commands (e.g. ['/home', '/warp'] to disable /home and /warp)")
    @YamlKey("disabled_commands")
    private List<String> disabledCommands = Collections.emptyList();

    @SuppressWarnings("unused")
    private Settings() {
    }

    public String getLanguage() {
        return language;
    }

    public boolean doCheckForUpdates() {
        return checkForUpdates;
    }

    public Database.Type getDatabaseType() {
        return databaseType;
    }

    public String getMySqlHost() {
        return mySqlHost;
    }

    public int getMySqlPort() {
        return mySqlPort;
    }

    public String getMySqlDatabase() {
        return mySqlDatabase;
    }

    public String getMySqlUsername() {
        return mySqlUsername;
    }

    public String getMySqlPassword() {
        return mySqlPassword;
    }

    public String getMySqlConnectionParameters() {
        return mySqlConnectionParameters;
    }

    public int getMySqlConnectionPoolSize() {
        return mySqlConnectionPoolSize;
    }

    public int getMySqlConnectionPoolIdle() {
        return mySqlConnectionPoolIdle;
    }

    public long getMySqlConnectionPoolLifetime() {
        return mySqlConnectionPoolLifetime;
    }

    public long getMySqlConnectionPoolKeepAlive() {
        return mySqlConnectionPoolKeepAlive;
    }

    public long getMySqlConnectionPoolTimeout() {
        return mySqlConnectionPoolTimeout;
    }

    public Map<String, String> getTableNames() {
        return tableNames;
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public int getMaxPublicHomes() {
        return maxPublicHomes;
    }

    public boolean doStackPermissionLimits() {
        return stackPermissionLimits;
    }

    public boolean isPermissionRestrictWarps() {
        return permissionRestrictWarps;
    }

    public boolean doOverwriteExistingHomesWarps() {
        return overwriteExistingHomesWarps;
    }

    public int getTeleportWarmupTime() {
        return teleportWarmupTime;
    }

    public Locales.DisplaySlot getTeleportWarmupDisplay() {
        return teleportWarmupDisplay;
    }

    public int getTeleportRequestExpiryTime() {
        return teleportRequestExpiryTime;
    }

    public boolean doStrictTpaHereRequests() {
        return strictTpaHereRequests;
    }

    public boolean isAllowUnicodeNames() {
        return allowUnicodeNames;
    }

    public boolean isAllowUnicodeDescriptions() {
        return allowUnicodeDescriptions;
    }

    public boolean isBackCommandReturnByDeath() {
        return backCommandReturnByDeath;
    }

    public boolean isBackCommandSaveOnTeleportEvent() {
        return backCommandSaveOnTeleportEvent;
    }

    public int getListItemsPerPage() {
        return listItemsPerPage;
    }

    public boolean doAsynchronousTeleports() {
        return asynchronousTeleports;
    }

    public boolean isPlaySoundEffects() {
        return playSoundEffects;
    }

    public Optional<String> getSoundEffect(@NotNull SoundEffectAction action) {
        if (!isPlaySoundEffects()) {
            return Optional.empty();
        }
        return Optional.ofNullable(soundEffects.get(action.name().toLowerCase()));
    }

    public boolean isCrossServer() {
        return crossServer;
    }

    public Broker.Type getBrokerType() {
        return messageBrokerType;
    }

    public String getClusterId() {
        return clusterId;
    }

    public boolean isGlobalSpawn() {
        return globalSpawn;
    }

    public String getGlobalSpawnName() {
        return globalSpawnName;
    }

    public boolean isGlobalRespawning() {
        return globalRespawning;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public boolean useRedisSsl() {
        return redisUseSsl;
    }

    public int getRtpCooldownLength() {
        return rtpCooldownLength;
    }

    public int getRtpRadius() {
        return rtpRadius;
    }

    public int getRtpSpawnRadius() {
        return rtpSpawnRadius;
    }

    public float getRtpDistributionMean() {
        return rtpDistributionMean;
    }

    public float getRtpDistributionStandardDeviation() {
        return rtpDistributionStandardDeviation;
    }

    public List<String> getRtpRestrictedWorlds() {
        return rtpRestrictedWorlds;
    }

    public boolean doEconomy() {
        return economy;
    }

    public String getRedisEconomyName() {
        return redisEconomyName;
    }

    public int getFreeHomeSlots() {
        return freeHomeSlots;
    }

    public Optional<Double> getEconomyCost(@NotNull EconomyHook.Action action) {
        if (!doEconomy()) {
            return Optional.empty();
        }
        final Double cost = economyCosts.get(action.name().toLowerCase());
        if (cost != null && cost > 0d) {
            return Optional.of(cost);
        }
        return Optional.empty();
    }

    public boolean doMapHook() {
        return doMapHook;
    }

    public boolean doPublicHomesOnMap() {
        return publicHomesOnMap;
    }

    public boolean doWarpsOnMap() {
        return warpsOnMap;
    }

    public boolean isCommandDisabled(Command type) {
        return disabledCommands.stream().anyMatch(disabled -> {
            final String command = (disabled.startsWith("/") ? disabled.substring(1) : disabled);
            return command.equalsIgnoreCase(type.getName())
                    || type.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(command));
        });
    }

    public boolean doBrigadierTabCompletion() {
        return brigadierTabCompletion;
    }

    /**
     * Represents the sound effect to play when an action happens
     */
    public enum SoundEffectAction {
        TELEPORTATION_COMPLETE("entity.enderman.teleport"),
        TELEPORTATION_WARMUP("block.note_block.banjo"),
        TELEPORTATION_CANCELLED("entity.item.break");
        private final String defaultSoundEffect;

        SoundEffectAction(@NotNull String defaultSoundEffect) {
            this.defaultSoundEffect = defaultSoundEffect;
        }
    }

}
