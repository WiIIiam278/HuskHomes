package me.william278.huskhomes2;

import me.william278.huskhomes2.api.HuskHomesAPI;
import me.william278.huskhomes2.commands.BackCommand;
import me.william278.huskhomes2.commands.CommandBase;
import me.william278.huskhomes2.commands.DelHomeCommand;
import me.william278.huskhomes2.commands.DelWarpCommand;
import me.william278.huskhomes2.commands.EditHomeCommand;
import me.william278.huskhomes2.commands.EditWarpCommand;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.commands.HomeListCommand;
import me.william278.huskhomes2.commands.HuskHomesCommand;
import me.william278.huskhomes2.commands.PublicHomeCommand;
import me.william278.huskhomes2.commands.PublicHomeListCommand;
import me.william278.huskhomes2.commands.RtpCommand;
import me.william278.huskhomes2.commands.SetHomeCommand;
import me.william278.huskhomes2.commands.SetSpawnCommand;
import me.william278.huskhomes2.commands.SetWarpCommand;
import me.william278.huskhomes2.commands.SpawnCommand;
import me.william278.huskhomes2.commands.TpCommand;
import me.william278.huskhomes2.commands.TpaCommand;
import me.william278.huskhomes2.commands.TpAcceptCommand;
import me.william278.huskhomes2.commands.TpaHereCommand;
import me.william278.huskhomes2.commands.TpDenyCommand;
import me.william278.huskhomes2.commands.TpHereCommand;
import me.william278.huskhomes2.commands.WarpCommand;
import me.william278.huskhomes2.commands.WarpListCommand;
import me.william278.huskhomes2.config.Settings;
import me.william278.huskhomes2.data.SQL.Database;
import me.william278.huskhomes2.data.SQL.MySQL;
import me.william278.huskhomes2.data.SQL.SQLite;
import me.william278.huskhomes2.integrations.DynMapIntegration;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.listeners.PlayerListener;
import me.william278.huskhomes2.listeners.PluginMessageListener;
import me.william278.huskhomes2.teleport.SettingHandler;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.logging.Level;

public final class HuskHomes extends JavaPlugin {

    // Instance getting
    private static HuskHomes instance;
    public static HuskHomes getInstance() {
        return instance;
    }

    // Database handling
    private static Database database;
    public static Connection getConnection() {
        return database.getConnection();
    }

    // Settings data
    private static Settings settings;
    public static Settings getSettings() {
        return settings;
    }


    /**
     * Returns the HuskHomes API
     * @return an instance of the HuskHomes API
     * @see HuskHomesAPI
     */
    public HuskHomesAPI getAPI() {
        return new HuskHomesAPI();
    }

    public static void disablePlugin(String reason) {
        instance.getLogger().severe("Disabling HuskHomes plugin because:\n" + reason);
        Bukkit.getPluginManager().disablePlugin(instance);
    }

    // Initialise the database
    private void initializeDatabase() {
        String dataStorageType = HuskHomes.getSettings().getDatabaseType().toLowerCase();
        switch (dataStorageType) {
            case "mysql":
                database = new MySQL(getInstance());
                database.load();
                break;
            case "sqlite":
                database = new SQLite(getInstance());
                database.load();
                break;
            default:
                getLogger().log(Level.WARNING, "An invalid data storage type was specified in config.yml; defaulting to SQLite");
                database = new SQLite(getInstance());
                database.load();
                break;
        }
    }

    private void setupBungeeChannels() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageListener());
    }

    private void registerCommands() {
        HomeCommand.Tab homeTab = new HomeCommand.Tab();
        new HomeCommand().register(getCommand("home")).setTabCompleter(homeTab);
        new DelHomeCommand().register(getCommand("delhome")).setTabCompleter(homeTab);

        WarpCommand.Tab warpTab = new WarpCommand.Tab();
        new WarpCommand().register(getCommand("warp")).setTabCompleter(warpTab);
        new DelWarpCommand().register(getCommand("delwarp")).setTabCompleter(warpTab);

        new PublicHomeCommand().register(getCommand("publichome"));
        new EditHomeCommand().register(getCommand("edithome"));
        new EditWarpCommand().register(getCommand("editwarp"));
        new HuskHomesCommand().register(getCommand("huskhomes"));

        TpCommand.Tab tpTab = new TpCommand.Tab();
        new TpCommand().register(getCommand("tp")).setTabCompleter(tpTab);
        new TpaCommand().register(getCommand("tpa")).setTabCompleter(tpTab);
        new TpHereCommand().register(getCommand("tphere")).setTabCompleter(tpTab);
        new TpaHereCommand().register(getCommand("tpahere")).setTabCompleter(tpTab);

        CommandBase.EmptyTab emptyTab = new CommandBase.EmptyTab();
        new TpAcceptCommand().register(getCommand("tpaccept")).setTabCompleter(emptyTab);
        new TpDenyCommand().register(getCommand("tpdeny")).setTabCompleter(emptyTab);
        new WarpListCommand().register(getCommand("warplist")).setTabCompleter(emptyTab);
        new HomeListCommand().register(getCommand("homelist")).setTabCompleter(emptyTab);
        new PublicHomeListCommand().register(getCommand("publichomelist")).setTabCompleter(emptyTab);
        new RtpCommand().register(getCommand("rtp")).setTabCompleter(emptyTab);
        new SpawnCommand().register(getCommand("spawn")).setTabCompleter(emptyTab);
        new SetSpawnCommand().register(getCommand("setspawn")).setTabCompleter(emptyTab);
        new SetHomeCommand().register(getCommand("sethome")).setTabCompleter(emptyTab);
        new SetWarpCommand().register(getCommand("setwarp")).setTabCompleter(emptyTab);
        new BackCommand().register(getCommand("back")).setTabCompleter(emptyTab);

        // Update caches
        PublicHomeCommand.updatePublicHomeTabCache();
        WarpCommand.Tab.updateWarpsTabCache();
    }

    // Register events
    private static void registerEvents(HuskHomes plugin) {
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
    }

    @Override
    public void onLoad() {
        // Set instance for easy cross-class referencing
        instance = this;
        settings = new Settings(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Plugin startup logic
        getLogger().info("Enabling HuskHomes version " + this.getDescription().getVersion());

        // Load the config
        settings.reload();

        // Load the messages (in the right language)
        MessageManager.loadMessages(HuskHomes.getSettings().getLanguage());

        // Check for updates (if enabled)
        if (HuskHomes.getSettings().doUpdateChecks()) {
            getLogger().info(getVersionCheckString());
        }

        // Initialize the database
        initializeDatabase();

        // Fetch spawn location if set
        SettingHandler.fetchSpawnLocation();

        // Setup the DynMap integration if it is enabled
        if (HuskHomes.getSettings().doDynMap()) {
            DynMapIntegration.initialize();
        }

        // Setup economy if it is enabled
        if (HuskHomes.getSettings().doEconomy()) {
            VaultIntegration.initializeEconomy();
        }

        // Set up bungee channels if bungee mode is enabled
        if (getSettings().doBungee()) {
            setupBungeeChannels();
        }

        // Register commands and tab completion handlers
        registerCommands();

        // Register events
        registerEvents(this);

        // bStats initialisation
        new MetricsLite(this, 8430);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskHomes version " + this.getDescription().getVersion());
    }

    public static String getVersionCheckString() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=83767"); // Numbers = Spigot Project ID!
            URLConnection urlConnection = url.openConnection();
            String latestVersion = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
            String pluginVersion = instance.getDescription().getVersion();
            if (!latestVersion.equals(pluginVersion)) {
                return "An update for HuskHomes is available; v" + latestVersion + " (Currently running v" + pluginVersion + ")";
            } else {
                return "HuskHomes is up to date! (Version " + pluginVersion + ")";
            }
        } catch (IOException e) {
            return "Error retrieving version information!";
        }
    }
}
