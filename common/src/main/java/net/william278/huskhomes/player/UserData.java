package net.william278.huskhomes.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Extension of a {@link User} that contains persistently-stored additional user data
 * (home slots, teleport ignoring status & rtp cooldown)
 */
public class UserData extends User {

    /**
     * The number of slots this user has consumed (bought) in total, used for economy checks
     */
    public int homeSlots;

    /**
     * Whether this user is ignoring incoming /tpa and /tpahere requests
     */
    public boolean isIgnoringTeleports;

    /**
     * A timestamp indicating when the user can use the /rtp command next
     */
    public long rtpCooldown;

    public UserData(@NotNull UUID uuid, @NotNull String username,
                    int homeSlots, boolean isIgnoringTeleports, long rtpCooldown) {
        super(uuid, username);
        this.homeSlots = homeSlots;
        this.isIgnoringTeleports = isIgnoringTeleports;
        this.rtpCooldown = rtpCooldown;
    }

    public UserData(@NotNull User user,
                    int homeSlots, boolean isIgnoringTeleports, long rtpCooldown) {
        super(user.uuid, user.username);
        this.homeSlots = homeSlots;
        this.isIgnoringTeleports = isIgnoringTeleports;
        this.rtpCooldown = rtpCooldown;
    }

}
