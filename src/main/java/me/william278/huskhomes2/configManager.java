package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.Settings;

public class configManager {

    private static final Main plugin = Main.getInstance();

    // (Re-)Load the config file
    public static void loadConfig() {
        plugin.getConfig().options().header(
                " ------------------------------ \n" +
                        "|       HuskHomes Config       |\n" +
                        "|    Developed by William278   |\n" +
                        " ------------------------------ \n" +
                        "Configuration guide: [tbd] \n");
        plugin.getConfig().options().copyHeader(true);
        plugin.saveDefaultConfig();
        Main.settings = new Settings(plugin.getConfig());
    }
}
