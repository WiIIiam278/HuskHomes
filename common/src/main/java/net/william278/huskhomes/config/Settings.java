/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.config;

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Plugin settings, read from config.yml
 */
@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃       HuskHomes Config       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://william278.net/project/huskhomes/
        ┣╸ Config Help: https://william278.net/docs/huskhomes/config-files/
        ┗╸ Documentation: https://william278.net/docs/huskhomes/""")
public class Settings {

    // Top-level settings
    @YamlComment("Locale of the default language file to use. Docs: https://william278.net/docs/huskhomes/translations")
    @YamlKey("language")
    private String language = "en-gb";

    @YamlComment("Whether to automatically check for plugin updates on startup")
    @YamlKey("check_for_updates")
    private boolean checkForUpdates = true;

    // Database settings
    @YamlComment("Type of database to use (SQLITE, H2, MYSQL or MARIADB)")
    @YamlKey("database.type")
    private Database.Type databaseType = Database.Type.SQLITE;

    @YamlComment("Specify credentials here if you are using MYSQL or MARIADB as your database type")
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
    private String mySqlConnectionParameters = "?autoReconnect=true"
            + "&useSSL=false"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8";

    @YamlComment("MYSQL / MARIADB database Hikari connection pool properties. "
            + "Don't modify this unless you know what you're doing!")
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

    @YamlComment("Names of tables to use on your database. Don't modify this unless you know what you're doing!")
    @YamlKey("database.table_names")
    private TreeMap<String, String> tableNames = new TreeMap<>(Database.Table.getConfigMap());


    // General settings
    @YamlComment("The maximum homes a user can create. Override with the huskhomes.max_homes.<number> permission.")
    @YamlKey("general.max_homes")
    private int maxHomes = 10;

    @YamlComment("The maximum public homes a user can create. "
            + "Override with the huskhomes.max_public_homes.<number> permission.")
    @YamlKey("general.max_public_homes")
    private int maxPublicHomes = 10;

    @YamlComment("Whether permission limits (i.e. huskhomes.max_homes.<number>) "
            + "should stack if the user inherits multiple nodes.")
    @YamlKey("general.stack_permission_limits")
    private boolean stackPermissionLimits = false;

    @YamlComment("Whether users require a permission (huskhomes.command.warp.<warp_name>) to use warps")
    @YamlKey("general.permission_restrict_warps")
    private boolean permissionRestrictWarps = false;

    @YamlComment("Whether running /sethome <name> or /setwarp <name> when a home/warp already exists should overwrite.")
    @YamlKey("general.overwrite_existing_homes_warps")
    private boolean overwriteExistingHomesWarps = true;

    @YamlComment("How long a player has to stand still and not take damage for when teleporting (in seconds) ")
    @YamlKey("general.teleport_warmup_time")
    private int teleportWarmupTime = 5;

    @YamlComment("Where the teleport warmup timer should display (CHAT, ACTION_BAR, TITLE, SUBTITLE or NONE)")
    @YamlKey("general.teleport_warmup_display")
    private Locales.DisplaySlot teleportWarmupDisplay = Locales.DisplaySlot.ACTION_BAR;

    @YamlComment("How long before received teleport requests expire (in seconds)")
    @YamlKey("general.teleport_request_expiry_time")
    private int teleportRequestExpiryTime = 60;

    @YamlComment("Whether /tpahere should use the location of the sender when sent. Docs: https://william278.net/docs/huskhomes/strict-tpahere/")
    @YamlKey("general.strict_tpa_here_requests")
    private boolean strictTpaHereRequests = true;

    @YamlComment("Whether home or warp names should be case insensitive (i.e. allow /home HomeOne and /home homeone)")
    @YamlKey("general.case_insensitive_names")
    private boolean caseInsensitiveNames = false;

    @YamlComment("Whether home or warp names should allow UTF-8 characters (i.e. allow /home 你好)")
    @YamlKey("general.allow_unicode_names")
    private boolean allowUnicodeNames = false;

    @YamlComment("Whether home or warp descriptions should allow UTF-8 characters")
    @YamlKey("general.allow_unicode_descriptions")
    private boolean allowUnicodeDescriptions = true;

    @YamlComment("Whether /back should work to teleport the user to where they died")
    @YamlKey("general.back_command_return_by_death")
    private boolean backCommandReturnByDeath = true;

    @YamlComment("Whether /back should work with other plugins that use the PlayerTeleportEvent (can cause conflicts)")
    @YamlKey("general.back_command_save_teleport_event")
    private boolean backCommandSaveOnTeleportEvent = false;

    @YamlComment("How many items should be displayed per-page in chat menu lists")
    @YamlKey("general.list_items_per_page")
    private int listItemsPerPage = 12;

    @YamlComment("Whether teleportation should be carried out asynchronously (ensuring chunks load before teleporting)")
    @YamlKey("general.asynchronous_teleports")
    private boolean asynchronousTeleports = true;

    @YamlComment("Whether to play sound effects")
    @YamlKey("general.play_sound_effects")
    private boolean playSoundEffects = true;

    @YamlComment("Which sound effects to play for various actions")
    @YamlKey("general.sound_effects")
    private TreeMap<String, String> soundEffects = new TreeMap<>(SoundEffectAction.getConfigMap());

    @YamlComment("Whether to provide modern, rich TAB suggestions for commands (if available)")
    @YamlKey("general.brigadier_tab_completion")
    private boolean brigadierTabCompletion = true;


    // Cross-server settings
    @YamlComment("Enable teleporting across your proxy network. Requires database type to be MYSQL")
    @YamlKey("cross_server.enabled")
    private boolean crossServer = false;

    @YamlComment("The type of message broker to use for cross-server communication. Options: PLUGIN_MESSAGE, REDIS")
    @YamlKey("cross_server.messenger_type")
    private Broker.Type messageBrokerType = Broker.Type.PLUGIN_MESSAGE;

    @YamlComment("Specify a common ID for grouping servers running HuskHomes on your proxy. "
            + "Don't modify this unless you know what you're doing!")
    @YamlKey("cross_server.cluster_id")
    private String clusterId = "";

    @YamlComment("Define a single global /spawn for your network via a warp. Docs: https://william278.net/docs/huskhomes/global-spawn/")
    @YamlKey("cross_server.global_spawn.enabled")
    private boolean globalSpawn = false;

    @YamlComment("The name of the warp to use as the global spawn.")
    @YamlKey("cross_server.global_spawn.warp_name")
    private String globalSpawnName = "Spawn";

    @YamlComment("Whether player respawn positions should work cross-server. Docs: https://william278.net/docs/huskhomes/global-respawning/")
    @YamlKey("cross_server.global_respawning")
    private boolean globalRespawning = false;

    @YamlComment("Specify credentials here if you are using REDIS as your messenger_type. Docs: https://william278.net/docs/huskhomes/redis-support/")
    @YamlKey("cross_server.redis_credentials.host")
    private String redisHost = "localhost";

    @YamlKey("cross_server.redis_credentials.port")
    private int redisPort = 6379;

    @YamlKey("cross_server.redis_credentials.password")
    private String redisPassword = "";

    @YamlKey("cross_server.redis_credentials.use_ssl")
    private boolean redisUseSsl = false;


    // Rtp command settings
    @YamlComment("Radius around the spawn point in which players cannot be random teleported to")
    @YamlKey("rtp.radius")
    private int rtpRadius = 5000;

    @YamlComment("Radius of the spawn area in which players cannot be random teleported to")
    @YamlKey("rtp.spawn_radius")
    private int rtpSpawnRadius = 500;

    @YamlComment("Mean of the normal distribution used to calculate the distance from the center of the world")
    @YamlKey("rtp.distribution_mean")
    private float rtpDistributionMean = 0.75f;

    @YamlComment("Standard deviation of the normal distribution for distributing players randomly")
    @YamlKey("rtp.distribution_deviation")
    private float rtpDistributionStandardDeviation = 2f;

    @YamlComment("List of worlds in which /rtp is disabled. Please note that /rtp does not work well in the nether.")
    @YamlKey("rtp.restricted_worlds")
    private List<String> rtpRestrictedWorlds = List.of("world_nether", "world_the_end");


    // Cooldown settings
    @YamlComment("Whether to apply a cooldown between performing certain actions")
    @YamlKey("cooldowns.enabled")
    private boolean cooldowns = true;

    @YamlComment("Set a cooldown between performing actions (in seconds). Docs: https://william278.net/docs/huskhomes/cooldowns/")
    @YamlKey("cooldowns.cooldown_times")
    private TreeMap<String, Integer> cooldownTimes = new TreeMap<>(TransactionResolver.Action.getCooldownsConfigMap());


    // Economy settings
    @YamlComment("Enable economy plugin integration (requires Vault)")
    @YamlKey("economy.enabled")
    private boolean economy = false;

    @YamlComment("Specify how many homes players can set for free, before they need to pay for more slots")
    @YamlKey("economy.free_home_slots")
    private int freeHomeSlots = 5;

    @YamlComment("Charge money for perform certain actions. Docs: https://william278.net/docs/huskhomes/economy-hook/")
    @YamlKey("economy.costs")
    private TreeMap<String, Double> economyCosts = new TreeMap<>(TransactionResolver.Action.getEconomyCostsConfigMap());

    // Mapping plugins
    @YamlComment("Display public homes/warps on your Dynmap, BlueMap or Pl3xMap. Docs: https://william278.net/docs/huskhomes/map-hooks")
    @YamlKey("map_hook.enabled")
    private boolean doMapHook = true;

    @YamlComment("Show public homes on the web map")
    @YamlKey("map_hook.show_public_homes")
    private boolean publicHomesOnMap = true;

    @YamlComment("Show warps on the web map")
    @YamlKey("map_hook.show_warps")
    private boolean warpsOnMap = true;


    // Disabled commands
    @YamlComment("List of commands to disable (e.g. ['/home', '/warp'] to disable /home and /warp)")
    @YamlKey("disabled_commands")
    private List<String> disabledCommands = Collections.emptyList();


    @SuppressWarnings("unused")
    private Settings() {
    }

    @NotNull
    public String getLanguage() {
        return language;
    }

    public boolean doCheckForUpdates() {
        return checkForUpdates;
    }

    @NotNull
    public Database.Type getDatabaseType() {
        return databaseType;
    }

    @NotNull
    public String getMySqlHost() {
        return mySqlHost;
    }

    public int getMySqlPort() {
        return mySqlPort;
    }

    @NotNull
    public String getMySqlDatabase() {
        return mySqlDatabase;
    }

    @NotNull
    public String getMySqlUsername() {
        return mySqlUsername;
    }

    @NotNull
    public String getMySqlPassword() {
        return mySqlPassword;
    }

    @NotNull
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

    @NotNull
    public TreeMap<String, String> getTableNames() {
        final TreeMap<String, String> tableNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        tableNames.putAll(this.tableNames);
        return tableNames;
    }

    @NotNull
    public String getTableName(@NotNull Database.Table table) {
        return Optional.ofNullable(getTableNames().get(table.name())).orElse(table.getDefaultName());
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

    public boolean doPermissionRestrictWarps() {
        return permissionRestrictWarps;
    }

    public boolean doOverwriteExistingHomesWarps() {
        return overwriteExistingHomesWarps;
    }

    public int getTeleportWarmupTime() {
        return teleportWarmupTime;
    }

    @NotNull
    public Locales.DisplaySlot getTeleportWarmupDisplay() {
        return teleportWarmupDisplay;
    }

    public int getTeleportRequestExpiryTime() {
        return teleportRequestExpiryTime;
    }

    public boolean doStrictTpaHereRequests() {
        return strictTpaHereRequests;
    }

    public boolean caseInsensitiveNames() {
        return caseInsensitiveNames;
    }

    public boolean doAllowUnicodeNames() {
        return allowUnicodeNames;
    }

    public boolean doAllowUnicodeDescriptions() {
        return allowUnicodeDescriptions;
    }

    public boolean doBackCommandReturnByDeath() {
        return backCommandReturnByDeath;
    }

    public boolean doBackCommandSaveOnTeleportEvent() {
        return backCommandSaveOnTeleportEvent;
    }

    public int getListItemsPerPage() {
        return listItemsPerPage;
    }

    public boolean doAsynchronousTeleports() {
        return asynchronousTeleports;
    }

    public boolean doPlaySoundEffects() {
        return playSoundEffects;
    }

    @NotNull
    public TreeMap<String, String> getSoundEffects() {
        final TreeMap<String, String> soundEffects = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        soundEffects.putAll(this.soundEffects);
        return soundEffects;
    }

    public Optional<String> getSoundEffect(@NotNull SoundEffectAction action) {
        if (!doPlaySoundEffects()) {
            return Optional.empty();
        }
        return Optional.ofNullable(getSoundEffects().get(action.name()));
    }

    public boolean doCrossServer() {
        return crossServer;
    }

    @NotNull
    public Broker.Type getBrokerType() {
        return messageBrokerType;
    }

    @NotNull
    public String getClusterId() {
        return clusterId;
    }

    public boolean isGlobalSpawn() {
        return globalSpawn;
    }

    @NotNull
    public String getGlobalSpawnName() {
        return globalSpawnName;
    }

    public boolean isGlobalRespawning() {
        return globalRespawning;
    }

    @NotNull
    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    @NotNull
    public String getRedisPassword() {
        return redisPassword;
    }

    public boolean useRedisSsl() {
        return redisUseSsl;
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

    public boolean isWorldRtpRestricted(@NotNull World world) {
        final String worldName = world.getName();
        final String filteredName = worldName.startsWith("minecraft:") ? worldName.substring(10) : worldName;
        return rtpRestrictedWorlds.stream()
                .map(name -> name.startsWith("minecraft:") ? name.substring(10) : name)
                .anyMatch(name -> name.equalsIgnoreCase(filteredName));
    }

    public boolean doCooldowns() {
        return cooldowns;
    }

    @NotNull
    public TreeMap<String, Integer> getCooldownTimes() {
        final TreeMap<String, Integer> cooldownTimes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        cooldownTimes.putAll(this.cooldownTimes);
        return cooldownTimes;
    }

    public long getCooldown(@NotNull TransactionResolver.Action action) {
        if (!doCooldowns()) {
            return 0;
        }
        final Integer cooldown = getCooldownTimes().get(action.name());
        if (cooldown != null && cooldown > 0) {
            return cooldown;
        }
        return 0;
    }

    public boolean doEconomy() {
        return economy;
    }

    public int getFreeHomeSlots() {
        return freeHomeSlots;
    }

    @NotNull
    public TreeMap<String, Double> getEconomyCosts() {
        final TreeMap<String, Double> economyCosts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        economyCosts.putAll(this.economyCosts);
        return economyCosts;
    }

    public Optional<Double> getEconomyCost(@NotNull TransactionResolver.Action action) {
        if (!doEconomy()) {
            return Optional.empty();
        }
        final Double cost = getEconomyCosts().get(action.name());
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
        return disabledCommands.stream()
                .anyMatch(disabled -> {
                    final String command = (disabled.startsWith("/") ? disabled.substring(1) : disabled);
                    return command.equalsIgnoreCase(type.getName())
                            || type.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(command));
                });
    }

    public boolean doBrigadierTabCompletion() {
        return brigadierTabCompletion;
    }

    /**
     * Represents actions that have a sound effect associated with performing them.
     */
    public enum SoundEffectAction {
        TELEPORTATION_COMPLETE("entity.enderman.teleport"),
        TELEPORTATION_WARMUP("block.note_block.banjo"),
        TELEPORTATION_CANCELLED("entity.item.break"),
        TELEPORT_REQUEST_RECEIVED("entity.experience_orb.pickup");

        private final String defaultEffect;

        SoundEffectAction(@NotNull String defaultEffect) {
            this.defaultEffect = defaultEffect;
        }

        @NotNull
        public static Map<String, String> getConfigMap() {
            return Arrays.stream(values()).collect(Collectors.toMap(
                    action -> action.name().toLowerCase(Locale.ENGLISH),
                    action -> action.defaultEffect
            ));
        }

    }

}
