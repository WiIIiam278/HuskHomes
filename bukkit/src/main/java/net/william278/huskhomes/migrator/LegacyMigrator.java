package net.william278.huskhomes.migrator;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.util.BukkitUpgradeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class LegacyMigrator extends Migrator {

    public Settings.DatabaseType sourceDatabaseType;
    public String sourceMySqlHost;
    public int sourceMySqlPort;
    public String sourceMySqlDatabase;
    public String sourceMySqlUsername;
    public String sourceMySqlPassword;
    public String sourcePlayerDataTable = "huskhomes_player_data";
    public String sourceLocationsDataTable = "huskhomes_location_data";
    public String sourceHomesDataTable = "huskhomes_home_data";
    public String sourceWarpsDataTable = "huskhomes_warp_data";

    public LegacyMigrator(@NotNull HuskHomes implementor) {
        super(implementor);
        this.sourceDatabaseType = implementor.getSettings().databaseType;
        this.sourceMySqlHost = implementor.getSettings().mySqlHost;
        this.sourceMySqlPort = implementor.getSettings().mySqlPort;
        this.sourceMySqlDatabase = implementor.getSettings().mySqlDatabase;
        this.sourceMySqlUsername = implementor.getSettings().mySqlUsername;
        this.sourceMySqlPassword = implementor.getSettings().mySqlPassword;
    }

    public LegacyMigrator(@NotNull HuskHomes implementor, @NotNull BukkitUpgradeUtil bukkitUpgradeUtil) {
        super(implementor);
        this.sourceDatabaseType = bukkitUpgradeUtil.databaseType;

        this.sourceMySqlHost = bukkitUpgradeUtil.mySqlHost;
        this.sourceMySqlPort = bukkitUpgradeUtil.mySqlPort;
        this.sourceMySqlDatabase = bukkitUpgradeUtil.mySqlDatabase;
        this.sourceMySqlUsername = bukkitUpgradeUtil.mySqlUsername;
        this.sourceMySqlPassword = bukkitUpgradeUtil.mySqlPassword;

        this.sourcePlayerDataTable = bukkitUpgradeUtil.sourcePlayerDataTable;
        this.sourceLocationsDataTable = bukkitUpgradeUtil.sourceLocationsDataTable;
        this.sourceHomesDataTable = bukkitUpgradeUtil.sourceHomesDataTable;
        this.sourceWarpsDataTable = bukkitUpgradeUtil.sourceWarpsDataTable;
    }

    @Override
    public CompletableFuture<Boolean> start() {
        return CompletableFuture.supplyAsync(() -> {
            if (sourceDatabaseType == Settings.DatabaseType.SQLITE) {
                migrateSqLiteDatabase();
                return true;
            }

            if (sourceDatabaseType == plugin.getSettings().databaseType
                && sourceMySqlHost.equals(plugin.getSettings().mySqlHost)
                && sourceMySqlPort == plugin.getSettings().mySqlPort) {
                migrateMySqlDatabase();
                return true;
            }

            plugin.getLoggingAdapter().log(Level.SEVERE, "Migration failed! Different source and target databases/types." +
                                                         "Please dump your data from your source database manually and then" +
                                                         " import it to your new database, then attempt migration again.");
            return false;
        });
    }

    private void migrateSqLiteDatabase() {
        plugin.getLoggingAdapter().log(Level.INFO, "Migrating SQLite database, please wait... This may take a while!");

        // Execute the migration script
        plugin.getDatabase().runScript(Objects.requireNonNull(plugin.getResource("migrator/sqlite_migrator.sql")),
                        Map.of("%target_positions_table%", plugin.getSettings().getTableName(Settings.TableName.POSITION_DATA),
                                "%source_positions_table%", sourceLocationsDataTable,
                                "%target_users_table%", plugin.getSettings().getTableName(Settings.TableName.PLAYER_DATA),
                                "%source_users_table%", sourcePlayerDataTable,
                                "%target_saved_positions%", plugin.getSettings().getTableName(Settings.TableName.SAVED_POSITION_DATA),
                                "%source_homes_table%", sourceHomesDataTable,
                                "%target_homes_table%", plugin.getSettings().getTableName(Settings.TableName.HOME_DATA),
                                "%source_warps_table%", sourceWarpsDataTable,
                                "%target_warps_table%", plugin.getSettings().getTableName(Settings.TableName.WARP_DATA)))
                .thenRun(() -> plugin.getLoggingAdapter().log(Level.INFO, "Migration complete!"))
                .exceptionally(e -> {
                    plugin.getLoggingAdapter().log(Level.SEVERE, "Migration of SQLite database failed!", e);
                    return null;
                });
    }

    private void migrateMySqlDatabase() {
        plugin.getLoggingAdapter().log(Level.INFO, "Migrating same MySQL database...");
        //todo write script
    }

    @Override
    public void handleConfigurationCommand(@NotNull String[] args) {
        if (args.length == 2) {
            if (switch (args[0].toLowerCase()) {
                case "database" -> {
                    this.sourceMySqlDatabase = args[1];
                    yield true;
                }
                case "player_data_table" -> {
                    this.sourcePlayerDataTable = args[1];
                    yield true;
                }
                case "locations_data_table" -> {
                    this.sourceLocationsDataTable = args[1];
                    yield true;
                }
                case "homes_data_table" -> {
                    this.sourceHomesDataTable = args[1];
                    yield true;
                }
                case "warps_data_table" -> {
                    this.sourceWarpsDataTable = args[1];
                    yield true;
                }
                default -> false;
            }) {
                plugin.getLoggingAdapter().log(Level.INFO, getHelpMenu());
                plugin.getLoggingAdapter().log(Level.INFO, "Successfully set " + args[0] + " to " +
                                                           obfuscateDataString(args[1]));
            } else {
                plugin.getLoggingAdapter().log(Level.INFO, "Invalid operation, could not set " + args[0] + " to " +
                                                           obfuscateDataString(args[1]) + " (is it a valid option?)");
            }
        } else {
            plugin.getLoggingAdapter().log(Level.INFO, getHelpMenu());
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "legacy";
    }

    @Override
    public @NotNull String getName() {
        return "HuskHomes Legacy (v2.x)";
    }

    @NotNull
    @Override
    public String getHelpMenu() {
        return """
                === HuskHomes v2.x --> v3.x Migration Wizard =========
                This will migrate all user data from HuskHomes v2.x
                to HuskHomes v3.x. The source and target databases
                must be the same, please note.
                                
                [!] Existing data in the database will be wiped. [!]
                                
                STEP 1] Please ensure no players are on any servers
                running HuskHomes. If you're running MySQL, you only
                need to do this once - it's best to only have one
                server running.
                                
                STEP 2] Ensure the following settings are correct:
                                
                Source table names:
                - player_data: %source_players_table%
                - locations_data: %source_locations_table%
                - homes_data: %source_homes_table%
                - warps_data: %source_warps_table%
                                
                Source MySQL database name (ignore if using SQLite):
                - database: %source_mysql_database%
                                
                If any of these are not correct, please correct them
                using the command:
                "huskhomes migrate legacy set <parameter> <value>"
                (e.g.: "huskhomes migrate legacy set database foo")
                                
                STEP 3] HuskHomes will migrate data into the database
                tables configured in the config.yml file of this
                server. Please make sure you're happy with this
                before proceeding.
                                
                STEP 4] To start the migration, please run:
                "huskhomes migrate legacy start"
                """.replaceAll("%source_players_table%", sourcePlayerDataTable)
                .replaceAll("%source_locations_table%", sourceLocationsDataTable)
                .replaceAll("%source_homes_table%", sourceHomesDataTable)
                .replaceAll("%source_warps_table%", sourceWarpsDataTable)
                .replaceAll("%source_mysql_database%", sourceMySqlDatabase);
    }
}
