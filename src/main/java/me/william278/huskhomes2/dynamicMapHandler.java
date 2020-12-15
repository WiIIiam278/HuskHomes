package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.Warp;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;

public class dynamicMapHandler {

    private static Plugin dynmap;
    private static final Main plugin = Main.getInstance();

    public static void removeDynamicMapMarker(String warpName) {
        String markerId = "huskhomes." + warpName;
        DynmapAPI dynmapAPI = (DynmapAPI) dynmap;

        MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.warps");
        for (Marker m : markerSet.getMarkers()) {
            if (m.getMarkerID().equals(markerId)) {
                m.deleteMarker();
            }
        }
    }

    public static void removeDynamicMapMarker(String homeName, String ownerName) {
        String markerId = "huskhomes." + ownerName + "." + homeName;
        DynmapAPI dynmapAPI = (DynmapAPI) dynmap;

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
            DynmapAPI dynmapAPI = (DynmapAPI) dynmap;

            MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.warps");
            Marker m = markerSet.createMarker(markerId, warp.getName(), warp.getWorldName(), warp.getX(), warp.getY(), warp.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(Main.settings.getDynmapWarpMarkerIconID()), false);
            String popupWindow = "<div class=\"infowindow\"><span style=\"font-weight:bold;\">%WARP_NAME%</span><br/><span style=\"font-weight:bold;\">Description: </span>%DESCRIPTION%</div>";
            popupWindow = popupWindow.replace("%WARP_NAME%", warp.getName());
            popupWindow = popupWindow.replace("%DESCRIPTION%", warp.getDescription());
            m.setDescription(popupWindow);
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding warp marker to the Dynamic Map (" + e.getCause() + ")");
            plugin.getLogger().warning("This may be because you reloaded the server instead of restarting!");
        }
    }

    public static void addDynamicMapMarker(Home home) {
        try {
            String markerId = "huskhomes." + home.getOwnerUsername() + "." + home.getName();
            DynmapAPI dynmapAPI = (DynmapAPI) dynmap;

            MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.public_homes");
            Marker m = markerSet.createMarker(markerId, home.getName(), home.getWorldName(), home.getX(), home.getY(), home.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(Main.settings.getDynmapPublicHomeMarkerIconID()), false);
            String popupWindow = "<div class=\"infowindow\"><span style=\"font-weight:bold;\">%HOME_NAME%</span><br/><span style=\"font-weight:bold;\">Owner: </span>%OWNER%<br/><span style=\"font-weight:bold;\">Description: </span>%DESCRIPTION%</div>";
            popupWindow = popupWindow.replace("%HOME_NAME%", home.getName());
            popupWindow = popupWindow.replace("%OWNER%", home.getOwnerUsername());
            popupWindow = popupWindow.replace("%DESCRIPTION%", home.getDescription());
            m.setDescription(popupWindow);
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding public home marker to the Dynamic Map (" + e.getCause() + ")");
            plugin.getLogger().warning("This may be because you reloaded the server instead of restarting!");
        }
    }

    public static void initializeDynmap() {
        dynmap = plugin.getServer().getPluginManager().getPlugin("dynmap");

        DynmapAPI dynmapAPI = (DynmapAPI) dynmap;
        if (Main.settings.isDynmapPublicHomes()) {
            dynmapAPI.getMarkerAPI().createMarkerSet("huskhomes.public_homes", Main.settings.getDynmapPublicHomeMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            for (Home home : dataManager.getPublicHomes()) {
                addDynamicMapMarker(home);
            }
        }
        if (Main.settings.isDynmapWarps()) {
            dynmapAPI.getMarkerAPI().createMarkerSet("huskhomes.warps", Main.settings.getDynmapWarpMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            for (Warp warp : dataManager.getWarps()) {
                addDynamicMapMarker(warp);
            }
        }

    }

}
