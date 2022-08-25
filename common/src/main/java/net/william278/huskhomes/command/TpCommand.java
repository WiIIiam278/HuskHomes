package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.HuskHomesException;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TpCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    protected TpCommand(@NotNull HuskHomes implementor) {
        super("tp", Permission.COMMAND_TP, implementor, "tpo");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        String teleportingUserName = onlineUser.username;
        TeleportCommandTarget targetPosition;

        // Validate argument length
        if (args.length > 6 || args.length < 1) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tp <target> [destination]")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // If the first argument is a player target
        if (args.length == 1) {
            targetPosition = new TeleportCommandTarget(args[0]);
        } else {
            // Determine if a player argument has been passed before the destination argument
            int coordinatesIndex = ((args.length > 3 && (isCoordinate(args[1]) && isCoordinate(args[2]) && isCoordinate(args[3])))
                                    || (args.length == 2)) ? 1 : 0;
            if (coordinatesIndex == 1) {
                teleportingUserName = args[0];
            }

            // Determine the target position (player or coordinates)
            if (args.length == 2) {
                targetPosition = new TeleportCommandTarget(args[1]);
            } else {
                // Coordinate teleportation requires a permission node
                if (!onlineUser.hasPermission(Permission.COMMAND_TP_TO_COORDINATES.node)) {
                    plugin.getLocales().getLocale("error_no_permission").ifPresent(onlineUser::sendMessage);
                    return;
                }

                // Parse the coordinates and set the target position
                final Position userPosition = onlineUser.getPosition();
                final Optional<Position> parsedPosition = Position.parse(userPosition,
                        Arrays.copyOfRange(args, coordinatesIndex, args.length));
                if (parsedPosition.isEmpty()) {
                    plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/tp <target> <x> <y> <z> [world] [server]")
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                targetPosition = new TeleportCommandTarget(parsedPosition.get());
            }
        }

        // Execute the teleport
        final String playerToTeleportName = teleportingUserName;
        if (targetPosition.targetType == TeleportCommandTarget.TargetType.PLAYER) {
            // Teleport players by usernames
            assert targetPosition.targetPlayer != null;
            plugin.getTeleportManager()
                    .teleportPlayerToPlayerByName(playerToTeleportName, targetPosition.targetPlayer, onlineUser, false)
                    .thenAccept(resultIfPlayerExists -> resultIfPlayerExists.ifPresentOrElse(
                            result -> {
                                if (result.successful) {
                                    plugin.getLocales().getLocale("teleporting_other_complete",
                                                    playerToTeleportName, targetPosition.targetPlayer)
                                            .ifPresent(onlineUser::sendMessage);
                                    return;
                                }
                                plugin.getTeleportManager().finishTeleport(onlineUser, result);
                            },
                            () -> plugin.getLocales().getLocale("error_player_not_found", playerToTeleportName)
                                    .ifPresent(onlineUser::sendMessage)));
            return;
        } else if (targetPosition.targetType == TeleportCommandTarget.TargetType.POSITION) {
            // Teleport players by specified position
            assert targetPosition.targetPosition != null;
            plugin.getTeleportManager().teleportPlayerByName(playerToTeleportName, targetPosition.targetPosition, onlineUser, false)
                    .thenAccept(resultIfPlayerExists -> resultIfPlayerExists.ifPresentOrElse(
                            result -> {
                                if (result.successful && !playerToTeleportName.equalsIgnoreCase(onlineUser.username)) {
                                    plugin.getLocales().getLocale("teleporting_other_complete_position",
                                                    playerToTeleportName,
                                                    Integer.toString((int) targetPosition.targetPosition.x),
                                                    Integer.toString((int) targetPosition.targetPosition.y),
                                                    Integer.toString((int) targetPosition.targetPosition.z))
                                            .ifPresent(onlineUser::sendMessage);
                                    return;
                                }
                                plugin.getTeleportManager().finishTeleport(onlineUser, result);
                            },
                            () -> plugin.getLocales().getLocale("error_player_not_found", playerToTeleportName)
                                    .ifPresent(onlineUser::sendMessage)));
            return;
        }
        throw new HuskHomesException("Attempted to execute invalid teleport command operation");
    }

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
        final TeleportCommandTarget teleportCommandTarget;
        if (args.length == 2) {
            teleportCommandTarget = new TeleportCommandTarget(args[1]);
        } else {
            try {
                teleportCommandTarget = new TeleportCommandTarget(new Position(
                        Double.parseDouble(args[1]),
                        Double.parseDouble(args[2]),
                        Double.parseDouble(args[3]),
                        0f, 0f,
                        args.length >= 5 ? new World(args[4], UUID.randomUUID()) : plugin.getWorlds().get(0),
                        args.length == 6 ? new Server(args[5]) : plugin.getServer(playerToTeleport)));
            } catch (NumberFormatException e) {
                plugin.getLoggingAdapter().log(Level.WARNING, "Invalid syntax. Usage: tp <player> <x> <y> <z> [world] [server]");
                return;
            }
        }

        // Execute the console teleport
        if (teleportCommandTarget.targetType == TeleportCommandTarget.TargetType.PLAYER) {
            assert teleportCommandTarget.targetPlayer != null;
            plugin.getLoggingAdapter().log(Level.INFO, "Teleporting " + playerToTeleport.username
                                                       + " to " + teleportCommandTarget.targetPlayer);
            plugin.getTeleportManager().teleportToPlayerByName(playerToTeleport, teleportCommandTarget.targetPlayer, false);
        } else {
            assert teleportCommandTarget.targetPosition != null;
            plugin.getTeleportManager().teleport(playerToTeleport, teleportCommandTarget.targetPosition)
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
     * Identifies the target of a teleport command operation
     */
    private static class TeleportCommandTarget {

        @NotNull
        private final TargetType targetType;
        @Nullable
        private String targetPlayer;
        @Nullable
        private Position targetPosition;

        public TeleportCommandTarget(@NotNull String targetPlayer) {
            this.targetPlayer = targetPlayer;
            this.targetType = TargetType.PLAYER;
        }

        public TeleportCommandTarget(@NotNull Position targetPosition) {
            this.targetPosition = targetPosition;
            this.targetType = TargetType.POSITION;
        }

        public enum TargetType {
            PLAYER,
            POSITION
        }
    }
}
