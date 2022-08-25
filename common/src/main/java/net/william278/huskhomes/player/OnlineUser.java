package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A cross-platform representation of a logged-in {@link User}
 */
public abstract class OnlineUser extends User {

    public OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }


    /**
     * Returns the current {@link Position} of this player
     *
     * @return the player's current {@link Position}
     */
    public abstract Position getPosition();

    /**
     * Returns the player's current bed or respawn anchor {@link Position}
     *
     * @return an optional with the player's current bed or respawn anchor {@link Position} if it has been set,
     * otherwise an {@link Optional#empty()}
     */
    public abstract Optional<Position> getBedSpawnPosition();

    /**
     * Returns the health of this player
     *
     * @return the player's health points
     */
    public abstract double getHealth();

    /**
     * Returns if the player has the permission node
     *
     * @param node The permission node string
     * @return {@code true} if the player has the node; {@code false} otherwise
     */
    public abstract boolean hasPermission(@NotNull String node);

    /**
     * Returns a {@link Map} of a player's permission nodes
     *
     * @return a {@link Map} of all permissions this player has to their set values
     */
    @NotNull
    public abstract Map<String, Boolean> getPermissions();

    /**
     * Dispatch a MineDown-formatted action bar message to this player
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public abstract void sendActionBar(@NotNull MineDown mineDown);


    /**
     * Dispatch a MineDown-formatted chat message to this player
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public abstract void sendMessage(@NotNull MineDown mineDown);

    /**
     * Dispatch a Minecraft translatable keyed-message to this player
     *
     * @param translationKey the translation key of the message to send
     */
    public abstract void sendMinecraftMessage(@NotNull String translationKey);

    /**
     * Play the specified sound to this player
     *
     * @param soundEffect the sound effect to play. If the sound name is invalid, the sound will not play
     */
    public abstract void playSound(@NotNull String soundEffect);

    /**
     * Teleport a player to the specified {@link Location}
     *
     * @param location     the {@link Location} to teleport the player to
     * @param asynchronous if the teleport should be asynchronous
     */
    public abstract CompletableFuture<TeleportResult> teleport(@NotNull Location location, boolean asynchronous);

    /**
     * Returns if a player is moving (i.e. they have momentum)
     *
     * @return {@code true} if the player is moving; {@code false} otherwise
     */
    public abstract boolean isMoving();

    /**
     * Returns if the player is tagged as being "vanished" by a /vanish plugin
     *
     * @return {@code true} if the player is tagged as being "vanished" by a /vanish plugin; {@code false} otherwise
     */
    public abstract boolean isVanished();

    /**
     * Get the maximum number of homes this user may set
     *
     * @param defaultMaxHomes the default maximum number of homes if the user has not set a custom value
     * @param stack           whether to stack numerical permissions that grant the user extra max homes
     * @return the maximum number of homes this user may set
     */
    public final int getMaxHomes(final int defaultMaxHomes, final boolean stack) {
        final List<Integer> homes = getNumericalPermissions("huskhomes.max_homes.");
        if (homes.isEmpty()) {
            return defaultMaxHomes;
        }
        if (stack) {
            return defaultMaxHomes + homes.stream().reduce(0, Integer::sum);
        } else {
            return homes.get(0);
        }
    }

    /**
     * Get the number of homes this user may make public
     *
     * @param defaultPublicHomes the default number of homes this user may make public
     * @param stack              whether to stack numerical permissions that grant the user extra public homes
     * @return the number of public home slots this user may set
     */
    public int getMaxPublicHomes(final int defaultPublicHomes, final boolean stack) {
        final List<Integer> homes = getNumericalPermissions("huskhomes.max_public_homes.");
        if (homes.isEmpty()) {
            return defaultPublicHomes;
        }
        if (stack) {
            return defaultPublicHomes + homes.stream().reduce(0, Integer::sum);
        } else {
            return homes.get(0);
        }
    }

    /**
     * Get the number of free home slots this user may set
     *
     * @param defaultFreeHomes the default number of free home slots to give this user
     * @param stack            whether to stack numerical permissions that grant the user extra free homes
     * @return the number of free home slots this user may set
     */
    public int getFreeHomes(final int defaultFreeHomes, final boolean stack) {
        final List<Integer> homes = getNumericalPermissions("huskhomes.free_homes.");
        if (homes.isEmpty()) {
            return defaultFreeHomes;
        }
        if (stack) {
            return defaultFreeHomes + homes.stream().reduce(0, Integer::sum);
        } else {
            return homes.get(0);
        }
    }

    /**
     * Gets a list of numbers from the prefixed permission nodes
     *
     * @param nodePrefix the prefix of the permission nodes to get
     * @return a list of numbers from the prefixed permission nodes, sorted by size
     */
    private List<Integer> getNumericalPermissions(@NotNull String nodePrefix) {
        return getPermissions().entrySet().stream()
                .filter(Map.Entry::getValue)
                .filter(permission -> permission.getKey().startsWith(nodePrefix))
                .filter(permission -> {
                    try {
                        // Remove node prefix from the permission and parse as an integer
                        Integer.parseInt(permission.getKey().substring(nodePrefix.length()));
                    } catch (final NumberFormatException e) {
                        return false;
                    }
                    return true;
                })
                .map(permission -> Integer.parseInt(permission.getKey().substring(nodePrefix.length())))
                .sorted().toList();
    }
}