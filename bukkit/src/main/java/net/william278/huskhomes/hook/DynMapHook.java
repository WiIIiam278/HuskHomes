package net.william278.huskhomes.hook;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        final CompletableFuture<Void> initializedFuture = new CompletableFuture<>();
        plugin.runAsync(() -> {
            final MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();
            if (plugin.getSettings().isPublicHomesOnMap()) {
                markerAPI.getMarkerIcons().stream()
                        .filter(markerIcon -> markerIcon.getMarkerIconID().equals(PUBLIC_HOME_MARKER_IMAGE_NAME))
                        .findFirst()
                        .ifPresent(markerIcon -> markerAPI.getMarkerIcons().remove(markerIcon));
                markerAPI.createMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME, "Public Home",
                        ((BukkitHuskHomes) plugin).getResource("markers/16x/" + PUBLIC_HOME_MARKER_IMAGE_NAME + ".png"));
                markerAPI.createMarkerSet(PUBLIC_HOMES_MARKER_SET_ID,
                        "Public Homes", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            }
            if (plugin.getSettings().isWarpsOnMap()) {
                markerAPI.getMarkerIcons().stream()
                        .filter(markerIcon -> markerIcon.getMarkerIconID().equals(WARP_MARKER_IMAGE_NAME))
                        .findFirst()
                        .ifPresent(markerIcon -> markerAPI.getMarkerIcons().remove(markerIcon));
                markerAPI.createMarkerIcon(WARP_MARKER_IMAGE_NAME, "Warp",
                        ((BukkitHuskHomes) plugin).getResource("markers/16x/" + WARP_MARKER_IMAGE_NAME + ".png"));
                markerAPI.createMarkerSet(WARPS_MARKER_SET_ID,
                        "Warps", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            }
            initializedFuture.complete(null);
        });
        return initializedFuture;
    }

    @Override
    public CompletableFuture<Void> updateHome(@NotNull Home home) {
        if (!isValidPosition(home)) return CompletableFuture.completedFuture(null);

        final CompletableFuture<Void> updatedFuture = new CompletableFuture<>();
        removeHome(home).thenRun(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).createMarker(
                            home.getOwner().getUuid() + ":" + home.getUuid(), home.getMeta().getName(), home.getWorld().getName(),
                            home.getX(), home.getY(), home.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME), false)
                    .setDescription(MarkerInformationPopup.create(home.getMeta().getName())
                            .setThumbnailMarker(PUBLIC_HOME_MARKER_IMAGE_NAME)
                            .addField("Owner", home.getOwner().getUsername())
                            .addField("Description", plugin.getLocales().wrapText(home.getMeta().getDescription()))
                            .addField("Command", "/" + BukkitCommand.Type.PUBLIC_HOME_COMMAND.getCommand().command + " " + home.getMeta().getName())
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
            dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).getMarkers().stream()
                    .filter(marker -> marker.getMarkerID().equals(home.getOwner().getUuid() + ":" + home.getUuid()))
                    .findFirst()
                    .ifPresent(Marker::deleteMarker);
            removedFuture.complete(null);
        });
        return removedFuture;
    }

    @Override
    public CompletableFuture<Void> clearHomes(@NotNull User user) {
        final CompletableFuture<Void> clearedFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).getMarkers().stream()
                    .filter(marker -> marker.getMarkerID().startsWith(user.getUuid().toString()))
                    .forEach(Marker::deleteMarker);
            clearedFuture.complete(null);
        });
        return clearedFuture;
    }

    @Override
    public CompletableFuture<Void> updateWarp(@NotNull Warp warp) {
        if (!isValidPosition(warp)) return CompletableFuture.completedFuture(null);

        final CompletableFuture<Void> updatedFuture = new CompletableFuture<>();
        removeWarp(warp).thenRun(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID).createMarker(
                            warp.getUuid().toString(), warp.getMeta().getName(), warp.getWorld().getName(),
                            warp.getX(), warp.getY(), warp.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(WARP_MARKER_IMAGE_NAME), false)
                    .setDescription(MarkerInformationPopup.create(warp.getMeta().getName())
                            .setThumbnailMarker(WARP_MARKER_IMAGE_NAME)
                            .addField("Description", plugin.getLocales().wrapText(warp.getMeta().getDescription()))
                            .addField("Command", "/" + BukkitCommand.Type.WARP_COMMAND.getCommand().command + " " + warp.getMeta().getName())
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
            dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID).getMarkers().stream()
                    .filter(marker -> marker.getMarkerID().equals(warp.getUuid().toString()))
                    .findFirst()
                    .ifPresent(Marker::deleteMarker);
            removedFuture.complete(null);
        }));
        return removedFuture;
    }

    @Override
    public CompletableFuture<Void> clearWarps() {
        final CompletableFuture<Void> clearedFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () -> {
            dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID).getMarkers()
                    .forEach(Marker::deleteMarker);
            clearedFuture.complete(null);
        });
        return clearedFuture;
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
