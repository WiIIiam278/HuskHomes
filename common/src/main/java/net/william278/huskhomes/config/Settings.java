package net.william278.huskhomes.config;

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
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
    public String language = "en-gb";

    @YamlKey("check_for_updates")
    public boolean checkForUpdates = true;

    @YamlKey("debug_logging")
    public boolean debugLogging = false;


    // Database settings
    @YamlComment("Database connection settings")
    @YamlKey("database.type")
    public DatabaseType databaseType = DatabaseType.SQLITE;

    @YamlKey("database.mysql.credentials.host")
    public String mySqlHost = "localhost";

    @YamlKey("database.mysql.credentials.port")
    public int mySqlPort = 3306;

    @YamlKey("database.mysql.credentials.database")
    public String mySqlDatabase = "HuskHomes";

    @YamlKey("database.mysql.credentials.username")
    public String mySqlUsername = "root";

    @YamlKey("database.mysql.credentials.password")
    public String mySqlPassword = "pa55w0rd";

    @YamlKey("database.mysql.credentials.parameters")
    public String mySqlConnectionParameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    @YamlComment("MySQL connection pool properties")
    @YamlKey("database.mysql.connection_pool.size")
    public int mySqlConnectionPoolSize = 12;

    @YamlKey("database.mysql.connection_pool.idle")
    public int mySqlConnectionPoolIdle = 12;

    @YamlKey("database.mysql.connection_pool.lifetime")
    public long mySqlConnectionPoolLifetime = 1800000;

    @YamlKey("database.mysql.connection_pool.keepalive")
    public long mySqlConnectionPoolKeepAlive = 30000;

    @YamlKey("database.mysql.connection_pool.timeout")
    public long mySqlConnectionPoolTimeout = 20000;

    @YamlKey("database.table_names")
    public Map<String, String> tableNames = Map.of(
            TableName.PLAYER_DATA.name().toLowerCase(), TableName.PLAYER_DATA.defaultName,
            TableName.POSITION_DATA.name().toLowerCase(), TableName.POSITION_DATA.defaultName,
            TableName.SAVED_POSITION_DATA.name().toLowerCase(), TableName.SAVED_POSITION_DATA.defaultName,
            TableName.HOME_DATA.name().toLowerCase(), TableName.HOME_DATA.defaultName,
            TableName.WARP_DATA.name().toLowerCase(), TableName.WARP_DATA.defaultName,
            TableName.TELEPORT_DATA.name().toLowerCase(), TableName.TELEPORT_DATA.defaultName
    );

    @NotNull
    public String getTableName(@NotNull TableName tableName) {
        return Optional.ofNullable(tableNames.get(tableName.name().toLowerCase())).orElse(tableName.defaultName);
    }


    // General settings
    @YamlComment("General plugin settings")
    @YamlKey("general.max_homes")
    public int maxHomes = 10;

    @YamlKey("general.max_public_homes")
    public int maxPublicHomes = 10;

    @YamlKey("general.stack_permission_limits")
    public boolean stackPermissionLimits = true;

    @YamlKey("general.permission_restrict_warps")
    public boolean permissionRestrictWarps = false;

    @YamlKey("general.teleport_warmup_time")
    public int teleportWarmupTime = 5;

    @YamlKey("general.teleport_warmup_display")
    public MessageDisplayType teleportWarmupDisplay = MessageDisplayType.ACTION_BAR;

    @YamlKey("general.teleport_request_expiry_time")
    public int teleportRequestExpiryTime = 60;
    @YamlKey("general.strict_tpa_here_requests")
    public boolean strictTpaHereRequests = true;

    @YamlKey("general.allow_unicode_names")
    public boolean allowUnicodeNames = false;

    @YamlKey("general.allow_unicode_descriptions")
    public boolean allowUnicodeDescriptions = true;

    @YamlKey("general.back_command_return_by_death")
    public boolean backCommandReturnByDeath = true;

    @YamlKey("general.back_command_save_teleport_event")
    public boolean backCommandSaveOnTeleportEvent = false;

    @YamlKey("general.list_items_per_page")
    public int listItemsPerPage = 12;

    @YamlKey("general.asynchronous_teleports")
    public boolean asynchronousTeleports = true;

    @YamlKey("general.play_sound_effects")
    public boolean playSoundEffects = true;

    @YamlKey("general.sound_effects")
    public Map<String, String> soundEffects = Map.of(
            SoundEffectAction.TELEPORTATION_COMPLETE.name().toLowerCase(), SoundEffectAction.TELEPORTATION_COMPLETE.defaultSoundEffect,
            SoundEffectAction.TELEPORTATION_WARMUP.name().toLowerCase(), SoundEffectAction.TELEPORTATION_WARMUP.defaultSoundEffect,
            SoundEffectAction.TELEPORTATION_CANCELLED.name().toLowerCase(), SoundEffectAction.TELEPORTATION_CANCELLED.defaultSoundEffect
    );

    public Optional<String> getSoundEffect(@NotNull SoundEffectAction action) {
        if (!playSoundEffects) {
            return Optional.empty();
        }
        return Optional.ofNullable(soundEffects.get(action.name().toLowerCase()));
    }


    // Cross-server settings
    @YamlComment("Enable teleporting across proxied servers. Requires MySQL")
    @YamlKey("cross_server.enabled")
    public boolean crossServer = false;

    @YamlKey("cross_server.messenger_type")
    public MessengerType messengerType = MessengerType.PLUGIN_MESSAGE;

    @YamlKey("cross_server.cluster_id")
    public String clusterId = "";

    @YamlKey("cross_server.global_spawn.enabled")
    public boolean globalSpawn = false;

    @YamlKey("cross_server.global_spawn.warp_name")
    public String globalSpawnName = "Spawn";

    @YamlKey("cross_server.global_respawning")
    public boolean globalRespawning = false;

    @YamlKey("cross_server.redis_credentials.host")
    public String redisHost = "localhost";

    @YamlKey("cross_server.redis_credentials.port")
    public int redisPort = 6379;

    @YamlKey("cross_server.redis_credentials.password")
    public String redisPassword = "";

    @YamlKey("cross_server.redis_credentials.use_ssl")
    public boolean redisUseSsl = false;


    // Rtp command settings
    @YamlComment("Random teleport (/rtp) command settings")
    @YamlKey("rtp.cooldown_length")
    public int rtpCooldownLength = 10;

    @YamlKey("rtp.radius")
    public int rtpRadius = 5000;

    @YamlKey("rtp.spawn_radius")
    public int rtpSpawnRadius = 500;

    @YamlKey("rtp.distribution_mean")
    public float rtpDistributionMean = 0.75f;

    @YamlKey("rtp.distribution_deviation")
    public float rtpDistributionStandardDeviation = 2f;

    @YamlKey("rtp.location_cache_size")
    public int rtpLocationCacheSize = 10;

    @YamlKey("rtp.restricted_worlds")
    public List<String> rtpRestrictedWorlds = List.of("world_nether", "world_the_end");


    // Economy settings
    @YamlComment("Charge for certain actions (requires Vault)")
    @YamlKey("economy.enabled")
    public boolean economy = false;

    @YamlKey("economy.free_home_slots")
    public int freeHomeSlots = 5;

    @YamlKey("economy.costs")
    public Map<String, Double> economyCosts = Map.of(
            EconomyAction.ADDITIONAL_HOME_SLOT.name().toLowerCase(), EconomyAction.ADDITIONAL_HOME_SLOT.defaultCost,
            EconomyAction.MAKE_HOME_PUBLIC.name().toLowerCase(), EconomyAction.MAKE_HOME_PUBLIC.defaultCost,
            EconomyAction.RANDOM_TELEPORT.name().toLowerCase(), EconomyAction.RANDOM_TELEPORT.defaultCost,
            EconomyAction.BACK_COMMAND.name().toLowerCase(), EconomyAction.BACK_COMMAND.defaultCost
    );

    public Optional<Double> getEconomyCost(@NotNull EconomyAction action) {
        if (!economy) {
            return Optional.empty();
        }
        final Double cost = economyCosts.get(action.name().toLowerCase());
        if (cost != null && cost > 0d) {
            return Optional.of(cost);
        }
        return Optional.empty();
    }

    // Mapping plugins
    @YamlComment("Display public homes/warps on web maps (DYNMAP, BLUEMAP)")
    @YamlKey("map_hook.enabled")
    public boolean doMapHook = false;

    @YamlKey("map_hook.map_plugin")
    public MappingPlugin mappingPlugin = MappingPlugin.DYNMAP;

    @YamlKey("map_hook.show_public_homes")
    public boolean publicHomesOnMap = true;

    @YamlKey("map_hook.show_warps")
    public boolean warpsOnMap = true;


    // Disabled commands
    @YamlComment("Disabled commands (e.g. ['/home', '/warp'] to disable /home and /warp)")
    @YamlKey("disabled_commands")
    public List<String> disabledCommands = Collections.emptyList();

    @SuppressWarnings("unused")
    public Settings() {
    }

    /**
     * Represents the names of tables in the database
     */
    public enum TableName {
        PLAYER_DATA("huskhomes_users"),
        POSITION_DATA("huskhomes_position_data"),
        SAVED_POSITION_DATA("huskhomes_saved_positions"),
        HOME_DATA("huskhomes_homes"),
        WARP_DATA("huskhomes_warps"),
        TELEPORT_DATA("huskhomes_teleports");

        private final String defaultName;

        TableName(@NotNull String defaultName) {
            this.defaultName = defaultName;
        }
    }

    /**
     * Represents where a chat message should display
     */
    public enum MessageDisplayType {
        MESSAGE,
        ACTION_BAR,
        SUBTITLE,
        TITLE
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

    /**
     * Identifies actions that incur an economic cost if economy is enabled
     */
    public enum EconomyAction {
        ADDITIONAL_HOME_SLOT(100.00, "economy_action_additional_home_slot"),
        MAKE_HOME_PUBLIC(50.00, "economy_action_make_home_public"),
        RANDOM_TELEPORT(25.00, "economy_action_random_teleport"),
        BACK_COMMAND(0.00, "economy_action_back_command");

        private final double defaultCost;
        @NotNull
        public final String confirmationLocaleId;

        EconomyAction(final double defaultCost, @NotNull String confirmationLocaleId) {
            this.defaultCost = defaultCost;
            this.confirmationLocaleId = confirmationLocaleId;
        }
    }

    /**
     * Identifies types of databases
     */
    public enum DatabaseType {
        MYSQL("MySQL"),
        SQLITE("SQLite");

        @NotNull
        public final String displayName;

        DatabaseType(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Identifies types of network messenger
     */
    public enum MessengerType {
        PLUGIN_MESSAGE("Plugin Messages"),
        REDIS("Redis");

        @NotNull
        public final String displayName;

        MessengerType(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Identifies types of supported Map plugins
     */
    public enum MappingPlugin {
        DYNMAP("Dynmap"),
        BLUEMAP("BlueMap");

        @NotNull
        public final String displayName;

        MappingPlugin(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }

}
