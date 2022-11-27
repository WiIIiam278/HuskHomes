package net.william278.huskhomes.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a user who has data saved in the database
 */
public class User {

    @NotNull
    public final UUID uuid;

    @NotNull
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

    @Override
    public boolean equals(@NotNull Object obj) {
        if (obj instanceof User user) {
            return user.uuid.equals(uuid);
        }
        return super.equals(obj);
    }
}
