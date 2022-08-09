package net.william278.huskhomes.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents data about a player on the server
 */
public record UserData(@NotNull User user, int homeSlots, boolean ignoringTeleports, long rtpCooldown) {

    @NotNull
    public UUID getUserUuid() {
        return user.uuid;
    }

    public String getUsername() {
        return user.username;
    }

}
