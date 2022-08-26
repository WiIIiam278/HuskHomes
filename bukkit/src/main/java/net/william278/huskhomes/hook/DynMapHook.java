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
import org.jetbrains.annotations.NotNull;

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
        return CompletableFuture.runAsync(() -> {
            dynmapAPI.getMarkerAPI().createMarkerSet(PUBLIC_HOMES_MARKER_SET_ID,
                    "Public Homes", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);
            dynmapAPI.getMarkerAPI().createMarkerSet(WARPS_MARKER_SET_ID,
                    "Warps", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);

        });
    }

    @Override
    public void updateHome(@NotNull Home home) {
        if (!plugin.getSettings().publicHomesOnMap) return;
        if (plugin.getWorlds().stream().noneMatch(world -> world.uuid.equals(home.world.uuid))) return;

        removeHome(home);
        Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () ->
                dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).createMarker(
                                home.uuid.toString(), home.meta.name, home.world.name,
                                home.x, home.y, home.z, dynmapAPI.getMarkerAPI().getMarkerIcon("house"), false)
                        .setDescription(MarkerInformationPopup.create(home.meta.name)
                                .addField("Owner", home.owner.username)
                                .addField("Description", plugin.getLocales().formatDescription(home.meta.description))
                                .addField("Command", "/" + BukkitCommandType.PUBLIC_HOME_COMMAND.commandBase.command + " " + home.meta.name)
                                .toHtml()));
    }

    @Override
    public void removeHome(@NotNull Home home) {
        if (!plugin.getSettings().publicHomesOnMap) return;
        CompletableFuture.runAsync(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () ->
                dynmapAPI.getMarkerAPI().getMarkerSet(PUBLIC_HOMES_MARKER_SET_ID).getMarkers()
                        .stream()
                        .filter(marker -> marker.getMarkerID().equals(home.uuid.toString()))
                        .findFirst()
                        .ifPresent(GenericMarker::deleteMarker)));
    }

    @Override
    public void updateWarp(@NotNull Warp warp) {
        if (!plugin.getSettings().warpsOnMap) return;
        if (plugin.getWorlds().stream().noneMatch(world -> world.uuid.equals(warp.world.uuid))) return;

        removeWarp(warp);
        CompletableFuture.runAsync(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () ->
                dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID).createMarker(
                                warp.uuid.toString(), warp.meta.name, warp.world.name,
                                warp.x, warp.y, warp.z, dynmapAPI.getMarkerAPI().getMarkerIcon("blueflag"), false)
                        .setDescription(MarkerInformationPopup.create(warp.meta.name)
                                .addField("Description", plugin.getLocales().formatDescription(warp.meta.description))
                                .addField("Command", "/" + BukkitCommandType.WARP_COMMAND.commandBase.command + " " + warp.meta.name)
                                .toHtml())));
    }

    @Override
    public void removeWarp(@NotNull Warp warp) {
        if (!plugin.getSettings().warpsOnMap) return;
        CompletableFuture.runAsync(() -> Bukkit.getScheduler().runTask((BukkitHuskHomes) plugin, () ->
                dynmapAPI.getMarkerAPI().getMarkerSet(WARPS_MARKER_SET_ID).getMarkers()
                        .stream()
                        .filter(marker -> marker.getMarkerID().equals(warp.uuid.toString()))
                        .findFirst()
                        .ifPresent(GenericMarker::deleteMarker)));

    }

    /**
     * Creates an HTML Dynmap marker information popup widget
     */
    private static class MarkerInformationPopup {
        @NotNull
        private final String title;

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
        private DynMapHook.MarkerInformationPopup addField(@NotNull String key, @NotNull String value) {
            fields.put(key, value);
            return this;
        }

        @NotNull
        private String toHtml() {
            final StringBuilder html = new StringBuilder();
            html.append("<div class=\"infowindow\">")
                    .append("<span style=\"font-weight: bold;\">")
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
