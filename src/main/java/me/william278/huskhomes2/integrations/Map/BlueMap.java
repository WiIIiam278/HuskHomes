package me.william278.huskhomes2.integrations.Map;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.POIMarker;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.util.HashMap;

public class BlueMap extends Map {

    //todo finish

    private static final HuskHomes plugin = HuskHomes.getInstance();
    private static final HashMap<Warp,Boolean> queuedWarps = new HashMap<>();
    private static final HashMap<Warp,Boolean> queuedPublicHomes = new HashMap<>();

    @Override
    public void addWarpMarker(Warp warp) {
        World world = warp.getLocation().getWorld();
        if (world == null) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            BlueMapAPI.getInstance().ifPresentOrElse(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();
                    markerAPI.getMarkerSet(WARPS_MARKER_SET_ID).ifPresent(markerSet -> {
                        api.getWorld(world.getUID()).ifPresent(blueMapWorld -> {
                            String markerId = WARPS_MARKER_SET_ID + "." + warp.getName();
                            for (BlueMapMap map : blueMapWorld.getMaps()) {
                                POIMarker marker = markerSet.createPOIMarker(markerId, map, warp.getX(), warp.getY(), warp.getZ());
                                marker.setLabel(warp.getName());
                                //marker.setIcon();
                            }
                        });
                    });
                } catch (IOException ignored) { }
            }, () -> queuedWarps.put(warp,true));
        });
    }

    @Override
    public void removeWarpMarker(String warpName) {

    }

    @Override
    public void addPublicHomeMarker(Home home) {

    }

    @Override
    public void removePublicHomeMarker(String homeName, String ownerName) {

    }

    @Override
    public void initialize() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Create Marker Set
            BlueMapAPI.onEnable(api -> {
                try {
                    MarkerAPI markerAPI = api.getMarkerAPI();

                    MarkerSet publicHomeMarkerSet = markerAPI.getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).orElse(markerAPI.createMarkerSet(PUBLIC_HOMES_MARKER_SET_ID));
                    publicHomeMarkerSet.setLabel(HuskHomes.getSettings().getMapPublicHomeMarkerSet());

                    MarkerSet warpsMarkerSet = markerAPI.getMarkerSet(WARPS_MARKER_SET_ID).orElse(markerAPI.createMarkerSet(WARPS_MARKER_SET_ID));
                    warpsMarkerSet.setLabel(HuskHomes.getSettings().getMapWarpMarkerSet());

                    markerAPI.save();
                    plugin.getLogger().info("Enabled BlueMap integration!");

                    //executeQueuedOperations();
                } catch (IOException e) {
                    plugin.getLogger().warning("An exception occurred initialising BlueMap.");
                }
            });
        });
    }
}
