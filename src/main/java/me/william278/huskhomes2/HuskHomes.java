package me.william278.huskhomes2;

import me.william278.huskhomes2.API.HuskHomesAPI;
import me.william278.huskhomes2.Commands.*;
import me.william278.huskhomes2.Commands.TabCompleters.*;
import me.william278.huskhomes2.Events.onPlayerDeath;
import me.william278.huskhomes2.Events.onPlayerJoin;
import me.william278.huskhomes2.Integrations.dynamicMap;
import me.william278.huskhomes2.Integrations.economy;
import me.william278.huskhomes2.Objects.Settings;
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
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", new pluginMessageHandler());
    }

    // Register tab completers
    private static void registerTabCompleters(HuskHomes plugin) {
        plugin.getCommand("home").setTabCompleter(new homeTabCompleter());
        plugin.getCommand("warp").setTabCompleter(new warpTabCompleter());
        plugin.getCommand("publichome").setTabCompleter(new publicHomeTabCompleter());
        plugin.getCommand("tpaccept").setTabCompleter(new emptyTabCompleter());
        plugin.getCommand("tpdeny").setTabCompleter(new emptyTabCompleter());
        plugin.getCommand("warplist").setTabCompleter(new emptyTabCompleter());
        plugin.getCommand("homelist").setTabCompleter(new emptyTabCompleter());
        plugin.getCommand("rtp").setTabCompleter(new emptyTabCompleter());
        plugin.getCommand("spawn").setTabCompleter(new emptyTabCompleter());
        plugin.getCommand("setspawn").setTabCompleter(new emptyTabCompleter());
        plugin.getCommand("edithome").setTabCompleter(new editHomeTabCompleter());
        plugin.getCommand("editwarp").setTabCompleter(new editWarpTabCompleter());

    }

    // Register commands
    private static void registerCommands(HuskHomes plugin) {
        plugin.getCommand("back").setExecutor(new backCommand());
        plugin.getCommand("delhome").setExecutor(new delHomeCommand());
        plugin.getCommand("delwarp").setExecutor(new delWarpCommand());
        plugin.getCommand("edithome").setExecutor(new editHomeCommand());
        plugin.getCommand("editwarp").setExecutor(new editWarpCommand());
        plugin.getCommand("home").setExecutor(new homeCommand());
        plugin.getCommand("homelist").setExecutor(new homeListCommand());
        plugin.getCommand("huskhomes").setExecutor(new huskHomesCommand());
        plugin.getCommand("publichome").setExecutor(new publicHomeCommand());
        plugin.getCommand("publichomelist").setExecutor(new publicHomeListCommand());
        plugin.getCommand("sethome").setExecutor(new setHomeCommand());
        plugin.getCommand("setwarp").setExecutor(new setWarpCommand());
        plugin.getCommand("tpaccept").setExecutor(new tpAcceptCommand());
        plugin.getCommand("tpdeny").setExecutor(new tpDenyCommand());
        plugin.getCommand("tpa").setExecutor(new tpaCommand());
        plugin.getCommand("tpahere").setExecutor(new tpaHereCommand());
        plugin.getCommand("tp").setExecutor(new tpCommand());
        plugin.getCommand("tphere").setExecutor(new tpHereCommand());
        plugin.getCommand("warp").setExecutor(new warpCommand());
        plugin.getCommand("warplist").setExecutor(new warpListCommand());
        plugin.getCommand("rtp").setExecutor(new rtpCommand());
        plugin.getCommand("spawn").setExecutor(new spawnCommand());
        plugin.getCommand("setspawn").setExecutor(new setSpawnCommand());
    }

    // Register events
    private static void registerEvents(HuskHomes plugin) {
        plugin.getServer().getPluginManager().registerEvents(new onPlayerJoin(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new onPlayerDeath(), plugin);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Enabling HuskHomes version " + this.getDescription().getVersion());

        // Set instance for easy cross-class referencing
        setInstance(this);

        // Check if HuskHomes is up-to-date
        getLogger().info(versionChecker.getVersionCheckString());

        // Load the config
        configManager.loadConfig();

        // Load the messages (in the right language)
        messageManager.loadMessages(HuskHomes.settings.getLanguage());

        // Fetch spawn location if set
        settingHandler.fetchSpawnLocation();

        // Set up data storage
        dataManager.setupStorage();

        // Setup dynmap if it is enabled
        if (HuskHomes.settings.doDynmap()) {
            dynamicMap.initializeDynmap();
        }

        // Setup economy if it is enabled
        if (HuskHomes.settings.doEconomy()) {
            economy.initializeEconomy();
        }

        // Return if the plugin is disabled
        if (!HuskHomes.getInstance().isEnabled()) {
            return;
        }

        // Set up bungee channels if bungee mode is enabled
        if (settings.doBungee()) {
            setupBungeeChannels(this);
        }

        // Register commands
        registerCommands(this);

        // Register events
        registerEvents(this);

        // Start Loop
        runEverySecond.startLoop();

        // bStats initialisation
        new metricsManager(this, 8430);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskHomes version " + this.getDescription().getVersion());
    }
}
