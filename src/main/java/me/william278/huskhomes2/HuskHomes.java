package me.william278.huskhomes2;

import me.william278.huskhomes2.api.HuskHomesAPI;
import me.william278.huskhomes2.commands.BackCommand;
import me.william278.huskhomes2.commands.CommandBase;
import me.william278.huskhomes2.commands.DelhomeCommand;
import me.william278.huskhomes2.commands.DelwarpCommand;
import me.william278.huskhomes2.commands.EdithomeCommand;
import me.william278.huskhomes2.commands.EditwarpCommand;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.commands.HomelistCommand;
import me.william278.huskhomes2.commands.HuskhomesCommand;
import me.william278.huskhomes2.commands.PublichomeCommand;
import me.william278.huskhomes2.commands.PublichomelistCommand;
import me.william278.huskhomes2.commands.RtpCommand;
import me.william278.huskhomes2.commands.SethomeCommand;
import me.william278.huskhomes2.commands.SetspawnCommand;
import me.william278.huskhomes2.commands.SetwarpCommand;
import me.william278.huskhomes2.commands.SpawnCommand;
import me.william278.huskhomes2.commands.TpCommand;
import me.william278.huskhomes2.commands.TpaCommand;
import me.william278.huskhomes2.commands.TpacceptCommand;
import me.william278.huskhomes2.commands.TpahereCommand;
import me.william278.huskhomes2.commands.TpdenyCommand;
import me.william278.huskhomes2.commands.TphereCommand;
import me.william278.huskhomes2.commands.WarpCommand;
import me.william278.huskhomes2.commands.WarplistCommand;
import me.william278.huskhomes2.config.Settings;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.DynMapIntegration;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.listeners.PlayerListener;
import me.william278.huskhomes2.migrators.LegacyMigrator;
import me.william278.huskhomes2.teleport.SettingHandler;
import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public final class HuskHomes extends JavaPlugin {

    private static HuskHomes instance;

    // TODO Remove
    public static HuskHomes getInstance() {
        return instance;
    }

    private static Settings settings;

    // TODO Remove
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

    private void setupBungeeChannels() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageHandler());
    }

    private void registerCommands() {
        HomeCommand.Tab homeTab = new HomeCommand.Tab();
        new HomeCommand().register(getCommand("home")).setTabCompleter(homeTab);
        new DelhomeCommand().register(getCommand("delhome")).setTabCompleter(homeTab);

        WarpCommand.Tab warpTab = new WarpCommand.Tab();
        new WarpCommand().register(getCommand("warp")).setTabCompleter(warpTab);
        new DelwarpCommand().register(getCommand("delwarp")).setTabCompleter(warpTab);

        new PublichomeCommand().register(getCommand("publichome"));
        new EdithomeCommand().register(getCommand("edithome"));
        new EditwarpCommand().register(getCommand("editwarp"));
        new HuskhomesCommand().register(getCommand("huskhomes"));

        TpCommand.Tab tpTab = new TpCommand.Tab();
        new TpCommand().register(getCommand("tp")).setTabCompleter(tpTab);
        new TpaCommand().register(getCommand("tpa")).setTabCompleter(tpTab);
        new TphereCommand().register(getCommand("tphere")).setTabCompleter(tpTab);
        new TpahereCommand().register(getCommand("tpahere")).setTabCompleter(tpTab);

        CommandBase.EmptyTab emptyTab = new CommandBase.EmptyTab();
        new TpacceptCommand().register(getCommand("tpaccept")).setTabCompleter(emptyTab);
        new TpdenyCommand().register(getCommand("tpdeny")).setTabCompleter(emptyTab);
        new WarplistCommand().register(getCommand("warplist")).setTabCompleter(emptyTab);
        new HomelistCommand().register(getCommand("homelist")).setTabCompleter(emptyTab);
        new PublichomelistCommand().register(getCommand("publichomelist")).setTabCompleter(emptyTab);
        new RtpCommand().register(getCommand("rtp")).setTabCompleter(emptyTab);
        new SpawnCommand().register(getCommand("spawn")).setTabCompleter(emptyTab);
        new SetspawnCommand().register(getCommand("setspawn")).setTabCompleter(emptyTab);
        new SethomeCommand().register(getCommand("sethome")).setTabCompleter(emptyTab);
        new SetwarpCommand().register(getCommand("setwarp")).setTabCompleter(emptyTab);
        new BackCommand().register(getCommand("back")).setTabCompleter(emptyTab);

        // Update caches
        PublichomeCommand.updatePublicHomeTabCache();
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
    public void onEnable() { //nb=
        saveDefaultConfig();

        // Plugin startup logic
        getLogger().info("Enabling HuskHomes version " + this.getDescription().getVersion());

        // MIGRATION: Check if a migration needs to occur
        LegacyMigrator.checkStartupMigration();

        // Load the config
        settings.reload();

        // MIGRATION: Migrate config files
        if (LegacyMigrator.isCanMigrate()) {
            new LegacyMigrator().migrateConfig();
        }

        // Load the messages (in the right language)
        MessageManager.loadMessages(HuskHomes.getSettings().getLanguage());

        // Check for updates (if enabled)
        if (HuskHomes.getSettings().doUpdateChecks()) {
            getLogger().info(getVersionCheckString());
        }

        // Fetch spawn location if set
        SettingHandler.fetchSpawnLocation();

        // Set up data storage
        DataManager.setupStorage();

        // MIGRATION: Migrate SQL data
        if (LegacyMigrator.isCanMigrate()) {
            new LegacyMigrator().migratePlayerData();
            new LegacyMigrator().migrateHomeData();
        }

        // Setup the Dynmap integration if it is enabled
        if (HuskHomes.getSettings().doDynmap()) {
            DynMapIntegration.initializeDynmap();
        }

        // Setup economy if it is enabled
        if (HuskHomes.getSettings().doEconomy()) {
            VaultIntegration.initializeEconomy();
        }

        // Set up bungee channels if bungee mode is enabled
        if (getSettings().doBungee()) {
            setupBungeeChannels();
        }

        // Register commands and their associated tab completers
        registerCommands();

        // Register events
        registerEvents(this);

        // Start Loop
        TeleportRequestHandler.startExpiredChecker(this);

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
