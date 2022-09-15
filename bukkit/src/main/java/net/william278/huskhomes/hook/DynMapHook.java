package net.william278.huskhomes.hook;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.command.BukkitCommandType;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.MarkerAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Hook to display warps and public homes on Dynmap
 */
public class DynMapHook extends MapHook {

    @NotNull
    private final DynmapAPI dynmapAPI;

    public DynMapHook(@NotNull HuskHomes implementor, @NotNull Plugin dynmapPlugin) {
        super(implementor, "Dynmap");
        this.dynmapAPI = (DynmapAPI) dynmapPlugin;
    }

    @Override
    protected CompletableFuture<Void> initializeMap() {
        return CompletableFuture.runAsync(() -> {
            final MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();
            if (plugin.getSettings().publicHomesOnMap) {
                markerAPI.getMarkerIcons().stream()
                        .filter(markerIcon -> markerIcon.getMarkerIconID().equals(PUBLIC_HOME_MARKER_IMAGE_NAME))
                        .findFirst()
                        .ifPresent(markerIcon -> markerAPI.getMarkerIcons().remove(markerIcon));
                markerAPI.createMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME, "Public Home",
                        ((BukkitHuskHomes) plugin).getResource("markers/16x/" + PUBLIC_HOME_MARKER_IMAGE_NAME + ".png"));
                markerAPI.createMarkerSet(PUBLIC_HOMES_MARKER_SET_ID,
                        "Public Homes", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            }
            if (plugin.getSettings().warpsOnMap) {
                markerAPI.getMarkerIcons().stream()
                        .filter(markerIcon -> markerIcon.getMarkerIconID().equals(WARP_MARKER_IMAGE_NAME))
                        .findFirst()
                        .ifPresent(markerIcon -> markerAPI.getMarkerIcons().remove(markerIcon));
                markerAPI.createMarkerIcon(WARP_MARKER_IMAGE_NAME, "Warp",
                        ((BukkitHuskHomes) plugin).getResource("markers/16x/" + WARP_MARKER_IMAGE_NAME + ".png"));
                markerAPI.createMarkerSet(WARPS_MARKER_SET_ID,
                        "Warps", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            }
        }).exceptionally(throwable -> {
            plugin.getLoggingAdapter().log(Level.SEVERE, "Failed to initialize Dynmap integration", throwable);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> updateHome(@NotNull Home home) {
        if (!isValidPosition(home)) return CompletableFuture.completedFuture(null);

        final CompletableFuture<Void> updatedFuture = new CompletableFuture<>();
        removeHome(home).thenRun(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).createMarker(
                            home.uuid.toString(), home.meta.name, home.world.name,
                            home.x, home.y, home.z, dynmapAPI.getMarkerAPI().getMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME), false)
                    .setDescription(MarkerInformationPopup.create(home.meta.name)
                            .setThumbnailMarker(PUBLIC_HOME_MARKER_IMAGE_NAME)
                            .addField("Owner", home.owner.username)
                            .addField("Description", plugin.getLocales().formatDescription(home.meta.description))
                            .addField("Command", "/" + BukkitCommandType.PUBLIC_HOME_COMMAND.commandBase.command + " " + home.meta.name)
                            .toHtml());
            updatedFuture.complete(null);
        }));
        return updatedFuture;
    }

    @Override
    public CompletableFuture<Void> removeHome(@NotNull Home home) {
        if (!isValidPosition(home)) return CompletableFuture.completedFuture(null);

        final CompletableFuture<Void> removedFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).getMarkers()
                    .stream()
                    .filter(marker -> marker.getMarkerID().equals(home.uuid.toString()))
                    .findFirst()
                    .ifPresent(GenericMarker::deleteMarker);
            removedFuture.complete(null);
        });
        return removedFuture;
    }

    @Override
    public CompletableFuture<Void> updateWarp(@NotNull Warp warp) {
        if (!isValidPosition(warp)) return CompletableFuture.completedFuture(null);

        final CompletableFuture<Void> updatedFuture = new CompletableFuture<>();
        removeWarp(warp).thenRun(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID).createMarker(
                            warp.uuid.toString(), warp.meta.name, warp.world.name,
                            warp.x, warp.y, warp.z, dynmapAPI.getMarkerAPI().getMarkerIcon(WARP_MARKER_IMAGE_NAME), false)
                    .setDescription(MarkerInformationPopup.create(warp.meta.name)
                            .setThumbnailMarker(WARP_MARKER_IMAGE_NAME)
                            .addField("Description", plugin.getLocales().formatDescription(warp.meta.description))
                            .addField("Command", "/" + BukkitCommandType.WARP_COMMAND.commandBase.command + " " + warp.meta.name)
                            .toHtml());
            updatedFuture.complete(null);
        }));
        return updatedFuture;
    }

    @Override
    public CompletableFuture<Void> removeWarp(@NotNull Warp warp) {
        if (!isValidPosition(warp)) return CompletableFuture.completedFuture(null);

        final CompletableFuture<Void> removedFuture = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID).getMarkers()
                    .stream()
                    .filter(marker -> marker.getMarkerID().equals(warp.uuid.toString()))
                    .findFirst()
                    .ifPresent(GenericMarker::deleteMarker);
            removedFuture.complete(null);
        }));
        return removedFuture;
    }

    /**
     * Creates an HTML Dynmap marker information popup widget
     */
    private static class MarkerInformationPopup {
        @NotNull
        private final String title;

        @Nullable
        private String thumbnailMarkerId;

        @NotNull
        private final Map<String, String> fields;

        private MarkerInformationPopup(@NotNull String title) {
            this.title = title;
            this.fields = new HashMap<>();
        }

        @NotNull
        private static DynMapHook.MarkerInformationPopup create(@NotNull String title) {
            return new MarkerInformationPopup(title);
        }

        @NotNull
        private DynMapHook.MarkerInformationPopup setThumbnailMarker(@NotNull String thumbnailMarkerId) {
            this.thumbnailMarkerId = thumbnailMarkerId;
            return this;
        }

        @NotNull
        private DynMapHook.MarkerInformationPopup addField(@NotNull String key, @NotNull String value) {
            fields.put(key, value);
            return this;
        }

        @NotNull
        private String toHtml() {
            final StringBuilder html = new StringBuilder();
            html.append("<div class=\"infowindow\">");
            if (thumbnailMarkerId != null) {
                html.append("<img src=\"/tiles/_markers_/")
                        .append(thumbnailMarkerId)
                        .append(".png\" class=\"thumbnail\"/>")
                        .append("&nbsp;");
            }
            html.append("<span style=\"font-weight: bold;\">")
                    .append(StringEscapeUtils.escapeHtml(title))
                    .append("</span><br/>");
            fields.forEach((key, value) -> html.append("<span style=\"font-weight: bold;\">")
                    .append(StringEscapeUtils.escapeHtml(key))
                    .append(": </span><span>")
                    .append(StringEscapeUtils.escapeHtml(value))
                    .append("</span><br/>"));
            return html.toString();
        }
    }

}
