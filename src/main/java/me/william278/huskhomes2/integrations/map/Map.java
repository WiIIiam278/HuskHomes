package me.william278.huskhomes2.integrations.map;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public abstract class Map {

    public Map() {
    }

    private static final HuskHomes plugin = HuskHomes.getInstance();

    public static final String PUBLIC_HOMES_MARKER_SET_ID = "huskhomes.public_homes";
    public static final String WARPS_MARKER_SET_ID = "huskhomes.warps";

    public static final String PUBLIC_HOME_MARKER_IMAGE_NAME = "public_home";
    public static final String WARP_MARKER_IMAGE_NAME = "warp";

    public BufferedImage getWarpIcon() {
        return getMarkerIcon(WARP_MARKER_IMAGE_NAME);
    }

    public BufferedImage getPublicHomeIcon() {
        return getMarkerIcon(PUBLIC_HOME_MARKER_IMAGE_NAME);
    }

    private BufferedImage getMarkerIcon(String name) {
        final String filePath = "marker-icons" + File.separator + name + ".png";
        File file = new File(plugin.getDataFolder(), filePath);
        if (!file.exists()) {
            plugin.saveResource(filePath, false);
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + name + " marker icon image!", e);
        }
        return image;
    }

    public abstract void addWarpMarker(Warp warp);

    public abstract void removeWarpMarker(String warpName);

    public abstract void addPublicHomeMarker(Home home);

    public abstract void removePublicHomeMarker(String homeName, String ownerName);

    public abstract void initialize();

    public String getPublicHomeMarkerName(String ownerName, String homeName) {
        return PUBLIC_HOMES_MARKER_SET_ID + "." + ownerName + "." + homeName;
    }

    public String getWarpMarkerName(String warpName) {
        return WARPS_MARKER_SET_ID + "." + warpName;
    }

    public String getWarpInfoMenu(Warp warp) {
        String warpPopup = "<div class=\"infowindow\"><span style=\"font-weight:bold;\">/warp %WARP_NAME%</span><br/><span style=\"font-weight:bold;\">Description: </span>%DESCRIPTION%</div>";
        warpPopup = warpPopup.replace("%WARP_NAME%", escapeHtml(warp.getName()));
        warpPopup = warpPopup.replace("%DESCRIPTION%", escapeHtml(warp.getDescription()));
        return warpPopup;
    }

    public String getPublicHomeInfoMenu(Home home) {
        String publicHomePopup = "<div class=\"infowindow\"><span style=\"font-weight:bold;\">/phome %HOME_NAME%</span><br/><span style=\"font-weight:bold;\">Owner: </span>%OWNER%<br/><span style=\"font-weight:bold;\">Description: </span>%DESCRIPTION%</div>";
        publicHomePopup = publicHomePopup.replace("%HOME_NAME%", escapeHtml(home.getName()));
        publicHomePopup = publicHomePopup.replace("%OWNER%", escapeHtml(home.getOwnerUsername()));
        publicHomePopup = publicHomePopup.replace("%DESCRIPTION%", escapeHtml(home.getDescription()));
        return publicHomePopup;
    }

}
