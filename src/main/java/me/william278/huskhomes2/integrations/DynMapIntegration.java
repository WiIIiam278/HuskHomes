package me.william278.huskhomes2.integrations;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class DynMapIntegration {

    private static Plugin dynMap;
    private static final HuskHomes plugin = HuskHomes.getInstance();

    public static void removeDynamicMapMarker(String warpName) {
        String markerId = "huskhomes." + warpName;
        DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

        MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.warps");
        for (Marker m : markerSet.getMarkers()) {
            if (m.getMarkerID().equals(markerId)) {
                m.deleteMarker();
            }
        }
    }

    public static void removeDynamicMapMarker(String homeName, String ownerName) {
        String markerId = "huskhomes." + ownerName + "." + homeName;
        DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

        MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.public_homes");
        for (Marker m : markerSet.getMarkers()) {
            if (m.getMarkerID().equals(markerId)) {
                m.deleteMarker();
            }
        }
    }

    public static void addDynamicMapMarker(Warp warp) {
        try {
            String markerId = "huskhomes." + warp.getName();
            DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

            MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.warps");
            Marker m = markerSet.createMarker(markerId, warp.getName(), warp.getWorldName(), warp.getX(), warp.getY(), warp.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(HuskHomes.getSettings().getDynmapWarpMarkerIconID()), false);
            String warpPopup = "<div class=\"infowindow\"><span style=\"font-weight:bold;\">/warp %WARP_NAME%</span><br/><span style=\"font-weight:bold;\">Description: </span>%DESCRIPTION%</div>";
            warpPopup = warpPopup.replace("%WARP_NAME%", escapeHtml(warp.getName()));
            warpPopup = warpPopup.replace("%DESCRIPTION%", escapeHtml(warp.getDescription()));
            m.setDescription(warpPopup);
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding warp marker to the Dynamic Map (" + e.getCause() + ")");
            plugin.getLogger().warning("This may be because you reloaded the server instead of restarting!");
        }
    }

    public static void addDynamicMapMarker(Home home) {
        try {
            String markerId = "huskhomes." + home.getOwnerUsername() + "." + home.getName();
            DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

            MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.public_homes");
            Marker m = markerSet.createMarker(markerId, home.getName(), home.getWorldName(), home.getX(), home.getY(), home.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(HuskHomes.getSettings().getDynmapPublicHomeMarkerIconID()), false);
            String publicHomePopup = "<div class=\"infowindow\"><span style=\"font-weight:bold;\">/phome %HOME_NAME%</span><br/><span style=\"font-weight:bold;\">Owner: </span>%OWNER%<br/><span style=\"font-weight:bold;\">Description: </span>%DESCRIPTION%</div>";
            publicHomePopup = publicHomePopup.replace("%HOME_NAME%", escapeHtml(home.getName()));
            publicHomePopup = publicHomePopup.replace("%OWNER%", escapeHtml(home.getOwnerUsername()));
            publicHomePopup = publicHomePopup.replace("%DESCRIPTION%", escapeHtml(home.getDescription()));
            m.setDescription(publicHomePopup);
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding public home marker to the Dynamic Map (" + e.getCause() + ")");
            plugin.getLogger().warning("This may be because you reloaded the server instead of restarting!");
        }
    }

    public static void initialize() {
        dynMap = plugin.getServer().getPluginManager().getPlugin("dynmap");

        DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

        if (dynmapAPI == null) {
            return;
        }

        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (HuskHomes.getSettings().showPublicHomesOnDynmap()) {
                    dynmapAPI.getMarkerAPI().createMarkerSet("huskhomes.public_homes", HuskHomes.getSettings().getDynmapPublicHomeMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
                    for (Home home : DataManager.getPublicHomes(connection)) {
                        if (!HuskHomes.getSettings().doBungee() || home.getServer().equals(HuskHomes.getSettings().getServerID())) {
                            Bukkit.getScheduler().runTask(plugin, () -> addDynamicMapMarker(home));
                        }
                    }
                }
                if (HuskHomes.getSettings().showWarpsOnDynmap()) {
                    dynmapAPI.getMarkerAPI().createMarkerSet("huskhomes.warps", HuskHomes.getSettings().getDynmapWarpMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
                    for (Warp warp : DataManager.getWarps(connection)) {
                        if (!HuskHomes.getSettings().doBungee() || warp.getServer().equals(HuskHomes.getSettings().getServerID())) {
                            Bukkit.getScheduler().runTask(plugin, () -> addDynamicMapMarker(warp));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "An SQL exception occurred adding homes and warps to the DynMap");
            }
        });

    }

}
