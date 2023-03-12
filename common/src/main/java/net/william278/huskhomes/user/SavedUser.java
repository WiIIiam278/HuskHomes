package net.william278.huskhomes.user;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents data about a player on the server
 */
public class SavedUser {

    private final User user;

    private int homeSlots;
    private boolean ignoringTeleports;
    private Instant rtpCooldown;

    public SavedUser(@NotNull User user, int homeSlots, boolean ignoringTeleports, @NotNull Instant rtpCooldown) {
        this.user = user;
        this.homeSlots = homeSlots;
        this.ignoringTeleports = ignoringTeleports;
        this.rtpCooldown = rtpCooldown;
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public UUID getUserUuid() {
        return user.getUuid();
    }

    @NotNull
    public String getUsername() {
        return user.getUsername();
    }

    public int getHomeSlots() {
        return homeSlots;
    }

    public void setHomeSlots(int homeSlots) {
        this.homeSlots = homeSlots;
    }

    public boolean isIgnoringTeleports() {
        return ignoringTeleports;
    }

    public void setIgnoringTeleports(boolean ignoringTeleports) {
        this.ignoringTeleports = ignoringTeleports;
    }

    @NotNull
    public Instant getRtpCooldown() {
        return rtpCooldown;
    }

    public void setRtpCooldown(@NotNull Instant rtpCooldown) {
        this.rtpCooldown = rtpCooldown;
    }

}
