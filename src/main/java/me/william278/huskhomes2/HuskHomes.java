package me.william278.huskhomes2;

import me.william278.huskhomes2.api.HuskHomesAPI;
import me.william278.huskhomes2.commands.*;
import me.william278.huskhomes2.config.Settings;
import me.william278.huskhomes2.data.SQL.Database;
import me.william278.huskhomes2.data.SQL.MySQL;
import me.william278.huskhomes2.data.SQL.SQLite;
import me.william278.huskhomes2.integrations.map.BlueMap;
import me.william278.huskhomes2.integrations.map.DynMap;
import me.william278.huskhomes2.integrations.map.Map;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.listeners.PlayerListener;
import me.william278.huskhomes2.listeners.PluginMessageListener;
import me.william278.huskhomes2.migrators.UpgradeDatabase;
import me.william278.huskhomes2.teleport.SettingHandler;
import me.william278.huskhomes2.util.PlayerList;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.HashSet;
import java.util.UUID;
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

    // Map integration handling
    private static Map map;
    public static Map getMap() { return map; }

    // Settings data
    private static Settings settings;
    public static Settings getSettings() {
        return settings;
    }

    // Player list managing
    private static PlayerList playerList;
    public static PlayerList getPlayerList() { return playerList; }

    // Ignoring teleport requests handler
    private static final HashSet<UUID> ignoringTeleportRequests = new HashSet<>();
    public static boolean isIgnoringTeleportRequests(UUID uuid) { return ignoringTeleportRequests.contains(uuid); }
    public static void setIgnoringTeleportRequests(UUID uuid) {
        ignoringTeleportRequests.add(uuid);
    }
    public static void setNotIgnoringTeleportRequests(UUID uuid) {
        ignoringTeleportRequests.remove(uuid);
    }

    // Ignoring teleport requests handler
    private static final HashSet<UUID> teleportingPlayers = new HashSet<>();
    public static boolean isTeleporting(UUID uuid) { return teleportingPlayers.contains(uuid); }
    public static void setTeleporting(UUID uuid) {
        teleportingPlayers.add(uuid);
    }
    public static void setNotTeleporting(UUID uuid) {
        teleportingPlayers.remove(uuid);
    }

    // Metrics ID for bStats integration
    private static final int METRICS_PLUGIN_ID = 8430;

    /**
     * Returns the HuskHomes API
     * @deprecated Use {@link HuskHomesAPI#getInstance()} instead
     * @return an instance of the HuskHomes API
     * @see HuskHomesAPI
     */
    public HuskHomesAPI getAPI() {
        return HuskHomesAPI.getInstance();
    }


    public static void disablePlugin(String reason) {
        instance.getLogger().severe("Disabling HuskHomes plugin because:\n" + reason);
        Bukkit.getPluginManager().disablePlugin(instance);
    }

    // Initialise the database
    private void initializeDatabase() {
        String dataStorageType = HuskHomes.getSettings().getDatabaseType().toLowerCase();
        switch (dataStorageType) {
            case "mysql" -> {
                database = new MySQL(getInstance());
                database.load();
            }
            case "sqlite" -> {
                database = new SQLite(getInstance());
                database.load();
            }
            default -> {
                getLogger().log(Level.WARNING, "An invalid data storage type was specified in config.yml; defaulting to SQLite");
                database = new SQLite(getInstance());
                database.load();
            }
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
        new TpIgnoreCommand().register(getCommand("tpignore")).setTabCompleter(emptyTab);
        new WarpListCommand().register(getCommand("warplist")).setTabCompleter(emptyTab);
        new HomeListCommand().register(getCommand("homelist")).setTabCompleter(emptyTab);
        new PublicHomeListCommand().register(getCommand("publichomelist")).setTabCompleter(emptyTab);
        new RtpCommand().register(getCommand("rtp")).setTabCompleter(emptyTab);
        new SpawnCommand().register(getCommand("spawn")).setTabCompleter(emptyTab);
        new SetSpawnCommand().register(getCommand("setspawn")).setTabCompleter(emptyTab);
        new SetHomeCommand().register(getCommand("sethome")).setTabCompleter(emptyTab);
        new SetWarpCommand().register(getCommand("setwarp")).setTabCompleter(emptyTab);
        new BackCommand().register(getCommand("back")).setTabCompleter(emptyTab);
        new TpOfflineCommand().register(getCommand("tpoffline")).setTabCompleter(emptyTab);

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
        // Fetch config file
        saveDefaultConfig();
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

        // Setup the map integration with the correct plugin
        if (HuskHomes.getSettings().doMapIntegration()) {
            String mapPlugin = HuskHomes.getSettings().getMapPlugin();
            if (mapPlugin.equalsIgnoreCase("dynmap")) {
                map = new DynMap();
            } else if (mapPlugin.equalsIgnoreCase("bluemap")) {
                map = new BlueMap();
            }
            map.initialize();
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

        // Upgrade Database if needed
        UpgradeDatabase.upgradeDatabase();

        // bStats initialisation
        try {
            Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode", () -> Boolean.toString(getSettings().doBungee())));
            metrics.addCustomChart(new SimplePie("language", () -> getSettings().getLanguage()));
            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().getDatabaseType()));
            metrics.addCustomChart(new SimplePie("using_economy", () -> Boolean.toString(getSettings().doEconomy())));
            metrics.addCustomChart(new SimplePie("using_map", () -> Boolean.toString(getSettings().doMapIntegration())));
            if (getSettings().doMapIntegration()) {
                metrics.addCustomChart(new SimplePie("map_type", () -> getSettings().getMapPlugin()));
            }
        } catch (Exception e) {
            getLogger().warning("An exception occurred initialising metrics; skipping.");
        }

        // Setup player list
        playerList = new PlayerList();
        playerList.initialize();

        // Log a message
        getLogger().info("Enabled HuskHomes version " + this.getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Cancel remaining tasks
        Bukkit.getServer().getScheduler().cancelTasks(this);

        // Log a message
        getLogger().info("Disabled HuskHomes version " + this.getDescription().getVersion());
    }

    public static String getVersionCheckString() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=83767"); // Numbers = Spigot Project ID!
            URLConnection urlConnection = url.openConnection();
            String latestVersion = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
            String pluginVersion = instance.getDescription().getVersion();
            if (!latestVersion.equals(pluginVersion)) {
                if (pluginVersion.contains("dev")) {
                    return "You are running a development build of HuskHomes. The latest stable version is " + latestVersion + ".";
                } else {
                    return "An update for HuskHomes is available; v" + latestVersion + " (Currently running v" + pluginVersion + ")";
                }
            } else {
                return "HuskHomes is up to date! (Version " + pluginVersion + ")";
            }
        } catch (IOException e) {
            return "Error retrieving version information!";
        }
    }
}
