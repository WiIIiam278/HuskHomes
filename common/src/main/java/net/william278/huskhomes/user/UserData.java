package net.william278.huskhomes.user;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents data about a player on the server
 */
public record UserData(@NotNull User user, int homeSlots, boolean ignoringTeleports, @NotNull Instant rtpCooldown) {

    @NotNull
    public UUID getUserUuid() {
        return user.getUuid();
    }

    public String getUsername() {
        return user.getUsername();
    }

}
