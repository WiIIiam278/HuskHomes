package me.william278.huskhomes2.Integrations;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.Objects.Home;
import me.william278.huskhomes2.Objects.Warp;
import me.william278.huskhomes2.dataManager;
import org.bukkit.entity.Husk;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class dynamicMap {

    private static Plugin dynmap;
    private static final HuskHomes plugin = HuskHomes.getInstance();

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
            Marker m = markerSet.createMarker(markerId, warp.getName(), warp.getWorldName(), warp.getX(), warp.getY(), warp.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(HuskHomes.settings.getDynmapWarpMarkerIconID()), false);
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
            DynmapAPI dynmapAPI = (DynmapAPI) dynmap;

            MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.public_homes");
            Marker m = markerSet.createMarker(markerId, home.getName(), home.getWorldName(), home.getX(), home.getY(), home.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(HuskHomes.settings.getDynmapPublicHomeMarkerIconID()), false);
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

    public static void initializeDynmap() {
        dynmap = plugin.getServer().getPluginManager().getPlugin("dynmap");

        DynmapAPI dynmapAPI = (DynmapAPI) dynmap;
        if (HuskHomes.settings.showPublicHomesOnDynmap()) {
            dynmapAPI.getMarkerAPI().createMarkerSet("huskhomes.public_homes", HuskHomes.settings.getDynmapPublicHomeMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            for (Home home : dataManager.getPublicHomes()) {
                if (!HuskHomes.settings.doBungee() || home.getServer().equals(HuskHomes.settings.getServerID())) {
                    addDynamicMapMarker(home);
                }
            }
        }
        if (HuskHomes.settings.showWarpsOnDynmap()) {
            dynmapAPI.getMarkerAPI().createMarkerSet("huskhomes.warps", HuskHomes.settings.getDynmapWarpMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            for (Warp warp : dataManager.getWarps()) {
                if (!HuskHomes.settings.doBungee() || warp.getServer().equals(HuskHomes.settings.getServerID())) {
                    addDynamicMapMarker(warp);
                }
            }
        }

    }

}
