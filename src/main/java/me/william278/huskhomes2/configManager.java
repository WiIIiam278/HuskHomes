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
                        "Configuration guide: https://github.com/WiIIiam278/HuskHomesDocs/wiki/Config-File/ \n" +
                        "To quickly setup the plugin:\n" +
                        "1. Fill in the details of your mySQL Database below\n" +
                        "2. (Optional) Change the appropriate details in the configuration\n" +
                        "3. Restart the server");
        plugin.getConfig().options().copyHeader(true).copyDefaults(true);
        plugin.saveConfig();
        Main.settings = new Settings(plugin.getConfig());
    }
}
