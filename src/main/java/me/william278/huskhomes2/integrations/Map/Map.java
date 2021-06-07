package me.william278.huskhomes2.integrations.Map;

import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.Warp;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public abstract class Map {

    public Map() { }

    public abstract void addWarpMarker(Warp warp);
    public abstract void removeWarpMarker(String warpName);

    public abstract void addPublicHomeMarker(Home home);
    public abstract void removePublicHomeMarker(String homeName, String ownerName);

    public abstract void initialize();

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
