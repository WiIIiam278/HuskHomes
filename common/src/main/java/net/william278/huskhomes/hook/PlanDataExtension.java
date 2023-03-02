package net.william278.huskhomes.hook;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.player.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@PluginInfo(
        name = "HuskHomes",
        iconName = "home",
        iconFamily = Family.SOLID,
        color = Color.LIGHT_BLUE
)
@SuppressWarnings("unused")
public class PlanDataExtension implements DataExtension {

    private Database database;

    private boolean crossServer;

    private static final String UNKNOWN_STRING = "N/A";

    protected PlanDataExtension(@NotNull Database database, boolean crossServer) {
        this.database = database;
    }

    protected PlanDataExtension() {
    }

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_JOIN,
                CallEvents.PLAYER_LEAVE
        };
    }

    @BooleanProvider(
            text = "Has Data",
            description = "Whether this user has HuskHomes data.",
            iconName = "bell",
            iconFamily = Family.SOLID,
            conditionName = "hasData",
            hidden = true
    )
    public boolean getHasUserData(@NotNull UUID uuid) {
        return database.getUserData(uuid).isPresent();
    }

    @NumberProvider(
            text = "Home Count",
            description = "The number of homes this user has set.",
            iconName = "home",
            iconFamily = Family.SOLID,
            priority = 5
    )
    @Conditional("hasData")
    public long getHomeCount(@NotNull UUID uuid) {
        return database.getUserData(uuid)
                .map(userData -> (long) database.getHomes(userData.user()).size())
                .orElse(0L);
    }

    @NumberProvider(
            text = "Public Homes",
            description = "The number of homes this user has made public.",
            iconName = "sun",
            iconFamily = Family.SOLID,
            priority = 4
    )
    @Conditional("hasData")
    public long getPublicHomeCount(@NotNull UUID uuid) {
        return database.getUserData(uuid)
                .map(userData -> database.getHomes(userData.user())
                        .stream().filter(home -> home.isPublic()).count())
                .orElse(0L);
    }

    @NumberProvider(
            text = "Home Slots",
            description = "The number of extra home slots this user has purchased.",
            iconName = "money-check-alt",
            iconFamily = Family.SOLID,
            priority = 3
    )
    @Conditional("hasData")
    public long getPurchasedHomeSlots(@NotNull UUID uuid) {
        return database.getUserData(uuid)
                .map(userData -> (long) userData.homeSlots())
                .orElse(0L);
    }

    @BooleanProvider(
            text = "Ignoring /tpa Requests",
            description = "Whether this player is ignoring /tpa requests.",
            iconName = "phone-slash",
            iconFamily = Family.SOLID,
            priority = 2
    )
    @Conditional("hasData")
    public boolean isIgnoringTeleportRequests(@NotNull UUID uuid) {
        return database.getUserData(uuid)
                .map(UserData::ignoringTeleports)
                .orElse(false);
    }

    @StringProvider(
            text = "Offline Position",
            description = "The location where this user logged out.",
            iconName = "door-open",
            iconFamily = Family.SOLID,
            priority = 1
    )
    @Conditional("hasData")
    public String getOfflinePosition(@NotNull UUID uuid) {
        return database.getUserData(uuid)
                .map(userData -> database.getOfflinePosition(userData.user())
                        .map(position -> "x: " + (int) position.getX() + ", y: " + (int) position.getY() + ", z: " + (int) position.getZ()
                                + " (" + position.getWorld().getName() +
                                ((crossServer) ? "/" + position.getServer().getName() + ")" : ")"))
                        .orElse(UNKNOWN_STRING))
                .orElse(UNKNOWN_STRING);
    }

}
