package net.william278.huskhomes.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Settings used for the plugin, as read from the config file
 */
public class Settings {

    /**
     * Map of {@link ConfigOption}s read from the config file
     */
    private final HashMap<ConfigOption, Object> configOptions;

    // Load the settings from the document
    private Settings(YamlDocument config) {
        this.configOptions = new HashMap<>();
        Arrays.stream(ConfigOption.values()).forEach(configOption -> configOptions
                .put(configOption, switch (configOption.optionType) {
                    case BOOLEAN -> configOption.getBooleanValue(config);
                    case STRING -> configOption.getStringValue(config);
                    case DOUBLE -> configOption.getDoubleValue(config);
                    case FLOAT -> configOption.getFloatValue(config);
                    case INTEGER -> configOption.getIntValue(config);
                    case STRING_LIST -> configOption.getStringListValue(config);
                }));
    }

    /**
     * Get the value of the specified {@link ConfigOption}
     *
     * @param option the {@link ConfigOption} to check
     * @return the value of the {@link ConfigOption} as a boolean
     * @throws ClassCastException if the option is not a boolean
     */
    public boolean getBooleanValue(ConfigOption option) throws ClassCastException {
        return (Boolean) configOptions.get(option);
    }

    /**
     * Get the value of the specified {@link ConfigOption}
     *
     * @param option the {@link ConfigOption} to check
     * @return the value of the {@link ConfigOption} as a string
     * @throws ClassCastException if the option is not a string
     */
    public String getStringValue(ConfigOption option) throws ClassCastException {
        return (String) configOptions.get(option);
    }

    /**
     * Get the value of the specified {@link ConfigOption}
     *
     * @param option the {@link ConfigOption} to check
     * @return the value of the {@link ConfigOption} as a double
     * @throws ClassCastException if the option is not a double
     */
    public double getDoubleValue(ConfigOption option) throws ClassCastException {
        return (Double) configOptions.get(option);
    }

    /**
     * Get the value of the specified {@link ConfigOption}
     *
     * @param option the {@link ConfigOption} to check
     * @return the value of the {@link ConfigOption} as a float
     * @throws ClassCastException if the option is not a float
     */
    public double getFloatValue(ConfigOption option) throws ClassCastException {
        return (Float) configOptions.get(option);
    }

    /**
     * Get the value of the specified {@link ConfigOption}
     *
     * @param option the {@link ConfigOption} to check
     * @return the value of the {@link ConfigOption} as an integer
     * @throws ClassCastException if the option is not an integer
     */
    public int getIntegerValue(ConfigOption option) throws ClassCastException {
        return (Integer) configOptions.get(option);
    }

    /**
     * Get the value of the specified {@link ConfigOption}
     *
     * @param option the {@link ConfigOption} to check
     * @return the value of the {@link ConfigOption} as a string {@link List}
     * @throws ClassCastException if the option is not a string list
     */
    @SuppressWarnings("unchecked")
    public List<String> getStringListValue(ConfigOption option) throws ClassCastException {
        return (List<String>) configOptions.get(option);
    }


    /**
     * Load the settings from a BoostedYaml {@link YamlDocument} config file
     *
     * @param config The loaded {@link YamlDocument} config.yml file
     * @return the loaded {@link Settings}
     */
    public static Settings load(YamlDocument config) {
        return new Settings(config);
    }

    /**
     * Represents an option stored by a path in config.yml
     */
    public enum ConfigOption {
        LANGUAGE("language", OptionType.STRING, "en-gb"),
        CHECK_FOR_UPDATES("check_for_updates", OptionType.BOOLEAN, true),
        DEBUG_LOGGING("debug_logging", OptionType.BOOLEAN, false),
        DATA_STORAGE_TYPE("data_storage_options.storage_type", OptionType.STRING, "SQLite"),
        DATABASE_HOST("data_storage_options.mysql_credentials.host", OptionType.STRING, "localhost"),
        DATABASE_PORT("data_storage_options.mysql_credentials.port", OptionType.INTEGER, 3306),
        DATABASE_NAME("data_storage_options.mysql_credentials.database", OptionType.STRING, "HuskHomes"),
        DATABASE_USERNAME("data_storage_options.mysql_credentials.username", OptionType.STRING, "root"),
        DATABASE_PASSWORD("data_storage_options.mysql_credentials.password", OptionType.STRING, "pa55w0rd"),
        DATABASE_CONNECTION_PARAMS("data_storage_options.mysql_credentials.params", OptionType.STRING, "?autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull"),

        DATABASE_CONNECTION_POOL_MAX_SIZE("data_storage_options.connection_pool_options.maximum_pool_size", OptionType.INTEGER, 10),
        DATABASE_CONNECTION_POOL_MIN_IDLE("data_storage_options.connection_pool_options.minimum_idle", OptionType.INTEGER, 10),
        DATABASE_CONNECTION_POOL_MAX_LIFETIME("data_storage_options.connection_pool_options.maximum_lifetime", OptionType.INTEGER, 1800000),
        DATABASE_CONNECTION_POOL_KEEPALIVE("data_storage_options.connection_pool_options.keepalive_time", OptionType.INTEGER, 0),
        DATABASE_CONNECTION_POOL_TIMEOUT("data_storage_options.connection_pool_options.connection_timeout", OptionType.INTEGER, 5000),

        DATABASE_PLAYER_TABLE_NAME("data_storage_options.table_names.player_data", OptionType.STRING, "huskhomes_player_data"),
        DATABASE_POSITIONS_TABLE_NAME("data_storage_options.table_names.position_data", OptionType.STRING, "huskhomes_position_data"),
        DATABASE_SAVED_POSITIONS_TABLE_NAME("data_storage_options.table_names.saved_positions_data", OptionType.STRING, "huskhomes_saved_positions_data"),
        DATABASE_HOMES_TABLE_NAME("data_storage_options.table_names.home_data", OptionType.STRING, "huskhomes_home_data"),
        DATABASE_WARPS_TABLE_NAME("data_storage_options.table_names.warp_data", OptionType.STRING, "huskhomes_warp_data"),
        DATABASE_TELEPORTS_TABLE_NAME("data_storage_options.table_names.teleport_data", OptionType.STRING, "huskhomes_teleport_data"),


        MAX_HOMES("general.max_sethomes", OptionType.INTEGER, 10),
        AUTO_COMPLETE_PLAYER_NAMES("general.auto_complete_player_names", OptionType.BOOLEAN, true),
        HOME_LIMIT_PERMISSION_STACKING("general.home_limit_permission_stacking", OptionType.BOOLEAN, false),
        TELEPORT_WARMUP_TIME("general.teleport_warmup_time", OptionType.INTEGER, 5),
        TELEPORT_WARMUP_DISPLAY_SLOT("general.teleport_warmup_display", OptionType.STRING, "ACTION_BAR"),
        TELEPORT_REQUEST_EXPIRY_TIME("general.teleport_request_expiry_time", OptionType.INTEGER, 60),
        ALLOW_UNICODE_DESCRIPTIONS("general.allow_unicode_descriptions", OptionType.BOOLEAN, true),
        USE_PAPERLIB_IF_AVAILABLE("general.use_paperlib_if_available", OptionType.BOOLEAN, true),

        TELEPORT_COMPLETE_SOUND("general.sounds.teleportation_complete", OptionType.STRING, "ENTITY_ENDERMAN_TELEPORT"),
        TELEPORT_WARMUP_SOUND("general.sounds.teleportation_warmup", OptionType.STRING, "BLOCK_NOTE_BLOCK_BANJO"),
        TELEPORT_CANCELLED_SOUND("general.sounds.teleportation_cancelled", OptionType.STRING, "ENTITY_ITEM_BREAK"),

        PRIVATE_HOME_LIST_ITEMS_PER_PAGE("general.lists.private_homes_per_page", OptionType.INTEGER, 10),
        PUBLIC_HOME_LIST_ITEMS_PER_PAGE("general.lists.public_homes_per_page", OptionType.INTEGER, 10),
        WARP_LIST_ITEMS_PER_PAGE("general.lists.warps_per_page", OptionType.INTEGER, 10),

        HELP_MENU_HIDE_COMMANDS_WITHOUT_PERMISSION("general.help_menu.hide_commands_without_permission", OptionType.BOOLEAN, true),
        HELP_MENU_HIDE_HUSKHOMES_COMMAND("general.help_menu.hide_huskhomes_command", OptionType.BOOLEAN, false),

        ENABLE_PROXY_MODE("proxy_options.enable_proxy_mode", OptionType.BOOLEAN, false),
        CLUSTER_ID("proxy_options.cluster_id", OptionType.STRING, ""),
        MESSENGER_TYPE("proxy_options.messenger_type", OptionType.STRING, "PLUGIN_MESSAGE"),

        REDIS_HOST("proxy_options.redis_credentials.host", OptionType.STRING, "localhost"),
        REDIS_PORT("proxy_options.redis_credentials.port", OptionType.INTEGER, 6379),
        REDIS_PASSWORD("proxy_options.redis_credentials.password", OptionType.STRING, ""),
        REDIS_USE_SSL("proxy_options.redis_credentials.use_ssl", OptionType.BOOLEAN, false);

        /**
         * The path in the config.yml file to the value
         */
        @NotNull
        public final String configPath;

        /**
         * The {@link OptionType} of this option
         */
        @NotNull
        public final OptionType optionType;

        /**
         * The default value of this option if not set in config
         */
        @Nullable
        private final Object defaultValue;

        ConfigOption(@NotNull String configPath, @NotNull OptionType optionType, @Nullable Object defaultValue) {
            this.configPath = configPath;
            this.optionType = optionType;
            this.defaultValue = defaultValue;
        }

        ConfigOption(@NotNull String configPath, @NotNull OptionType optionType) {
            this.configPath = configPath;
            this.optionType = optionType;
            this.defaultValue = null;
        }

        /**
         * Get the value at the path specified (or return default if set), as a string
         *
         * @param config The {@link YamlDocument} config file
         * @return the value defined in the config, as a string
         */
        public String getStringValue(@NotNull YamlDocument config) {
            return defaultValue != null
                    ? config.getString(configPath, (String) defaultValue)
                    : config.getString(configPath);
        }

        /**
         * Get the value at the path specified (or return default if set), as a boolean
         *
         * @param config The {@link YamlDocument} config file
         * @return the value defined in the config, as a boolean
         */
        public boolean getBooleanValue(@NotNull YamlDocument config) {
            return defaultValue != null
                    ? config.getBoolean(configPath, (Boolean) defaultValue)
                    : config.getBoolean(configPath);
        }

        /**
         * Get the value at the path specified (or return default if set), as a double
         *
         * @param config The {@link YamlDocument} config file
         * @return the value defined in the config, as a double
         */
        public double getDoubleValue(@NotNull YamlDocument config) {
            return defaultValue != null
                    ? config.getDouble(configPath, (Double) defaultValue)
                    : config.getDouble(configPath);
        }

        /**
         * Get the value at the path specified (or return default if set), as a float
         *
         * @param config The {@link YamlDocument} config file
         * @return the value defined in the config, as a float
         */
        public float getFloatValue(@NotNull YamlDocument config) {
            return defaultValue != null
                    ? config.getFloat(configPath, (Float) defaultValue)
                    : config.getFloat(configPath);
        }

        /**
         * Get the value at the path specified (or return default if set), as an int
         *
         * @param config The {@link YamlDocument} config file
         * @return the value defined in the config, as an int
         */
        public int getIntValue(@NotNull YamlDocument config) {
            return defaultValue != null
                    ? config.getInt(configPath, (Integer) defaultValue)
                    : config.getInt(configPath);
        }

        /**
         * Get the value at the path specified (or return default if set), as a string {@link List}
         *
         * @param config The {@link YamlDocument} config file
         * @return the value defined in the config, as a string {@link List}
         */
        public List<String> getStringListValue(@NotNull YamlDocument config) {
            return config.getStringList(configPath, new ArrayList<>());
        }

        /**
         * Represents the type of the object
         */
        public enum OptionType {
            BOOLEAN,
            STRING,
            DOUBLE,
            FLOAT,
            INTEGER,
            STRING_LIST
        }
    }

}
