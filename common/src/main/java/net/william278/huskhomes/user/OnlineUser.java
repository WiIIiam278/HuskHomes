/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.user;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.hook.LuckPermsHook;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.teleport.TeleportationException;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A cross-platform representation of a logged-in {@link User}.
 */
public abstract class OnlineUser extends User implements Teleportable, CommandUser {

    protected final HuskHomes plugin;
    protected boolean markedAsInvulnerable = false;

    protected OnlineUser(@NotNull UUID uuid, @NotNull String username, @NotNull HuskHomes plugin) {
        super(uuid, username);
        this.plugin = plugin;
    }

    /**
     * Returns the current {@link Position} of this player.
     *
     * @return the player's current {@link Position}
     */
    public abstract Position getPosition();

    /**
     * Returns the player's current bed or respawn anchor {@link Position}.
     *
     * @return the player's current respawn {@link Position} if it has been set, or {@link Optional#empty()}
     */
    public abstract Optional<Position> getBedSpawnPosition();

    /**
     * Returns the health of this player.
     *
     * @return the player's health points
     */
    public abstract double getHealth();

    /**
     * Returns if the player has the permission node.
     *
     * @param node The permission node string
     * @return {@code true} if the player has the node; {@code false} otherwise
     */
    public abstract boolean hasPermission(@NotNull String node);

    /**
     * Returns a {@link Map} of a player's permission nodes.
     *
     * @return a {@link Map} of all permissions this player has to their set values
     */
    @NotNull
    public abstract Map<String, Boolean> getPermissions();

    /**
     * Dispatch a MineDown-formatted title or subtitle to the player.
     *
     * @param mineDown the parsed {@link MineDown} to send
     * @param subTitle whether to send the title as a subtitle ({@code true} for a subtitle, {@code false} for a title)
     */
    public void sendTitle(@NotNull MineDown mineDown, boolean subTitle) {
        final Component message = mineDown.toComponent();
        getAudience().showTitle(Title.title(
                subTitle ? Component.empty() : message,
                subTitle ? message : Component.empty()
        ));
    }

    /**
     * Dispatch a MineDown-formatted action bar message to this player.
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public void sendActionBar(@NotNull MineDown mineDown) {
        getAudience().sendActionBar(mineDown.toComponent());
    }


    /**
     * Dispatch a MineDown-formatted chat message to this player.
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public void sendMessage(@NotNull MineDown mineDown) {
        System.out.println("SEND MESSAGE!" + mineDown.toComponent().toString());
        getAudience().sendMessage(mineDown.toComponent());
    }

    /**
     * Dispatch a MineDown-formatted message to this player.
     *
     * @param mineDown the parsed {@link MineDown} to send
     * @param slot     the {@link Locales.DisplaySlot} to send the message to
     */
    public void sendMessage(@NotNull MineDown mineDown, @NotNull Locales.DisplaySlot slot) {
        switch (slot) {
            case ACTION_BAR -> sendActionBar(mineDown);
            case TITLE -> sendTitle(mineDown, false);
            case SUBTITLE -> sendTitle(mineDown, true);
            default -> sendMessage(mineDown);
        }
    }

    /**
     * Dispatch a Minecraft translatable keyed-message to this player.
     *
     * <p>This method is intended for use with Minecraft's built-in translation keys.
     * If the key is invalid, it will be substituted with {@code minecraft:block.minecraft.spawn.not_valid}
     *
     * @param translationKey the translation key of the message to send
     */
    public void sendTranslatableMessage(@Subst(Key.MINECRAFT_NAMESPACE + "block.minecraft.spawn.not_valid")
                                        @NotNull String translationKey) {
        getAudience().sendMessage(Component.translatable(translationKey));
    }

    /**
     * Play the specified sound to this player.
     *
     * @param soundEffect the sound effect to play. If the sound name is invalid, the sound will not play
     * @implNote If the key is invalid, it will be substituted with {@code minecraft:block.note_block.banjo}
     */
    public void playSound(@Subst(Key.MINECRAFT_NAMESPACE + ":block.note_block.banjo")
                          @NotNull String soundEffect) throws IllegalArgumentException {
        try {
            getAudience().playSound(
                    Sound.sound(Key.key(soundEffect), Sound.Source.PLAYER, 1.0f, 1.0f),
                    Sound.Emitter.self()
            );
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid sound effect name: " + soundEffect);
        }
    }

    /**
     * Get the adventure {@link Audience} for this player.
     *
     * @return the adventure {@link Audience} for this player
     */
    @NotNull
    public Audience getAudience() {
        System.out.println("TEST AUDIENCE GET OL");
        return plugin.getAudience(getUuid());
    }

    /**
     * Dismount the player, readying them for teleportation.
     *
     * @return a {@link CompletableFuture} that completes when the player has dismounted
     */
    public abstract CompletableFuture<Void> dismount();

    /**
     * Teleport a player to the specified local {@link Location}.
     *
     * @param location the {@link Location} to teleport the player to
     * @param async    if the teleport should be asynchronous
     * @throws TeleportationException if the teleport fails
     */
    public abstract void teleportLocally(@NotNull Location location, boolean async) throws TeleportationException;

    /**
     * Send a plugin message to the user on the bungee channel.
     *
     * @param message byte array of message data
     */
    public abstract void sendPluginMessage(byte[] message);

    /**
     * Returns if a player is moving (i.e., they have momentum).
     *
     * @return {@code true} if the player is moving; {@code false} otherwise
     */
    public abstract boolean isMoving();

    /**
     * Returns if the player is tagged as being "vanished" by a /vanish plugin.
     *
     * @return {@code true} if the player is tagged as being "vanished" by a /vanish plugin; {@code false} otherwise
     */
    public abstract boolean isVanished();

    @ApiStatus.Internal
    public abstract boolean hasInvulnerability();

    @ApiStatus.Internal
    public abstract void handleInvulnerability();

    @ApiStatus.Internal
    public abstract void removeInvulnerabilityIfPermitted();

    /**
     * Get the maximum number of homes this user may set.
     *
     * @param defaultMaxHomes the default maximum number of homes if the user has not set a custom value
     * @param stack           whether to stack numerical permissions that grant the user extra max homes
     * @return the maximum number of homes this user may set
     */
    public final int getMaxHomes(int defaultMaxHomes, boolean stack) {
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
     * Get the number of homes this user may make public.
     *
     * @param defaultPublicHomes the default number of homes this user may make public
     * @param stack              whether to stack numerical permissions that grant the user extra public homes
     * @return the number of public home slots this user may set
     */
    public int getMaxPublicHomes(int defaultPublicHomes, boolean stack) {
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
     * Get the largest permission node value for teleport warmup.
     *
     * @param defaultTeleportWarmup the default teleport warmup time, if no perms are set
     * @return the largest permission node value for teleport warmup
     */
    public int getMaxTeleportWarmup(int defaultTeleportWarmup) {
        final List<Integer> homes = getNumericalPermissions("huskhomes.teleport_warmup.");
        if (homes.isEmpty()) {
            return defaultTeleportWarmup;
        }
        return homes.get(0);
    }

    /**
     * Get the number of free home slots this user may set.
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
     * Gets a list of numbers from the prefixed permission nodes.
     *
     * @param nodePrefix the prefix of the permission nodes to get
     * @return a list of numbers from the prefixed permission nodes, sorted by size
     */
    @NotNull
    protected List<Integer> getNumericalPermissions(@NotNull String nodePrefix) {
        return plugin.getHook(LuckPermsHook.class)
                .map(hook -> hook.getNumericalPermissions(this, nodePrefix))
                .orElseGet(() -> getPermissions().entrySet().stream().filter(Map.Entry::getValue)
                        .filter(perm -> perm.getKey().startsWith(nodePrefix))
                        .filter(perm -> canParse(perm.getKey(), nodePrefix))
                        .map(p -> Integer.parseInt(p.getKey().substring(nodePrefix.length())))
                        .sorted(Collections.reverseOrder()).toList());
    }

    // Remove node prefix from the permission and parse as an integer
    private static boolean canParse(@NotNull String perm, @NotNull String nodePrefix) {
        try {
            Integer.parseInt(perm.substring(nodePrefix.length()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}