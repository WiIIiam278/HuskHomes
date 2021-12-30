package me.william278.huskhomes2.integrations.map;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;

public class SquareMap extends Map {

    private final HashMap<String, SimpleLayerProvider> publicHomeProviders = new HashMap<>();
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
        final Key warpMarkerKey = Key.of(getWarpMarkerName(warpName));
        removeMarker(warpMarkerKey, warpProviders);
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
        final Key publicHomeMarkerKey = Key.of(getPublicHomeMarkerName(ownerName, homeName));
        removeMarker(publicHomeMarkerKey, publicHomeProviders);
    }

    @Override
    public void initialize() {
        Squaremap mapAPI = SquaremapProvider.get();
        plugin.getServer().getPluginManager().registerEvents(new SquareMapWorldLoadListener(), plugin);
        plugin.getLogger().info("Initializing Squaremap integration");

        final Key publicHomeMarkerIconKey = Key.of(PUBLIC_HOME_MARKER_IMAGE_NAME);
        final Key warpMarkerIconKey = Key.of(WARP_MARKER_IMAGE_NAME);

        // Make sure markers are unregistered, then register marker icon keys
        if (SquaremapProvider.get().iconRegistry().hasEntry(publicHomeMarkerIconKey)) {
            SquaremapProvider.get().iconRegistry().unregister(publicHomeMarkerIconKey);
        }
        SquaremapProvider.get().iconRegistry().register(publicHomeMarkerIconKey, getPublicHomeIcon());
        if (SquaremapProvider.get().iconRegistry().hasEntry(warpMarkerIconKey)) {
            SquaremapProvider.get().iconRegistry().unregister(warpMarkerIconKey);
        }
        SquaremapProvider.get().iconRegistry().register(warpMarkerIconKey, getWarpIcon());

        for (MapWorld world : mapAPI.mapWorlds()) {
            loadWorld(world);
        }

        // Populate map with markers
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = HuskHomes.getConnection()) {
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
                plugin.getLogger().log(Level.WARNING, "An SQL exception occurred adding homes and warps to the Squaremap");
            }
        });
    }

    // Removes a marker from the Squaremap Marker API
    private void removeMarker(Key markerKey, HashMap<String, SimpleLayerProvider> layerProviders) {
        String markerWorld = null;
        for (String worldName : layerProviders.keySet()) {
            SimpleLayerProvider warpProvider = layerProviders.get(worldName);
            if (warpProvider.hasMarker(markerKey)) {
                markerWorld = worldName;
                break;
            }
        }
        if (markerWorld != null) {
            layerProviders.get(markerWorld).removeMarker(markerKey);
        }
    }

    // Loads the world
    private void loadWorld(MapWorld world) {
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

    public class SquareMapWorldLoadListener implements Listener {

        @EventHandler
        public void onWorldLoad(WorldLoadEvent e) {
            Squaremap mapAPI = SquaremapProvider.get();
            mapAPI.getWorldIfEnabled(BukkitAdapter.worldIdentifier(e.getWorld())).ifPresent(SquareMap.this::loadWorld);

            // Populate map with markers
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = HuskHomes.getConnection()) {
                    if (HuskHomes.getSettings().showPublicHomesOnMap()) {
                        for (Home home : DataManager.getPublicHomes(connection)) {
                            if (home.getWorldName().equals(e.getWorld().getName())) {
                                if (!HuskHomes.getSettings().doBungee() || home.getServer().equals(HuskHomes.getSettings().getServerID())) {
                                    Bukkit.getScheduler().runTask(plugin, () -> addPublicHomeMarker(home));
                                }
                            }
                        }
                    }
                    if (HuskHomes.getSettings().showWarpsOnMap()) {
                        for (Warp warp : DataManager.getWarps(connection)) {
                            if (warp.getWorldName().equals(e.getWorld().getName())) {
                                if (!HuskHomes.getSettings().doBungee() || warp.getServer().equals(HuskHomes.getSettings().getServerID())) {
                                    Bukkit.getScheduler().runTask(plugin, () -> addWarpMarker(warp));
                                }
                            }
                        }
                    }
                } catch (SQLException exception) {
                    plugin.getLogger().log(Level.WARNING, "An SQL exception occurred adding homes and warps to the Squaremap");
                }
            });
        }

    }

}
