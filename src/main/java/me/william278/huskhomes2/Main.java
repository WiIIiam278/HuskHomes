package me.william278.huskhomes2;

import me.william278.huskhomes2.Commands.delHomeCommand;
import me.william278.huskhomes2.Commands.homeCommand;
import me.william278.huskhomes2.Commands.homeListCommand;
import me.william278.huskhomes2.Commands.setHomeCommand;
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
        plugin.getCommand("home").setExecutor(new homeCommand());
        plugin.getCommand("sethome").setExecutor(new setHomeCommand());
        plugin.getCommand("delhome").setExecutor(new delHomeCommand());
        plugin.getCommand("homelist").setExecutor(new homeListCommand());
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
