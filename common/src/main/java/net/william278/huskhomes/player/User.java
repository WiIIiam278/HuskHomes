package net.william278.huskhomes.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents persistently stored {@link User} data about a {@link Player}; uuid and username
 * A {@link Player} may be retrieved from a user by the platform implementing classes
 *
 * @param uuid
 * @param username {@see {@link Player}} - an online player representation
 */
public record User(@NotNull UUID uuid, @NotNull String username) {

}
