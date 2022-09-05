package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TpCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    protected TpCommand(@NotNull HuskHomes implementor) {
        super("tp", Permission.COMMAND_TP, implementor, "tpo");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        CompletableFuture.runAsync(() -> {
            // Ensure a valid target was found
            final Optional<TeleportTarget> teleportTarget = getTeleportTarget(args, onlineUser);
            if (teleportTarget.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", "/tp <target> [destination]")
                        .ifPresent(onlineUser::sendMessage);
                return;
            }

            // Determine the player to teleport
            final String targetPlayerToTeleport = ((teleportTarget.get() instanceof TargetPlayer) && args.length == 2)
                    ? args[0] : ((teleportTarget.get() instanceof TargetPosition)
                    ? args.length > 3 ? (isCoordinate(args[1]) && isCoordinate(args[2]) && isCoordinate(args[3]) ? args[0] : onlineUser.username)
                    : onlineUser.username : onlineUser.username);

            // Find the online user to teleport
            plugin.getCache().updatePlayerListCache(plugin, onlineUser).join();
            final String playerToTeleport = plugin.getCache().players.stream()
                    .filter(user -> user.equalsIgnoreCase(targetPlayerToTeleport)).findFirst()
                    .or(() -> Optional.ofNullable(targetPlayerToTeleport.equals("@s") ? onlineUser.username : null))
                    .or(() -> plugin.getCache().players.stream().filter(user -> user.toLowerCase().startsWith(targetPlayerToTeleport)).findFirst())
                    .orElse(null);

            // Ensure the player to teleport exists
            if (playerToTeleport == null) {
                plugin.getLocales().getLocale("error_player_not_found", targetPlayerToTeleport)
                        .ifPresent(onlineUser::sendMessage);
                return;
            }

            // Ensure the user has permission to teleport the player to teleport
            if (!playerToTeleport.equals(onlineUser.username)) {
                if (!onlineUser.hasPermission(Permission.COMMAND_TP_OTHER.node)) {
                    plugin.getLocales().getLocale("error_no_permission")
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
            }

            // Execute the teleport
            if (teleportTarget.get() instanceof TargetPlayer targetPlayer) {
                // Handle player teleport targets
                plugin.getTeleportManager()
                        .teleportNamedPlayers(playerToTeleport, targetPlayer.playerName, onlineUser, false)
                        .thenAccept(resultIfPlayerExists -> resultIfPlayerExists.ifPresentOrElse(
                                result -> {
                                    if (!result.successful) {
                                        plugin.getTeleportManager().finishTeleport(onlineUser, result);
                                        return;
                                    }
                                    plugin.getLocales().getLocale("teleporting_other_complete",
                                                    playerToTeleport, targetPlayer.playerName)
                                            .ifPresent(onlineUser::sendMessage);
                                },
                                () -> plugin.getLocales().getLocale("error_player_not_found", targetPlayer.playerName)
                                        .ifPresent(onlineUser::sendMessage)));
            } else if (teleportTarget.get() instanceof TargetPosition targetPosition) {
                // Handle coordinate teleport targets
                if (!onlineUser.hasPermission(Permission.COMMAND_TP_TO_COORDINATES.node)) {
                    plugin.getLocales().getLocale("error_no_permission").ifPresent(onlineUser::sendMessage);
                    return;
                }

                plugin.getTeleportManager()
                        .teleportPlayerByName(playerToTeleport, targetPosition.position, onlineUser, false)
                        .thenAccept(resultIfPlayerExists -> resultIfPlayerExists.ifPresent(
                                result -> {
                                    if (!result.successful || playerToTeleport.equalsIgnoreCase(onlineUser.username)) {
                                        plugin.getTeleportManager().finishTeleport(onlineUser, result);
                                        return;
                                    }
                                    plugin.getLocales().getLocale("teleporting_other_complete_position",
                                                    playerToTeleport,
                                                    Integer.toString((int) targetPosition.position.x),
                                                    Integer.toString((int) targetPosition.position.y),
                                                    Integer.toString((int) targetPosition.position.z))
                                            .ifPresent(onlineUser::sendMessage);
                                }));
            }
        }).exceptionally(throwable -> {
            plugin.getLoggingAdapter().log(Level.SEVERE, "An error occurred whilst executing a teleport command", throwable);
            return null;
        });
    }

    /**
     * Determines the teleport target from a set of arguments, which may be comprised of usernames and/or position
     * coordinates with a world and server.
     *
     * @param args       The arguments to parse.
     * @param relativeTo The position to use as a relative reference. This is used for relative coordinate handling
     *                   (e.g. {@code ~-10 ~ ~20})
     * @return The teleport target, if it could be parsed, otherwise and empty {@link Optional}.
     */
    @NotNull
    private Optional<TeleportTarget> getTeleportTarget(@NotNull String[] args, @NotNull OnlineUser relativeTo) {
        if (args.length == 1 || args.length == 2) {
            return Optional.of(new TargetPlayer(args[args.length - 1]));
        }
        if (args.length > 2 && args.length < 7) {
            final Optional<TeleportTarget> targetPosition = Position.parse(args, relativeTo.getPosition())
                    .map(TargetPosition::new);
            return targetPosition.or(() -> Position.parse(Arrays.copyOfRange(args, 1, args.length),
                    relativeTo.getPosition()).map(TargetPosition::new));
        }
        return Optional.empty();
    }

    /**
     * Determines if a string is a valid (relative) position coordinate double target.
     *
     * @param coordinate The string to check.
     * @return {@code true} if the string is a valid coordinate, otherwise {@code false}.
     */
    private boolean isCoordinate(@NotNull String coordinate) {
        try {
            if (coordinate.startsWith("~")) {
                coordinate = coordinate.substring(1);
            }
            if (coordinate.isBlank()) {
                return true;
            }
            Double.parseDouble(coordinate);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        if (args.length < 2 || args.length > 6) {
            plugin.getLoggingAdapter().log(Level.WARNING, "Invalid syntax. Usage: tp <player> <destination>");
            return;
        }
        final OnlineUser playerToTeleport = plugin.findPlayer(args[0]).orElse(null);
        if (playerToTeleport == null) {
            plugin.getLoggingAdapter().log(Level.WARNING, "Player not found: " + args[0]);
            return;
        }
        final TeleportTarget teleportTarget;
        if (args.length == 2) {
            teleportTarget = new TargetPlayer(args[1]);
        } else {
            try {
                teleportTarget = new TargetPosition(new Position(
                        Double.parseDouble(args[1]),
                        Double.parseDouble(args[2]),
                        Double.parseDouble(args[3]),
                        0f, 0f,
                        args.length >= 5 ? new World(args[4], UUID.randomUUID()) : plugin.getWorlds().get(0),
                        args.length == 6 ? new Server(args[5]) : plugin.getPluginServer()));
            } catch (NumberFormatException e) {
                plugin.getLoggingAdapter().log(Level.WARNING, "Invalid syntax. Usage: tp <player> <x> <y> <z> [world] [server]");
                return;
            }
        }

        // Execute the console teleport
        if (teleportTarget instanceof TargetPlayer targetPlayer) {
            plugin.getLoggingAdapter().log(Level.INFO, "Teleporting " + playerToTeleport.username
                    + " to " + targetPlayer.playerName);
            plugin.getTeleportManager().teleportToPlayerByName(playerToTeleport, targetPlayer.playerName, false);
        } else {
            final TargetPosition targetPosition = (TargetPosition) teleportTarget;
            plugin.getTeleportManager().teleport(playerToTeleport, targetPosition.position)
                    .thenAccept(teleportResult -> {
                        if (teleportResult.successful) {
                            plugin.getLoggingAdapter().log(Level.INFO, "Successfully teleported " + playerToTeleport.username);
                        } else {
                            plugin.getLoggingAdapter().log(Level.WARNING, "Failed to teleport " + playerToTeleport.username
                                    + " (" + teleportResult.name() + ")");
                        }
                        plugin.getTeleportManager().finishTeleport(playerToTeleport, teleportResult);
                    });
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        final boolean serveCoordinateCompletions = user != null && user.hasPermission(Permission.COMMAND_TP_TO_COORDINATES.node);
        switch (args.length) {
            case 0, 1 -> {
                final ArrayList<String> completions = new ArrayList<>();
                completions.addAll(serveCoordinateCompletions
                        ? List.of("~", "~ ~", "~ ~ ~",
                        Integer.toString((int) user.getPosition().x),
                        ((int) user.getPosition().x + " " + (int) user.getPosition().y),
                        ((int) user.getPosition().x + " " + (int) user.getPosition().y + " " + (int) user.getPosition().z))
                        : Collections.emptyList());
                completions.addAll(plugin.getCache().players);
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                        .sorted().collect(Collectors.toList());
            }
            case 2 -> {
                final ArrayList<String> completions = new ArrayList<>();
                if (isCoordinate(args[0])) {
                    if (user == null) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) user.getPosition().y)));
                    completions.addAll(List.of("~ ~", (int) user.getPosition().y + " " + (int) user.getPosition().z));
                } else {
                    completions.addAll(serveCoordinateCompletions
                            ? List.of("~", "~ ~", "~ ~ ~",
                            Integer.toString((int) user.getPosition().x),
                            ((int) user.getPosition().x + " " + (int) user.getPosition().y),
                            ((int) user.getPosition().x + " " + (int) user.getPosition().y + " " + (int) user.getPosition().z))
                            : Collections.emptyList());
                    completions.addAll(plugin.getCache().players);
                }
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .sorted().collect(Collectors.toList());
            }
            case 3 -> {
                final ArrayList<String> completions = new ArrayList<>();
                if (isCoordinate(args[0]) && isCoordinate(args[1])) {
                    if (!serveCoordinateCompletions) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) user.getPosition().z)));
                } else if (isCoordinate(args[1])) {
                    if (!serveCoordinateCompletions) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) user.getPosition().y)));
                    completions.addAll(List.of("~ ~", (int) user.getPosition().y + " " + (int) user.getPosition().z));
                }
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .sorted().collect(Collectors.toList());
            }
            case 4 -> {
                final ArrayList<String> completions = new ArrayList<>();
                if (isCoordinate(args[1]) && isCoordinate(args[2]) && !isCoordinate(args[0])) {
                    if (!serveCoordinateCompletions) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) user.getPosition().z)));
                }
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                        .sorted().collect(Collectors.toList());
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Represents a target position to teleport to
     */
    private static final class TargetPosition extends TeleportTarget {
        /**
         * The target {@link Position}
         */
        private final Position position;

        private TargetPosition(@NotNull Position position) {
            this.position = position;
        }

    }

    /**
     * Represents a target player to teleport to
     */
    private static final class TargetPlayer extends TeleportTarget {
        /**
         * Name of the target player
         */
        private final String playerName;

        private TargetPlayer(@NotNull String playerName) {
            this.playerName = playerName;
        }
    }

    /**
     * Identifies the target of a teleport command operation
     */
    private static abstract class TeleportTarget {
    }
}
