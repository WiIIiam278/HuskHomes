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

package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RtpCommand extends Command implements UserListTabProvider {

    protected RtpCommand(@NotNull HuskHomes plugin) {
        super("rtp", List.of(), "[player] [world]", plugin);
        addAdditionalPermissions(Map.of(
                "other", true,
                "world", true
        ));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<OnlineUser> optionalTeleporter = args.length >= 1 ? plugin.getOnlineUser(args[0])
                : executor instanceof OnlineUser ? Optional.of((OnlineUser) executor) : Optional.empty();
        if (optionalTeleporter.isEmpty()) {
            if (args.length == 0) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }

            plugin.getLocales().getLocale("error_player_not_found", args[0])
                    .ifPresent(executor::sendMessage);
            return;
        }

        // Validate, then executeTeleport the RTP
        final OnlineUser teleporter = optionalTeleporter.get();
        this.validateRtp(teleporter, executor, args.length > 1 ? removeFirstArg(args) : args)
                .ifPresent(world -> this.executeRtp(teleporter, executor, world, args));
    }

    @Nullable
    @Override
    public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return switch (args.length) {
            case 0, 1 -> user.hasPermission("other") ? UserListTabProvider.super.suggestLocal(args)
                    : user instanceof OnlineUser online ? List.of(online.getUsername()) : List.of();
            case 2 -> user.hasPermission("world") ? plugin.getWorlds().stream()
                    .filter(world -> !plugin.getSettings().getRtp().isWorldRtpRestricted(world))
                    .map(World::getName).toList() : List.of();
            default -> null;
        };
    }

    /**
     * Validates the RTP command, returning the world to randomly teleport in if successful.
     *
     * @param teleporter The user to teleport
     * @param executor   The user executing the command
     * @param args       The command arguments
     * @return The world to randomly teleport in, if successful
     */
    private Optional<World> validateRtp(@NotNull OnlineUser teleporter, @NotNull CommandUser executor,
                                        @NotNull String[] args) {
        // Check permissions if the user is being teleported by another player
        if (!executor.equals(teleporter) && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        // Check they have sufficient funds
        if (!plugin.validateTransaction(teleporter, TransactionResolver.Action.RANDOM_TELEPORT)) {
            return Optional.empty();
        }

        // Determine the world to carry out the RTP in
        final World teleporterWorld = teleporter.getPosition().getWorld();
        final Optional<World> optionalWorld = args.length >= 1 ? plugin.getWorlds().stream().filter(w -> w.getName()
                .equalsIgnoreCase(args[0])).findFirst() : Optional.of(teleporterWorld);
        if (optionalWorld.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_world", args[0])
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        // Ensure the user has permission to randomly teleport in the world
        final World world = optionalWorld.get();
        if (!world.equals(teleporterWorld) && !executor.hasPermission(getPermission("world"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        if (plugin.getSettings().getRtp().isWorldRtpRestricted(world)) {
            plugin.getLocales().getLocale("error_rtp_restricted_world")
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        return Optional.of(world);
    }

    /**
     * Executes the random teleport.
     *
     * @param teleporter The player to teleport
     * @param executor   The player executing the command
     * @param world      The world to teleport in
     * @param args       Arguments to pass to the RTP engine
     */
    private void executeRtp(@NotNull OnlineUser teleporter, @NotNull CommandUser executor, @NotNull World world,
                            @NotNull String[] args) {
        // Generate a random position
        plugin.getLocales().getLocale("teleporting_random_generation")
                .ifPresent(teleporter::sendMessage);
        plugin.getRandomTeleportEngine()
                .getRandomPosition(world, args.length > 1 ? removeFirstArg(args) : args)
                .thenAccept(position -> {
                    if (position.isEmpty()) {
                        plugin.getLocales().getLocale("error_rtp_randomization_timeout")
                                .ifPresent(executor::sendMessage);
                        return;
                    }

                    // Build and execute the teleport
                    final TeleportBuilder builder = Teleport.builder(plugin)
                            .teleporter(teleporter)
                            .actions(TransactionResolver.Action.RANDOM_TELEPORT)
                            .target(position.get());
                    builder.buildAndComplete(executor.equals(teleporter), args);
                });
    }

}
