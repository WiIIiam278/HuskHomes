package net.william278.huskhomes.config;

import net.william278.annotaml.EmbeddedYaml;
import net.william278.annotaml.KeyPath;
import net.william278.annotaml.YamlFile;
import org.jetbrains.annotations.NotNull;

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

    public boolean checkForUpdates = true;

    public boolean debugLogging = false;


    // Database settings

    @KeyPath("database.type")
    public DatabaseType databaseType = DatabaseType.SQLITE;

    @KeyPath("database.mysql.credentials.host")
    public String mySqlHost = "localhost";

    @KeyPath("database.mysql.credentials.port")
    public int mySqlPort = 3306;

    @KeyPath("database.mysql.credentials.database")
    public String mySqlDatabase = "HuskHomes";

    @KeyPath("database.mysql.credentials.username")
    public String mySqlUsername = "root";

    @KeyPath("database.mysql.credentials.password")
    public String mySqlPassword = "pa55w0rd";

    @KeyPath("database.mysql.credentials.parameters")
    public String mySqlConnectionParameters = "?useUnicode=true&characterEncoding=UTF-8&useSSL=false";

    @KeyPath("database.mysql.connection_pool")
    public ConnectionPoolOptions mySqlPoolOptions = new ConnectionPoolOptions();

    @KeyPath("database.table_names")
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

    @KeyPath("general.max_homes")
    public int maxHomes = 10;

    @KeyPath("general.max_public_homes")
    public int maxPublicHomes = 10;

    @KeyPath("general.stack_permission_limits")
    public boolean stackPermissionLimits = true;

    @KeyPath("general.permission_restrict_warps")
    public boolean permissionRestrictWarps = false;

    @KeyPath("general.teleport_warmup_time")
    public int teleportWarmupTime = 5;

    @KeyPath("general.teleport_warmup_display")
    public MessageDisplayType teleportWarmupDisplay = MessageDisplayType.ACTION_BAR;

    @KeyPath("general.teleport_request_expiry_time")
    public int teleportRequestExpiryTime = 60;

    @KeyPath("general.list_items_per_page")
    public int listItemsPerPage = 12;

    @KeyPath("general.strict_tpa_here_requests")
    public boolean strictTpaHereRequests = true;

    @KeyPath("general.allow_unicode_names")
    public boolean allowUnicodeNames = false;

    @KeyPath("general.allow_unicode_descriptions")
    public boolean allowUnicodeDescriptions = true;

    @KeyPath("general.use_paper_lib")
    public boolean usePaperLib = true;

    @KeyPath("general.play_sound_effects")
    public boolean playSoundEffects = true;

    @KeyPath("general.sound_effects")
    public Map<String, String> soundEffects = Map.of(
            SoundEffectAction.TELEPORTATION_COMPLETE.name().toLowerCase(), SoundEffectAction.TELEPORTATION_COMPLETE.defaultSoundEffect,
            SoundEffectAction.TELEPORTATION_WARMUP.name().toLowerCase(), SoundEffectAction.TELEPORTATION_WARMUP.defaultSoundEffect,
            SoundEffectAction.TELEPORTATION_CANCELLED.name().toLowerCase(), SoundEffectAction.TELEPORTATION_CANCELLED.defaultSoundEffect
    );

    public Optional<String> getSoundEffect(@NotNull SoundEffectAction action) {
        return Optional.ofNullable(soundEffects.get(action.name().toLowerCase()));
    }


    // Cross-server settings
    @KeyPath("cross_server.enabled")
    public boolean crossServer = false;

    @KeyPath("cross_server.messenger_type")
    public MessengerType messengerType = MessengerType.PLUGIN_MESSAGE;

    @KeyPath("cross_server.cluster_id")
    public String clusterId = "";

    @KeyPath("cross_server.global_spawn.enabled")
    public boolean globalSpawn = false;

    @KeyPath("cross_server.global_spawn.warp_name")
    public String globalSpawnName = "Spawn";

    @KeyPath("cross_server.redis_credentials.host")
    public String redisHost = "localhost";

    @KeyPath("cross_server.redis_credentials.port")
    public int redisPort = 6379;

    @KeyPath("cross_server.redis_credentials.password")
    public String redisPassword = "";

    @KeyPath("cross_server.redis_credentials.use_ssl")
    public boolean redisUseSsl = false;


    // Rtp command settings
    @KeyPath("rtp.cooldown_length")
    public int rtpCooldownLength = 10;

    @KeyPath("rtp.radius")
    public int rtpRadius = 5000;

    @KeyPath("rtp.spawn_radius")
    public int rtpSpawnRadius = 500;

    @KeyPath("rtp.distribution_mean")
    public float rtpDistributionMean = 0.75f;

    @KeyPath("rtp.distribution_deviation")
    public float rtpDistributionStandardDeviation = 2f;

    @KeyPath("rtp.location_cache_size")
    public int rtpLocationCacheSize = 10;

    @KeyPath("rtp.restricted_worlds")
    public List<String> rtpRestrictedWorlds = List.of("world_nether", "world_the_end");


    // Economy settings
    @KeyPath("economy.enabled")
    public boolean economy = false;

    @KeyPath("economy.free_home_slots")
    public int freeHomeSlots = 5;

    @KeyPath("economy.costs")
    public Map<String, Double> economyCosts = Map.of(
            EconomyAction.ADDITIONAL_HOME_SLOT.name().toLowerCase(), EconomyAction.ADDITIONAL_HOME_SLOT.defaultCost,
            EconomyAction.MAKE_HOME_PUBLIC.name(), EconomyAction.MAKE_HOME_PUBLIC.defaultCost,
            EconomyAction.RANDOM_TELEPORT.name().toLowerCase(), EconomyAction.RANDOM_TELEPORT.defaultCost,
            EconomyAction.BACK_COMMAND.name(), EconomyAction.BACK_COMMAND.defaultCost
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

    @SuppressWarnings("unused")
    public Settings() {
    }


    /**
     * Options for use in establishing pooled connections to a database
     */
    @EmbeddedYaml
    public static class ConnectionPoolOptions {

        public int size = 10;
        public int idle = 10;
        public long lifetime = 1800000;
        public long keepalive = 0;
        public long timeout = 5000;

        @SuppressWarnings("unused")
        public ConnectionPoolOptions() {
        }
    }

    /**
     * Represents the names of tables in the database
     */
    public enum TableName {
        PLAYER_DATA("huskhomes_player_data"),
        POSITION_DATA("huskhomes_position_data"),
        SAVED_POSITION_DATA("huskhomes_saved_position_data"),
        HOME_DATA("huskhomes_home_data"),
        WARP_DATA("huskhomes_warp_data"),
        TELEPORT_DATA("huskhomes_teleport_data");

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
        SUBTITLE
    }

    /**
     * Represents the sound effect to play when an action happens
     */
    public enum SoundEffectAction {
        TELEPORTATION_COMPLETE("entity_enderman_teleport"),
        TELEPORTATION_WARMUP("block_note_block_banjo"),
        TELEPORTATION_CANCELLED("entity_item_break");
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
        MYSQL,
        SQLITE
    }

    /**
     * Identifies types of network messenger
     */
    public enum MessengerType {
        PLUGIN_MESSAGE,
        REDIS
    }

}
