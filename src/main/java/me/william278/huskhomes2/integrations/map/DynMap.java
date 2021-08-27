package me.william278.huskhomes2.integrations.map;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class DynMap extends Map {

    private static Plugin dynMap;
    private static final HuskHomes plugin = HuskHomes.getInstance();
    private static final double MARKER_ICON_SCALE = 0.75;

    // Returns the marker icons from the plugin resources in the correct format and scaled to look good with Dynmap
    private InputStream getScaledMarkerIconStream(BufferedImage image) {
        try {
            BufferedImage scaledImage = new BufferedImage((int)(32 * MARKER_ICON_SCALE), (int)(32 * MARKER_ICON_SCALE), image.getType());
            AffineTransform scaleInstance = AffineTransform.getScaleInstance(MARKER_ICON_SCALE, MARKER_ICON_SCALE);
            AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);
            scaleOp.filter(image, scaledImage);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(scaledImage, "png", outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An exception occurred converting marker icon image data!", e);
            return null;
        }
    }

    @Override
    public void removeWarpMarker(String warpName) {
        String markerId = getWarpMarkerName(warpName);
        DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

        MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.warps");
        for (Marker m : markerSet.getMarkers()) {
            if (m.getMarkerID().equals(markerId)) {
                m.deleteMarker();
            }
        }
    }

    @Override
    public void removePublicHomeMarker(String homeName, String ownerName) {
        String markerId = getPublicHomeMarkerName(ownerName, homeName);
        DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

        MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("huskhomes.public_homes");
        for (Marker m : markerSet.getMarkers()) {
            if (m.getMarkerID().equals(markerId)) {
                m.deleteMarker();
            }
        }
    }

    @Override
    public void addWarpMarker(Warp warp) {
        try {
            String markerId = getWarpMarkerName(warp.getName());
            DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

            MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID);
            Marker m = markerSet.createMarker(markerId, warp.getName(), warp.getWorldName(), warp.getX(), warp.getY(), warp.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(WARP_MARKER_IMAGE_NAME), false);

            m.setDescription(getWarpInfoMenu(warp));
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding warp marker to the Dynamic Map (" + e.getCause() + ")");
            plugin.getLogger().warning("This may be because you reloaded the server instead of restarting!");
        }
    }

    @Override
    public void addPublicHomeMarker(Home home) {
        try {
            String markerId = getPublicHomeMarkerName(home.getOwnerUsername(), home.getName());
            DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

            MarkerSet markerSet = dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID);
            Marker m = markerSet.createMarker(markerId, home.getName(), home.getWorldName(), home.getX(), home.getY(), home.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME), false);

            m.setDescription(getPublicHomeInfoMenu(home));
        } catch (Exception e) {
            plugin.getLogger().severe("Error adding public home marker to the Dynamic Map (" + e.getCause() + ")");
            plugin.getLogger().warning("This may be because you reloaded the server instead of restarting!");
        }
    }

    @Override
    public void initialize() {
        dynMap = plugin.getServer().getPluginManager().getPlugin("dynmap");

        DynmapAPI dynmapAPI = (DynmapAPI) dynMap;

        if (dynmapAPI == null) {
            return;
        }

        dynmapAPI.getMarkerAPI().createMarkerIcon(WARP_MARKER_IMAGE_NAME, "Warp", getScaledMarkerIconStream(getWarpIcon()));
        dynmapAPI.getMarkerAPI().createMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME, "Public Home", getScaledMarkerIconStream(getPublicHomeIcon()));

        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (HuskHomes.getSettings().showPublicHomesOnMap()) {
                    dynmapAPI.getMarkerAPI().createMarkerSet(PUBLIC_HOMES_MARKER_SET_ID, HuskHomes.getSettings().getMapPublicHomeMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
                    for (Home home : DataManager.getPublicHomes(connection)) {
                        if (!HuskHomes.getSettings().doBungee() || home.getServer().equals(HuskHomes.getSettings().getServerID())) {
                            Bukkit.getScheduler().runTask(plugin, () -> addPublicHomeMarker(home));
                        }
                    }
                }
                if (HuskHomes.getSettings().showWarpsOnMap()) {
                    dynmapAPI.getMarkerAPI().createMarkerSet(WARPS_MARKER_SET_ID, HuskHomes.getSettings().getMapWarpMarkerSet(), dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
                    for (Warp warp : DataManager.getWarps(connection)) {
                        if (!HuskHomes.getSettings().doBungee() || warp.getServer().equals(HuskHomes.getSettings().getServerID())) {
                            Bukkit.getScheduler().runTask(plugin, () -> addWarpMarker(warp));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "An SQL exception occurred adding homes and warps to the DynMap");
            }
        });

    }

}
