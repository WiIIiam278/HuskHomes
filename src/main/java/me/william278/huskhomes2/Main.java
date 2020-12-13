package me.william278.huskhomes2;

import me.william278.huskhomes2.Commands.*;
import me.william278.huskhomes2.Events.onPlayerDeath;
import me.william278.huskhomes2.Events.onPlayerJoin;
import me.william278.huskhomes2.Objects.Settings;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }
    private void setInstance(Main instance) {
        Main.instance = instance;
    }
    public static Settings settings;

    private static void setupBungeeChannels(Main plugin) {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", new pluginMessageHandler());
    }

    private static void registerCommands(Main plugin) {
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
    }

    private static void registerEvents(Main plugin) {
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
        messageManager.loadMessages(Main.settings.getLanguage());

        // Set up data storage
        dataManager.setupStorage(settings.getStorageType());

        // Set up bungee channels if bungee mode is enabled
        if (settings.doBungee()) {
            setupBungeeChannels(this);
        }

        // Register commands
        registerCommands(this);

        // Register events
        registerEvents(this);

        runEverySecond.startLoop();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskHomes version " + this.getDescription().getVersion());
    }
}
