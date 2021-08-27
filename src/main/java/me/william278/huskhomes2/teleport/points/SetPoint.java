package me.william278.huskhomes2.teleport.points;

import org.bukkit.Location;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class SetPoint extends TeleportationPoint {

    private String name;
    private String description;
    private final long creationTime;

    public SetPoint(String worldName, double x, double y, double z, float yaw, float pitch, String server, String name, String description, long creationTime) {
        super(worldName, x, y, z, yaw, pitch, server);
        this.name = name;
        this.description = description;
        this.creationTime = creationTime;
    }

    public SetPoint(Location location, String serverName, String name, String description) {
        super(location, serverName);
        this.name = name;
        this.description = description;
        this.creationTime = Instant.now().getEpochSecond();
    }

    /**
     * Returns the name of the point
     * @return the point name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the point
     * @return the point description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the name of the point
     * @param name the new point name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the description of the point
     * @param description the new point description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the creation time epoch seconds of the point
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Return a formatted string displaying the time this point was created
     * @return The formatted creation time string
     */
    public String getFormattedCreationTime() {
        if (creationTime == 0) {
            return "N/A";
        }
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(creationTime));
    }
}
