package net.william278.huskhomes.position;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents a home set by a {@link User}
 */
public class Home extends SavedPosition {

    /**
     * The {@link User} who owns this home
     */
    @NotNull
    public final User owner;

    /**
     * {@code true} if this home is public
     */
    public boolean isPublic;

    public Home(double x, double y, double z, float yaw, float pitch,
                @NotNull World world, @NotNull Server server,
                @NotNull PositionMeta positionMeta, @NotNull UUID uuid,
                @NotNull User owner, boolean isPublic) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
        this.owner = owner;
        this.isPublic = isPublic;
    }

    public Home(@NotNull Position position, @NotNull PositionMeta meta, @NotNull User owner) {
        super(position, meta);
        this.owner = owner;
        this.isPublic = false;
    }

    @NotNull
    public List<MineDown> getHomeEditorWindow(@NotNull Locales locales, final boolean otherViewer,
                                              final boolean showServerInformation,
                                              final boolean showUseButtons, final boolean showManagementButtons) {
        return new ArrayList<>() {{
            if (!otherViewer) {
                locales.getLocale("edit_home_menu_title", meta.name)
                        .ifPresent(this::add);
            } else {
                locales.getLocale("edit_home_menu_title_other", owner.username, meta.name)
                        .ifPresent(this::add);
            }

            locales.getLocale("edit_home_menu_metadata_" + (!isPublic ? "private" : "public"),
                            DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm")
                                    .format(meta.creationTime.atZone(ZoneId.systemDefault())),
                            uuid.toString().split(Pattern.quote("-"))[0],
                            uuid.toString())
                    .ifPresent(this::add);

            if (meta.description.length() > 0) {
                locales.getLocale("edit_home_menu_description", MineDown.escape(meta.description))
                        .ifPresent(this::add);
            }

            if (!showServerInformation) {
                locales.getLocale("edit_home_menu_world", world.name).ifPresent(this::add);
            } else {
                locales.getLocale("edit_home_menu_world_server", world.name, server.name).ifPresent(this::add);
            }

            locales.getLocale("edit_home_menu_coordinates",
                            String.format("%.1f", x), String.format("%.1f", y), String.format("%.1f", z),
                            String.format("%.2f", yaw), String.format("%.2f", pitch))
                    .ifPresent(this::add);

            final String formattedName = owner.username + "." + meta.name;
            if (showUseButtons) {
                locales.getLocale("edit_home_menu_use_buttons",
                                formattedName)
                        .ifPresent(this::add);
            }
            if (showManagementButtons) {
                locales.getLocale("edit_home_menu_manage_buttons",
                                formattedName)
                        .ifPresent(this::add);
                locales.getLocale("edit_home_menu_meta_edit_buttons",
                                formattedName)
                        .ifPresent(this::add);
            }
        }};
    }

}
