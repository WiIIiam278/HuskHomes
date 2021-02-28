package me.william278.huskhomes2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VersionChecker {
    private static final HuskHomes plugin = HuskHomes.getInstance();

    public static String getVersionCheckString() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=83767"); // Numbers = Spigot Project ID!
            URLConnection urlConnection = url.openConnection();
            String latestVersion = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
            String pluginVersion = plugin.getDescription().getVersion();
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
