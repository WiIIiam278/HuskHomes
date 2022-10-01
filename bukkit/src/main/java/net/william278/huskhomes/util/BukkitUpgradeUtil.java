package net.william278.huskhomes.util;

import net.william278.annotaml.Annotaml;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.config.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Utility class for upgrading important stuff from HuskHomes v2.x config files to v3.x.
 */
public class BukkitUpgradeUtil {

    private final BukkitHuskHomes plugin;
    public Settings.DatabaseType databaseType;
    public String mySqlHost;
    public int mySqlPort;
    public String mySqlDatabase;
    public String mySqlUsername;
    public String mySqlPassword;
    public String mySqlParams;

    public String sourcePlayerDataTable;
    public String sourceLocationsDataTable;
    public String sourceHomesDataTable;
    public String sourceWarpsDataTable;

    private BukkitUpgradeUtil(@NotNull BukkitHuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Reads old settings from the config and writes them to the new config.
     */
    private void readOldSettings() {
        this.databaseType = Settings.DatabaseType.valueOf(plugin.getConfig()
                .getString("data_storage_options.storage_type", "SQLITE").toUpperCase());

        this.mySqlHost = plugin.getConfig().getString("data_storage_options.mysql_credentials.host", "localhost");
        this.mySqlPort = plugin.getConfig().getInt("data_storage_options.mysql_credentials.port", 3306);
        this.mySqlDatabase = plugin.getConfig().getString("data_storage_options.mysql_credentials.database", "HuskHomes");
        this.mySqlUsername = plugin.getConfig().getString("data_storage_options.mysql_credentials.username", "root");
        this.mySqlPassword = plugin.getConfig().getString("data_storage_options.mysql_credentials.password", "pa55w0rd");
        this.mySqlParams = plugin.getConfig().getString("data_storage_options.mysql_credentials.params", "?autoReconnect=true&useSSL=false");

        this.sourcePlayerDataTable = plugin.getConfig().getString("data_storage_options.table_names.player_data", "huskhomes_player_data");
        this.sourceLocationsDataTable = plugin.getConfig().getString("data_storage_options.table_names.locations_data", "huskhomes_location_data");
        this.sourceHomesDataTable = plugin.getConfig().getString("data_storage_options.table_names.homes_data", "huskhomes_home_data");
        this.sourceWarpsDataTable = plugin.getConfig().getString("data_storage_options.table_names.warps_data", "huskhomes_warp_data");
    }

    /**
     * Upgrade the config from an older version to the current version.
     *
     * @param settings The settings to upgrade
     */
    public void upgradeSettings(@NotNull Settings settings) {
        plugin.getLoggingAdapter().log(Level.WARNING, "Upgrading critical HuskHomes v2.x settings to v3.x.");
        settings.databaseType = databaseType;
        settings.mySqlHost = mySqlHost;
        settings.mySqlPort = mySqlPort;
        settings.mySqlDatabase = mySqlDatabase;
        settings.mySqlUsername = mySqlUsername;
        settings.mySqlPassword = mySqlPassword;
        settings.mySqlConnectionParameters = mySqlParams;
        try {
            Annotaml.create(new File(plugin.getDataFolder(), "config.yml"), settings);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to upgrade HuskHomes settings from a legacy version", e);
        }
    }

    @Nullable
    public static BukkitUpgradeUtil detect(@NotNull BukkitHuskHomes plugin) {
        if (!plugin.getConfig().contains("config_file_version")) {
            return null;
        }
        final BukkitUpgradeUtil upgrade = new BukkitUpgradeUtil(plugin);
        upgrade.readOldSettings();
        return upgrade;
    }

}
