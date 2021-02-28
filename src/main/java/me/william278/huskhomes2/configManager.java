package me.william278.huskhomes2;

import me.william278.huskhomes2.objects.Settings;

public class configManager {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // (Re-)Load the config file
    public static void loadConfig() {
        plugin.getConfig().options().header(
                " ------------------------------ \n" +
                        "|       HuskHomes Config       |\n" +
                        "|    Developed by William278   |\n" +
                        " ------------------------------ \n" +
                        "Configuration guide: https://github.com/WiIIiam278/HuskHomes2/wiki \n");
        plugin.getConfig().options().copyHeader(true);
        plugin.saveDefaultConfig();
        HuskHomes.settings = new Settings(plugin.getConfig());
    }
}
