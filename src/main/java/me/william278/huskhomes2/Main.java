package me.william278.huskhomes2;

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

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Enabling HuskHomes version " + this.getDescription().getVersion());

        // Set instance for easy cross-class referencing
        setInstance(this);

        // Load the config
        configManager.loadConfig();

        // Set up data storage
        dataManager.setupStorage(settings.getStorageType());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled HuskHomes version " + this.getDescription().getVersion());
    }
}
