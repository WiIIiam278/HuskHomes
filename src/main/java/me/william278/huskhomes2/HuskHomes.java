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
import me.william278.huskhomes2.commands.SetspawnCommand;
import me.william278.huskhomes2.commands.SpawnCommand;
import me.william278.huskhomes2.commands.TpCommand;
import me.william278.huskhomes2.commands.TpaCommand;
import me.william278.huskhomes2.commands.TpacceptCommand;
import me.william278.huskhomes2.commands.TpahereCommand;
import me.william278.huskhomes2.commands.TpdenyCommand;
import me.william278.huskhomes2.commands.TphereCommand;
import me.william278.huskhomes2.commands.WarpCommand;
import me.william278.huskhomes2.commands.WarplistCommand;
import me.william278.huskhomes2.config.ConfigManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.DynMapIntegration;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.listeners.PlayerListener;
import me.william278.huskhomes2.migrators.LegacyMigrator;
import me.william278.huskhomes2.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuskHomes extends JavaPlugin {

    private static HuskHomes instance;
    public static HuskHomes getInstance() {
        return instance;
    }
    private void setInstance(HuskHomes instance) {
        HuskHomes.instance = instance;
    }
    public static Settings settings;

    /**
     * Returns the HuskHomes API
     * @return an instance of the HuskHomes API
     * @see HuskHomesAPI
     */
    public HuskHomesAPI getAPI() {
        return new HuskHomesAPI();
    }

    // Disable the plugin for the given reason
    public static void disablePlugin(String reason) {
        getInstance().getLogger().severe("Disabling HuskHomes plugin because:\n" + reason);
        Bukkit.getPluginManager().disablePlugin(getInstance());
    }

    // Initialise bungee plugin channels
    private static void setupBungeeChannels(HuskHomes plugin) {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", new PluginMessageHandler());
    }

    // Register tab completers
    private void registerCommands() {
        HomeCommand.Tab homeTab = new HomeCommand.Tab();
        new HomeCommand().register(getCommand("home")).setTabCompleter(homeTab);
        new DelhomeCommand().register(getCommand("delhome")).setTabCompleter(homeTab);

        WarpCommand.Tab warpTab = new WarpCommand.Tab();
        new WarpCommand().register(getCommand("warp")).setTabCompleter(warpTab);
        new DelwarpCommand().register(getCommand("delwarp")).setTabCompleter(warpTab);

        // TODO
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
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Enabling HuskHomes version " + this.getDescription().getVersion());

        // Set instance for easy cross-class referencing
        setInstance(this);

        // MIGRATION: Check if a migration needs to occur
        LegacyMigrator.checkStartupMigration();

        // Load the config
        ConfigManager.loadConfig();

        // MIGRATION: Migrate config files
        if (LegacyMigrator.startupMigrate) {
            new LegacyMigrator().migrateConfig();
        }

        // Load the messages (in the right language)
        MessageManager.loadMessages(HuskHomes.settings.getLanguage());

        // Check for updates (if enabled)
        if (HuskHomes.settings.doUpdateChecks()) {
            getLogger().info(VersionChecker.getVersionCheckString());
        }

        // Fetch spawn location if set
        SettingHandler.fetchSpawnLocation();

        // Set up data storage
        DataManager.setupStorage();

        // MIGRATION: Migrate SQL data
        if (LegacyMigrator.startupMigrate) {
            new LegacyMigrator().migratePlayerData();
            new LegacyMigrator().migrateHomeData();
        }

        // Setup the Dynmap integration if it is enabled
        if (HuskHomes.settings.doDynmap()) {
            DynMapIntegration.initializeDynmap();
        }

        // Setup economy if it is enabled
        if (HuskHomes.settings.doEconomy()) {
            VaultIntegration.initializeEconomy();
        }

        // Return if the plugin is disabled
        if (!HuskHomes.getInstance().isEnabled()) {
            return;
        }

        // Set up bungee channels if bungee mode is enabled
        if (settings.doBungee()) {
            setupBungeeChannels(this);
        }

        // Register commands and their associated tab completers
        registerCommands();

        // Register events
        registerEvents(this);

        // Start Loop
        RunEverySecond.startLoop();

        // bStats initialisation
        new MetricsManager(this, 8430);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskHomes version " + this.getDescription().getVersion());
    }
}
