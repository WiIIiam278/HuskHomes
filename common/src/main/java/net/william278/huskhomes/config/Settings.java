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

import com.google.common.collect.Lists;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Settings {

    static final String CONFIG_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃       HuskHomes Config       ┃
            ┃    Developed by William278   ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
            ┣╸ Information: https://william278.net/project/huskhomes/
            ┣╸ Config Help: https://william278.net/docs/huskhomes/config-files/
            ┗╸ Documentation: https://william278.net/docs/huskhomes/""";

    // Top-level settings
    @Comment("Locale of the default language file to use. Docs: https://william278.net/docs/huskhomes/translations")
    private String language = Locales.DEFAULT_LOCALE;

    @Comment("Whether to automatically check for plugin updates on startup")
    private boolean checkForUpdates = true;

    // Database settings
    @Comment("Database settings")
    private DatabaseSettings database = new DatabaseSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DatabaseSettings {

        @Comment("Type of database to use (SQLITE, H2, MYSQL, MARIADB, or POSTGRESQL)")
        private Database.Type type = Database.Type.SQLITE;

        @Comment("Specify credentials here if you are using MYSQL, MARIADB, or POSTGRESQL")
        private DatabaseCredentials credentials = new DatabaseCredentials();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class DatabaseCredentials {
            private String host = "localhost";
            private int port = 3306;
            private String database = "huskhomes";
            private String username = "root";
            private String password = "pa55w0rd";
            private String parameters = String.join("&",
                    "?autoReconnect=true", "useSSL=false",
                    "useUnicode=true", "characterEncoding=UTF-8");
        }

        @Comment({"MYSQL / MARIADB / POSTGRESQL database Hikari connection pool properties",
                "Don't modify this unless you know what you're doing!"})
        private PoolOptions poolOptions = new PoolOptions();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class PoolOptions {
            private int size = 12;
            private int idle = 12;
            private long lifetime = 1800000;
            private long keepAlive = 30000;
            private long timeout = 20000;
        }

        @Comment("Names of tables to use on your database. Don't modify this unless you know what you're doing!")
        private Map<Database.Table, String> tableNames = Database.Table.getConfigMap();

        @NotNull
        public String getTableName(@NotNull Database.Table tableName) {
            return Optional.ofNullable(tableNames.get(tableName)).orElse(tableName.getDefaultName());
        }
    }

    @Comment("General settings")
    private GeneralSettings general = new GeneralSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GeneralSettings {
        @Comment("The maximum homes a user can create. Override with the huskhomes.max_homes.<number> permission.")
        private int maxHomes = 10;

        @Comment("The maximum public homes a user can create. "
                + "Override with the huskhomes.max_public_homes.<number> permission.")
        private int maxPublicHomes = 10;

        @Comment({"What character to use between owner usernames and home names when specifying a home or public home.",
                "(e.g. the '.' in: /phome username.home_name). Change if you're using '.' as the Bedrock user prefix."})
        private String homeDelimiter = ".";

        @Comment("Whether permission limits (i.e. huskhomes.max_homes.<number>) should stack "
                + "if the user inherits multiple nodes.")
        private boolean stackPermissionLimits = false;

        @Comment("Whether users require a permission (huskhomes.warp.<warp_name>) to use warps")
        private boolean permissionRestrictWarps = false;

        @Comment("How long a player has to stand still and not take damage for when teleporting (in seconds) ")
        private int teleportWarmupTime = 5;

        @Comment("Whether the teleport warmup timer should be cancelled if the player takes damage")
        private boolean teleportWarmupCancelOnDamage = true;

        @Comment("Whether the teleport warmup timer should be cancelled if the player moves")
        private boolean teleportWarmupCancelOnMove = true;

        @Comment("Where the teleport warmup timer should display (CHAT, ACTION_BAR, TITLE, SUBTITLE or NONE)")
        private Locales.DisplaySlot teleportWarmupDisplay = Locales.DisplaySlot.ACTION_BAR;

        @Comment("How long the player should be invulnerable for after teleporting (in seconds)")
        private int teleportInvulnerabilityTime = 0;

        @Comment("How long before received teleport requests expire (in seconds)")
        private int teleportRequestExpiryTime = 60;

        @Comment("Whether /tpahere should use the location of the sender when sent. "
                + "Docs: https://william278.net/docs/huskhomes/strict-tpahere/")
        private boolean strictTpaHereRequests = true;

        @Comment("How many items should be displayed per-page in chat menu lists")
        private int listItemsPerPage = 12;

        @Comment("Whether the user should always be put back at the /spawn point when they die "
                + "(ignores beds/respawn anchors)")
        private boolean alwaysRespawnAtSpawn = false;

        @Comment("Whether teleportation should be carried out async (ensuring chunks load before teleporting)")
        private boolean teleportAsync = true;

        @Comment("Settings for home and warp names")
        private NameSettings names = new NameSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class NameSettings {
            @Comment("Whether running /sethome <name> or /setwarp <name> when one already exists should overwrite.")
            private boolean overwriteExisting = true;

            @Comment("Whether home or warp names should be case insensitive (i.e. allow /home HomeOne & /home homeone)")
            private boolean caseInsensitive = false;

            @Comment("Whether home and warp names should be restricted to a regex filter."
                    + "Set this to false to allow full UTF-8 names (i.e. allow /home 你好).")
            private boolean restrict = true;

            @Comment("Regex which home and warp names must match. Names have a max length of 16 characters")
            private String regex = "[a-zA-Z0-9-_]*";
        }

        @Comment("Settings for home and warp descriptions")
        private DescriptionSettings descriptions = new DescriptionSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class DescriptionSettings {
            @Comment("Whether home/warp descriptions should be restricted to a regex filter. "
                    + "Set this to true to restrict UTF-8 usage.")
            private boolean restrict = false;

            @Comment("Regex which home and warp descriptions must match. "
                    + "A hard max length of 256 characters is enforced")
            private String regex = "\\A\\p{ASCII}*\\z";
        }

        @Comment("Settings for the /back command")
        private BackCommandSettings backCommand = new BackCommandSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class BackCommandSettings {
            @Comment("Whether /back should work to teleport the user to where they died")
            private boolean returnByDeath = true;

            @Comment("Whether /back should work with other plugins that use the PlayerTeleportEvent (can conflict)")
            private boolean saveOnTeleportEvent = false;

            @Comment({"List of world names where the /back command cannot RETURN the player to. ",
                    "A user's last position won't be updated if they die or teleport from these worlds, but they still "
                            + "will be able to use the command while IN the world."})
            private List<String> restrictedWorlds = new ArrayList<>();

            public boolean canReturnToWorld(@NotNull World world) {
                final String name = world.getName();
                final String formattedName = name.replace("minecraft:", "");
                return restrictedWorlds.stream()
                        .map(n -> n.replace("minecraft:", ""))
                        .noneMatch(n -> n.equalsIgnoreCase(formattedName));
            }
        }

        @Comment("Settings for sound effects")
        private SoundEffectSettings soundEffects = new SoundEffectSettings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class SoundEffectSettings {
            @Comment("Whether to play sound effects")
            private boolean enabled = true;

            @Comment("Map of sound effect actions to types")
            private Map<SoundEffectAction, String> types = SoundEffectAction.getDefaults();

            @NotNull
            public Optional<String> get(@NotNull SoundEffectAction action) {
                if (!enabled) {
                    return Optional.empty();
                }
                return Optional.ofNullable(types.get(action));
            }
        }
    }

    @Comment("Cross-server settings")
    private CrossServerSettings crossServer = new CrossServerSettings();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CrossServerSettings {
        @Comment("Whether to enable cross-server mode for teleporting across your proxy network.")
        private boolean enabled = false;

        @Comment({"The cluster ID, for if you're networking multiple separate groups of HuskHomes-enabled servers.",
                "Do not change unless you know what you're doing"})
        private String clusterId = "main";

        @Comment("Type of network message broker to ues for cross-server networking (PLUGIN_MESSAGE or REDIS)")
        private Broker.Type brokerType = Broker.Type.PLUGIN_MESSAGE;

        @Comment("Settings for if you're using REDIS as your message broker")
        private RedisSettings redis = new RedisSettings();

        @Getter
        @Configuration
        @NoArgsConstructor
        public static class RedisSettings {
            private String host = "localhost";
            private int port = 6379;
            @Comment("Password for your Redis server. Leave blank if you're not using a password.")
            private String password = "";
            private boolean useSsl = false;

            @Comment({"Settings for if you're using Redis Sentinels.",
                    "If you're not sure what this is, please ignore this section."})
            private SentinelSettings sentinel = new SentinelSettings();

            @Getter
            @Configuration
            @NoArgsConstructor
            public static class SentinelSettings {
                private String masterName = "";
                @Comment("List of host:port pairs")
                private List<String> nodes = Lists.newArrayList();
                private String password = "";
            }
        }

        @Comment("Define a single global /spawn for your network via a warp. "
                + "Docs: https://william278.net/docs/huskhomes/global-spawn/")
        private GlobalSpawnSettings globalSpawn = new GlobalSpawnSettings();

        @Getter
        @Configuration
        @NoArgsConstructor
        public static class GlobalSpawnSettings {
            @Comment("Whether to define a single global /spawn for your network via a warp.")
            private boolean enabled = false;
            @Comment("The name of the warp to use as the global spawn.")
            private String warpName = "Spawn";
        }

        @Comment("Whether player respawn positions should work cross-server. "
                + "Docs: https://william278.net/docs/huskhomes/global-respawning/")
        private boolean globalRespawning = false;
    }

    @Comment("Random teleport (/rtp) settings.")
    private RtpSettings rtp = new RtpSettings();

    @Getter
    @Configuration
    @NoArgsConstructor
    public static class RtpSettings {

        @Comment({"Radial region around the /spawn position where players CAN be randomly teleported.",
                "If no /spawn has been set, (0, 0) will be used instead."})
        private RtpRadius region = new RtpRadius();

        @Getter
        @Configuration
        @NoArgsConstructor
        public static class RtpRadius {
            private int min = 500;
            private int max = 5000;
        }

        @Comment("Mean of the normal distribution used to calculate the distance from the center of the world")
        private float distributionMean = 0.75f;

        @Comment("Standard deviation of the normal distribution for distributing players randomly")
        private float distributionStandardDeviation = 2.0f;

        @Comment({"Set the minimum random teleportation height for each world", "List of world_name:height pairs"})
        private List<String> minHeight = Lists.newArrayList();

        @Comment({"Set the maximum random teleportation height for each world", "List of world_name:height pairs"})
        private List<String> maxHeight = Lists.newArrayList();

        @Comment("List of worlds in which /rtp is disabled. Please note that /rtp does not work well in the nether.")
        private List<String> restrictedWorlds = List.of("world_nether", "world_the_end");

        public boolean isWorldRtpRestricted(@NotNull World world) {
            final String name = world.getName();
            final String formattedName = name.replace("minecraft:", "");
            return restrictedWorlds.stream()
                    .map(n -> n.replace("minecraft:", ""))
                    .anyMatch(n -> n.equalsIgnoreCase(formattedName));
        }

        @Comment("Whether or not RTP should perform cross-server.")
        private boolean crossServer = false;

        @Comment({"List of server in which /rtp is allowed. (Only relevant when using cross server mode WITH REDIS)",
                "If a server is not defined here the RTP logic has no way of knowing its existence."})
        private Map<String, List<String>> randomTargetServers = new HashMap<>(
                Map.of("server", List.of("world", "world_nether", "world_the_end"))
        );
    }

    @Comment("Action cooldown settings. Docs: https://william278.net/docs/huskhomes/cooldowns")
    private CooldownSettings cooldowns = new CooldownSettings();

    @Getter
    @Configuration
    @NoArgsConstructor
    public static class CooldownSettings {

        @Comment("Whether to apply a cooldown between performing certain actions")
        private boolean enabled = true;

        @Comment("Map of cooldown times to actions")
        private Map<TransactionResolver.Action, Long> cooldownTimes = TransactionResolver.Action.getCooldownTimes();

        public long getCooldown(@NotNull TransactionResolver.Action action) {
            return enabled ? cooldownTimes.getOrDefault(action, 0L) : 0L;
        }
    }

    // Economy settings
    @Comment("Economy settings. Docs: https://william278.net/docs/huskhomes/economy-hook")
    private EconomySettings economy = new EconomySettings();

    @Getter
    @Configuration
    @NoArgsConstructor
    public static class EconomySettings {

        @Comment("Enable economy plugin integration (requires Vault and a compatible Economy plugin)")
        private boolean enabled = false;

        @Comment("Map of economy actions to costs.")
        private Map<TransactionResolver.Action, Double> economyCosts = TransactionResolver.Action.getEconomyCosts();

        @Comment("Specify how many homes players can set for free, before they need to pay for more slots")
        private int freeHomeSlots = 5;

        public Optional<Double> getCost(@NotNull TransactionResolver.Action action) {
            if (!enabled) {
                return Optional.empty();
            }
            return economyCosts.containsKey(action) ? Optional.of(economyCosts.get(action)) : Optional.empty();
        }
    }

    @Comment("Plan hook settings. Docs: https://william278.net/docs/huskhomes/plan-hook")
    private PlanHookSettings plan = new PlanHookSettings();

    @Getter
    @Configuration
    @NoArgsConstructor
    public static class PlanHookSettings {

        @Comment("Hook into Player Analytics to provide HuskHomes statistics in your web dashboard.")
        private boolean enabled = true;

    }


    @Comment("LuckPerms hook settings. Docs: https://william278.net/docs/huskhomes/luckperms-hook")
    private LuckPermsHookSettings luckperms = new LuckPermsHookSettings();

    @Getter
    @Configuration
    @NoArgsConstructor
    public static class LuckPermsHookSettings {

        @Comment("Hook into LuckPerms for more accurate numerical permission calculations")
        private boolean enabled = true;

    }

    @Comment("Web map hook settings. Docs: https://william278.net/docs/huskhomes/map-hooks")
    private MapHookSettings mapHook = new MapHookSettings();

    @Getter
    @Configuration
    @NoArgsConstructor
    public static class MapHookSettings {

        @Comment("Display public homes/warps on your Dynmap, BlueMap or Pl3xMap")
        private boolean enabled = true;

        @Comment("Show public homes on the web map")
        private boolean showPublicHomes = true;

        @Comment("Show warps on the web map")
        private boolean showWarps = true;
    }

    @Comment("List of commands to disable (e.g. ['/home', '/warp'] to disable /home and /warp)")
    private List<String> disabledCommands = Lists.newArrayList();

    public boolean isCommandDisabled(@NotNull Command type) {
        return disabledCommands.stream()
                .anyMatch(disabled -> {
                    final String command = (disabled.startsWith("/") ? disabled.substring(1) : disabled);
                    return command.equalsIgnoreCase(type.getName())
                            || type.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(command));
                });
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
        public static Map<SoundEffectAction, String> getDefaults() {
            return Arrays.stream(values()).collect(Collectors.toMap(
                    action -> action,
                    action -> action.defaultEffect,
                    (a, b) -> a,
                    TreeMap::new
            ));
        }
    }

}
