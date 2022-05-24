package net.william278.huskhomes.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents persistently stored {@link User} data about a {@link Player}; uuid and username
 * A {@link Player} may be retrieved from a user by the platform implementing classes
 */
public class User {

    public final UUID uuid;

    public final String username;

    /**
     * Get a user from a {@link UUID} and username
     *
     * @param uuid     Minecraft account {@link UUID} of the player
     * @param username Username of the player
     */
    public User(@NotNull UUID uuid, @NotNull String username) {
        this.uuid = uuid;
        this.username = username;
    }

    /**
     * Get a user from an online {@link Player}
     *
     * @param player the online {@link Player}
     */
    public User(Player player) {
        this.uuid = player.getUuid();
        this.username = player.getName();
    }
}
