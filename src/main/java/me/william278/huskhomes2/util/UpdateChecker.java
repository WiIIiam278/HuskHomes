package me.william278.huskhomes2.util;
import me.william278.huskhomes2.HuskHomes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class UpdateChecker {

    private final static int SPIGOT_PROJECT_ID = 83767;
    private final String currentVersion;
    private String latestVersion;
    private final HuskHomes plugin;

    public UpdateChecker(HuskHomes plugin) {
        this.plugin = plugin;
        this.currentVersion = this.plugin.getDescription().getVersion();

        try {
            final URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOT_PROJECT_ID);
            URLConnection urlConnection = url.openConnection();
            this.latestVersion = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, "An IOException occurred when trying to check for updates.");
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "An exception occurred when trying to check for updates.");
        }
    }

    public boolean isUpToDate() {
        return latestVersion.equals(currentVersion);
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void logToConsole() {
        if (!isUpToDate()) {
            plugin.getLogger().log(Level.WARNING, "A new version of HuskHomes is available: Version "
                    + latestVersion + " (Currently running: " + currentVersion + ")");
        }
    }
}

