package net.william278.huskhomes.user;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a user who has data saved in the database
 */
public class User {

    @NotNull
    private final UUID uuid;

    @NotNull
    private final String username;

    /**
     * Get a user from a {@link UUID} and username
     *
     * @param uuid     Minecraft account {@link UUID} of the player
     * @param username Username of the player
     */
    protected User(@NotNull UUID uuid, @NotNull String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @NotNull
    public static User of(@NotNull UUID uuid, @NotNull String username) {
        return new User(uuid, username);
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (obj instanceof User user) {
            return user.getUuid().equals(getUuid());
        }
        return super.equals(obj);
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getUsername() {
        return username;
    }
}
