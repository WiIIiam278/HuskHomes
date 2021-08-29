package me.william278.huskhomes2.integrations.map;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import net.pl3x.map.api.Key;
import net.pl3x.map.api.Pl3xMapProvider;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.SimpleLayerProvider;
import net.pl3x.map.api.marker.Icon;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;

public class Pl3xMap extends Map {

    private final HashMap<String,SimpleLayerProvider> publicHomeProviders = new HashMap<>();
    private final HashMap<String,SimpleLayerProvider> warpProviders = new HashMap<>();
    private final static int MARKER_SIZE = 20;
    private final static HuskHomes plugin = HuskHomes.getInstance();

    @Override
    public void addWarpMarker(Warp warp) {
        World world = Bukkit.getWorld(warp.getWorldName());
        if (world != null) {
            if (warpProviders.containsKey(world.getName())) {
                Icon marker = Marker.icon(
                        Point.of(warp.getX(), warp.getZ()),
                        Key.of(WARP_MARKER_IMAGE_NAME),
                        MARKER_SIZE);
                marker.markerOptions(MarkerOptions.builder()
                        .hoverTooltip(warp.getName())
                        .clickTooltip(getWarpInfoMenu(warp))
                        .build());
                warpProviders.get(world.getName()).addMarker(Key.of(getWarpMarkerName(warp.getName())), marker);
            }
        }
    }

    @Override
    public void removeWarpMarker(String warpName) {
        String warpMarkerWorld = null;
        final Key warpMarkerKey = Key.of(getWarpMarkerName(warpName));
        for (String worldName : warpProviders.keySet()) {
            SimpleLayerProvider warpProvider = warpProviders.get(worldName);
            if (warpProvider.hasMarker(warpMarkerKey)) {
                warpMarkerWorld = worldName;
                break;
            }
        }
        if (warpMarkerWorld != null) {
            warpProviders.get(warpMarkerWorld).removeMarker(warpMarkerKey);
        }
    }

    @Override
    public void addPublicHomeMarker(Home home) {
        World world = Bukkit.getWorld(home.getWorldName());
        if (world != null) {
            if (publicHomeProviders.containsKey(world.getName())) {
                Icon marker = Marker.icon(
                        Point.of(home.getX(), home.getZ()),
                        Key.of(PUBLIC_HOME_MARKER_IMAGE_NAME),
                        MARKER_SIZE);
                marker.markerOptions(MarkerOptions.builder()
                        .hoverTooltip(home.getName())
                        .clickTooltip(getPublicHomeInfoMenu(home))
                        .build());
                publicHomeProviders.get(world.getName()).addMarker(
                        Key.of(getPublicHomeMarkerName(home.getOwnerUsername(), home.getName())), marker);
            }
        }
    }

    @Override
    public void removePublicHomeMarker(String homeName, String ownerName) {
        String publicHomeMarkerWorld = null;
        final Key publicHomeMarkerKey = Key.of(getPublicHomeMarkerName(ownerName, homeName));
        for (String worldName : publicHomeProviders.keySet()) {
            SimpleLayerProvider warpProvider = publicHomeProviders.get(worldName);
            if (warpProvider.hasMarker(publicHomeMarkerKey)) {
                publicHomeMarkerWorld = worldName;
                break;
            }
        }
        if (publicHomeMarkerWorld != null) {
            publicHomeProviders.get(publicHomeMarkerWorld).removeMarker(publicHomeMarkerKey);
        }
    }

    @Override
    public void initialize() {
        net.pl3x.map.api.Pl3xMap mapAPI = Pl3xMapProvider.get();

        // Register map icons
        Pl3xMapProvider.get().iconRegistry().unregister(Key.of(PUBLIC_HOME_MARKER_IMAGE_NAME));
        Pl3xMapProvider.get().iconRegistry().register(Key.of(PUBLIC_HOME_MARKER_IMAGE_NAME), getPublicHomeIcon());
        Pl3xMapProvider.get().iconRegistry().unregister(Key.of(WARP_MARKER_IMAGE_NAME));
        Pl3xMapProvider.get().iconRegistry().register(Key.of(WARP_MARKER_IMAGE_NAME), getWarpIcon());

        for (net.pl3x.map.api.MapWorld world : mapAPI.mapWorlds()) {
            // Register public home map layer
            SimpleLayerProvider publicHomeProvider =
                    SimpleLayerProvider.builder(HuskHomes.getSettings().getMapPublicHomeMarkerSet())
                            .showControls(true)
                            .defaultHidden(false)
                            .layerPriority(6)
                            .zIndex(250)
                            .build();
            world.layerRegistry().register(Key.of(PUBLIC_HOMES_MARKER_SET_ID), publicHomeProvider);
            publicHomeProviders.put(world.name(), publicHomeProvider);

            // Register warps map layer
            SimpleLayerProvider warpProvider =
                    SimpleLayerProvider.builder(HuskHomes.getSettings().getMapWarpMarkerSet())
                            .showControls(true)
                            .defaultHidden(false)
                            .layerPriority(7)
                            .zIndex(250)
                            .build();
            world.layerRegistry().register(Key.of(WARPS_MARKER_SET_ID), warpProvider);
            warpProviders.put(world.name(), warpProvider);
        }

        // Populate map with markers
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (HuskHomes.getSettings().showPublicHomesOnMap()) {
                    for (Home home : DataManager.getPublicHomes(connection)) {
                        if (!HuskHomes.getSettings().doBungee() || home.getServer().equals(HuskHomes.getSettings().getServerID())) {
                            Bukkit.getScheduler().runTask(plugin, () -> addPublicHomeMarker(home));
                        }
                    }
                }
                if (HuskHomes.getSettings().showWarpsOnMap()) {
                    for (Warp warp : DataManager.getWarps(connection)) {
                        if (!HuskHomes.getSettings().doBungee() || warp.getServer().equals(HuskHomes.getSettings().getServerID())) {
                            Bukkit.getScheduler().runTask(plugin, () -> addWarpMarker(warp));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "An SQL exception occurred adding homes and warps to the Pl3xMap");
            }
        });
    }

}
