package net.william278.huskhomes.hook;

import net.pl3x.map.Key;
import net.pl3x.map.Pl3xMap;
import net.pl3x.map.event.EventHandler;
import net.pl3x.map.event.EventListener;
import net.pl3x.map.event.server.ServerLoadedEvent;
import net.pl3x.map.event.world.WorldLoadedEvent;
import net.pl3x.map.event.world.WorldUnloadedEvent;
import net.pl3x.map.image.IconImage;
import net.pl3x.map.markers.Point;
import net.pl3x.map.markers.layer.SimpleLayer;
import net.pl3x.map.markers.marker.Icon;
import net.pl3x.map.markers.marker.Marker;
import net.pl3x.map.world.World;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Pl3xMapHook extends MapHook implements EventListener {

    private final ConcurrentLinkedQueue<Home> publicHomes = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Warp> warps = new ConcurrentLinkedQueue<>();
    private final Key warpsLayerKey;
    private final Key publicHomesLayerKey;

    public Pl3xMapHook(@NotNull HuskHomes plugin) {
        super(plugin, "Pl3xMap");
        this.warpsLayerKey = Key.of("warp_markers");
        this.publicHomesLayerKey = Key.of("public_home_markers");
    }

    @Override
    public void initialize() {
        Pl3xMap.api().getEventRegistry().register(this);

        if (plugin.getSettings().doWarpsOnMap()) {
            this.registerIcon(warpsLayerKey, "markers/16x/warp.png");
        }
        if (plugin.getSettings().doPublicHomesOnMap()) {
            this.registerIcon(publicHomesLayerKey, "markers/16x/public-home.png");
        }

        plugin.runAsync(() -> {
            plugin.getDatabase().getLocalPublicHomes(plugin).forEach(this::updateHome);
            plugin.getDatabase().getLocalWarps(plugin).forEach(this::updateWarp);
        });
    }

    @Override
    public void updateHome(@NotNull Home home) {
        publicHomes.remove(home);
        if (isValidPosition(home)) {
            publicHomes.add(home);
        }
    }

    @Override
    public void removeHome(@NotNull Home home) {
        publicHomes.remove(home);
    }

    @Override
    public void clearHomes(@NotNull User user) {
        publicHomes.removeIf(home -> home.getOwner().equals(user));
    }

    @Override
    public void updateWarp(@NotNull Warp warp) {
        warps.remove(warp);
        if (isValidPosition(warp)) {
            warps.add(warp);
        }
    }

    @Override
    public void removeWarp(@NotNull Warp warp) {
        warps.remove(warp);
    }

    @Override
    public void clearWarps() {
        warps.clear();
    }

    private void registerIcon(@NotNull Key key, @NotNull String iconFileName) {
        try (InputStream iconStream = plugin.getResource(iconFileName)) {
            if (iconStream == null) {
                plugin.log(Level.WARNING, "Failed to load Pl3xMap icon for warps: icon file not found");
                return;
            }
            Pl3xMap.api().getIconRegistry().register(new IconImage(key, ImageIO.read(iconStream), "png"));
        } catch (IOException e) {
            plugin.log(Level.WARNING, "Failed to load Pl3xMap icon for warps: " + e.getMessage());
        }
    }

    private void registerLayers(@NotNull World world) {
        if (plugin.getSettings().doWarpsOnMap()) {
            WarpsLayer layer = new WarpsLayer(this, world);
            world.getLayerRegistry().register(layer);
        }
        if (plugin.getSettings().doPublicHomesOnMap()) {
            PublicHomesLayer layer = new PublicHomesLayer(this, world);
            world.getLayerRegistry().register(layer);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onServerLoaded(ServerLoadedEvent event) {
        Pl3xMap.api().getWorldRegistry().entries().values().forEach(this::registerLayers);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onWorldLoaded(WorldLoadedEvent event) {
        registerLayers(event.getWorld());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onWorldUnloaded(WorldUnloadedEvent event) {
        event.getWorld().getLayerRegistry().unregister(warpsLayerKey);
        event.getWorld().getLayerRegistry().unregister(publicHomesLayerKey);
    }

    public static class WarpsLayer extends SimpleLayer {

        private final Pl3xMapHook hook;
        private final World mapWorld;

        public WarpsLayer(@NotNull Pl3xMapHook hook, @NotNull World mapWorld) {
            super(hook.warpsLayerKey, hook::getWarpsMarkerSetName);
            this.hook = hook;
            this.mapWorld = mapWorld;
        }

        @Override
        @NotNull
        public Collection<Marker<?>> getMarkers() {
            return hook.warps.stream()
                    .filter(warp -> warp.getWorld().getName().equals(mapWorld.getName()))
                    .map(warp -> Icon.of(
                            Key.of("warp_" + warp.getUuid()),
                            Point.of(warp.getX(), warp.getZ()),
                            hook.warpsLayerKey
                    ))
                    .collect(Collectors.toCollection(LinkedList::new));
        }

    }

    public static class PublicHomesLayer extends SimpleLayer {

        private final Pl3xMapHook hook;
        private final World mapWorld;

        public PublicHomesLayer(@NotNull Pl3xMapHook hook, @NotNull World mapWorld) {
            super(hook.publicHomesLayerKey, hook::getPublicHomesMarkerSetName);
            this.hook = hook;
            this.mapWorld = mapWorld;
        }

        @Override
        @NotNull
        public Collection<Marker<?>> getMarkers() {
            return hook.publicHomes.stream()
                    .filter(home -> home.getWorld().getName().equals(mapWorld.getName()))
                    .map(home -> Icon.of(
                            Key.of("public_home_" + home.getUuid()),
                            Point.of(home.getX(), home.getZ()),
                            hook.publicHomesLayerKey
                    ))
                    .collect(Collectors.toCollection(LinkedList::new));
        }

    }

}
