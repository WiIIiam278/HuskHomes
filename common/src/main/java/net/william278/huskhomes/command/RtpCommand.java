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
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RtpCommand extends Command implements UserListTabCompletable {

    private final Random random = new Random();

    protected RtpCommand(@NotNull HuskHomes plugin) {
        super(
                List.of("rtp"),
                "[player] [<world> [server]|<world>]",
                plugin
        );

        addAdditionalPermissions(Map.of(
                "other", true
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

        final OnlineUser teleporter = optionalTeleporter.get();

        // Determine the target world and server based on the command arguments
        String worldName = teleporter.getPosition().getWorld().getName();
        String targetServer = null;

        if (args.length == 2) {
            // If there's only one argument after the player name, it could be either a world or a server
            if (plugin.getSettings().getRtp().getRandomTargetServers().containsKey(args[1])) {
                targetServer = args[1];
                worldName = teleporter.getPosition().getWorld().getName();
            } else {
                worldName = args[1];
            }
        } else if (args.length > 2) {
            // If two arguments are provided after the player name, treat them as world and server
            worldName = args[1];
            targetServer = args[2];
        }

        // Validate world and server, and execute RTP
        validateRtp(teleporter, executor, worldName.replace("minecraft:", ""), targetServer)
                .ifPresent(entry -> executeRtp(teleporter, executor, entry.getKey(), entry.getValue(), args));
    }

    @Nullable
    @Override
    public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return switch (args.length) {
            case 0, 1 -> user.hasPermission(getPermission("other"))
                    ? UserListTabCompletable.super.suggest(user, args)
                    : user instanceof OnlineUser online ? List.of(online.getName()) : List.of();

            case 2 -> {
                String input = args[1].toLowerCase();

                // Check if the input could be a world or a server name
                List<String> possibleSuggestions = new ArrayList<>();

                // Suggest available servers if user has permission
                possibleSuggestions.addAll(plugin.getSettings().getRtp().getRandomTargetServers().keySet().stream()
                        .filter(server -> user.hasPermission(getPermission(server)))
                        .toList());

                // Additionally suggest worlds that the user has permission to RTP into
                possibleSuggestions.addAll(plugin.getWorlds().stream()
                        .filter(world -> !plugin.getSettings().getRtp().isWorldRtpRestricted(world))
                        .map(World::getName)
                        .filter(world -> user.hasPermission(getPermission(world)))
                        .toList());

                if (!input.isEmpty()) {
                    yield possibleSuggestions.stream()
                            .filter(suggestion -> suggestion.toLowerCase().startsWith(input))
                            .toList();
                }

                yield possibleSuggestions;
            }
            case 3 -> {
                // If worldName is a world, suggest servers that contain the world
                String worldName = args[1];

                List<String> possibleSuggestions = new ArrayList<>(plugin.getWorlds().stream()
                        .filter(world -> !plugin.getSettings().getRtp().isWorldRtpRestricted(world))
                        .map(World::getName)
                        .filter(world -> user.hasPermission(getPermission(world)))
                        .toList());

                if (possibleSuggestions.contains(worldName)) {
                    yield plugin.getSettings().getRtp().getRandomTargetServers().entrySet().stream()
                            .filter(entry -> entry.getValue().contains(worldName))
                            .map(Map.Entry::getKey)
                            .toList();
                }

                yield List.of();
            }

            default -> null;
        };
    }

    /**
     * Validates that a random teleport operation is valid.
     *
     * @param teleporter   The player being teleported
     * @param executor     The player executing the command
     * @param worldName    The world name to teleport to
     * @param targetServer The server name to teleport to (optional)
     * @return A pair of the target world and server to use for teleportation, if valid
     */
    private Optional<Map.Entry<World, String>> validateRtp(@NotNull OnlineUser teleporter, @NotNull CommandUser executor,
                                                           @NotNull String worldName, @Nullable String targetServer) {
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

        // Validate a cross-server RTP, if applicable
        if (plugin.getSettings().getRtp().isCrossServer() && !plugin.getServerName().equals(targetServer)) {
            return validateCrossServerRtp(executor, worldName, targetServer);
        }

        // Find the local world
        final Optional<World> localWorld = plugin.getWorlds().stream().filter((world) -> world
                .getName().replace("minecraft:", "")
                .equalsIgnoreCase(worldName)).findFirst();
        if (localWorld.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_world", worldName)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        // Check the local world is not restricted
        if (plugin.getSettings().getRtp().isWorldRtpRestricted(localWorld.get())) {
            plugin.getLocales().getLocale("error_rtp_restricted_world")
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        return localWorld.map(world -> new AbstractMap.SimpleImmutableEntry<>(world, worldName));
    }

    /**
     * Validates the RTP target world and server based on arguments, ensuring the server contains the target world.
     * - If no server is specified, randomly selects a server containing the world.
     * - Returns both the validated world and server as a pair.
     *
     * @param executor     The player executing the command
     * @param worldName    The world name to teleport to
     * @param targetServer The server name to teleport to (optional)
     * @return A pair of the target world and server to use for teleportation, if valid
     */
    private Optional<Map.Entry<World, String>> validateCrossServerRtp(CommandUser executor, String worldName, String targetServer) {
        // Get a list of servers that have the specified world
        Map<String, List<String>> randomTargetServers = plugin.getSettings().getRtp().getRandomTargetServers();
        List<String> eligibleServers = randomTargetServers.entrySet().stream()
                .filter(entry -> entry.getValue().contains(worldName))
                .map(Map.Entry::getKey)
                .toList();

        // If targetServer is specified, validate it; otherwise, pick a random eligible server
        String selectedServer = targetServer != null ? targetServer :
                (!eligibleServers.isEmpty() ? eligibleServers.get(random.nextInt(eligibleServers.size())) : null);

        // If no server found or the specified server is invalid, return an error
        if (selectedServer == null || (targetServer != null && !eligibleServers.contains(targetServer))) {
            plugin.getLocales().getLocale("error_invalid_world", worldName)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        Optional<World> targetWorld = plugin.getWorlds().stream()
                .filter(world -> world.getName().replace("minecraft:", "")
                        .equalsIgnoreCase(worldName))
                .findFirst()
                .or(() -> Optional.of(World.from(worldName)));

        return targetWorld.map(world -> new AbstractMap.SimpleImmutableEntry<>(world, selectedServer));
    }

    /**
     * Executes the RTP, handling both local and cross-server teleportation.
     * Uses the validated world-server pair from validateRtp.
     *
     * @param teleporter   The player to teleport
     * @param executor     The player executing the command
     * @param world        The validated world to teleport to
     * @param targetServer The validated server to teleport to
     * @param args         Arguments to pass to the RTP engine
     */
    private void executeRtp(@NotNull OnlineUser teleporter, @NotNull CommandUser executor,
                            @NotNull World world, @NotNull String targetServer, @NotNull String[] args) {
        // Generate a random position
        plugin.getLocales().getLocale("teleporting_random_generation")
                .ifPresent(teleporter::sendMessage);
        
        plugin.log(java.util.logging.Level.INFO, "RTP: User " + teleporter.getName() + " requesting RTP to world '" + world.getName() + "' on server '" + targetServer + "'");
        plugin.log(java.util.logging.Level.INFO, "RTP: Current server name: '" + plugin.getServerName() + "'");
        plugin.log(java.util.logging.Level.INFO, "RTP: Cross-server enabled: " + plugin.getSettings().getRtp().isCrossServer() + ", Cross-server settings enabled: " + plugin.getSettings().getCrossServer().isEnabled());

        if (plugin.getSettings().getRtp().isCrossServer() && plugin.getSettings().getCrossServer().isEnabled()
            && plugin.getSettings().getCrossServer().getBrokerType() == Broker.Type.REDIS) {
            plugin.log(java.util.logging.Level.INFO, "RTP: Entering cross-server logic, comparing targetServer '" + targetServer + "' with currentServer '" + plugin.getServerName() + "'");
            if (targetServer.equals(plugin.getServerName())) {
                plugin.log(java.util.logging.Level.INFO, "RTP: Target server matches current server, performing local RTP");
                performLocalRTP(teleporter, executor, world, args);
                return;
            }
            plugin.log(java.util.logging.Level.INFO, "RTP: Cross-server RTP to " + targetServer + ", sending broker message");

            plugin.getBroker().ifPresent(b -> Message.builder()
                    .type(Message.MessageType.REQUEST_RTP_LOCATION)
                    .target(targetServer, Message.TargetType.SERVER)
                    .payload(Payload.string(world.getName()))
                    .build().send(b, teleporter));
            return;
        }

        performLocalRTP(teleporter, executor, world, args);
    }

    /**
     * Performs the RTP locally.
     *
     * @param teleporter person to teleport
     * @param executor   the person executing the teleport
     * @param world      the world to teleport to
     * @param args       rtp engine args
     */
    private void performLocalRTP(@NotNull OnlineUser teleporter, @NotNull CommandUser executor, @NotNull World world,
                                 @NotNull String[] args) {
        plugin.log(java.util.logging.Level.INFO, "RTP: Starting local RTP for " + teleporter.getName() + " in world " + world.getName());
        plugin.getRandomTeleportEngine()
                .getRandomPosition(world, args.length > 1 ? removeFirstArg(args) : args)
                .thenAccept(position -> {
                    if (position.isEmpty()) {
                        plugin.log(java.util.logging.Level.WARNING, "RTP: Failed to find safe position for " + teleporter.getName() + " after max attempts");
                        plugin.getLocales().getLocale("error_rtp_randomization_timeout")
                                .ifPresent(executor::sendMessage);
                        return;
                    }
                    plugin.log(java.util.logging.Level.INFO, "RTP: Found safe position at " + position.get().getX() + ", " + position.get().getY() + ", " + position.get().getZ());

                    // Build and execute the teleport
                    final TeleportBuilder builder = Teleport.builder(plugin)
                            .teleporter(teleporter)
                            .type(Teleport.Type.RANDOM_TELEPORT)
                            .actions(TransactionResolver.Action.RANDOM_TELEPORT)
                            .target(position.get());
                    builder.buildAndComplete(executor.equals(teleporter), args);
                });
    }
}
