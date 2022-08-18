package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.HuskHomesException;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TpCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    protected TpCommand(@NotNull HuskHomes implementor) {
        super("tp", Permission.COMMAND_TP, implementor, "tpo");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        OnlineUser userToTeleport = onlineUser;
        TeleportCommandTarget targetPosition;

        // Validate argument length
        if (args.length > 6 || args.length < 1) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tp <target> [destination]")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // If the first argument is a player
        if (args.length == 1) {
            targetPosition = new TeleportCommandTarget(args[0]);
        } else {
            // Determine if a player argument has been passed before the destination argument
            int coordinatesIndex = (args[0].matches("^[a-zA-Z0-9_.*]{1,17}$") || args.length == 4 || args.length == 6) ? 1 : 0;
            if (coordinatesIndex == 1) {
                userToTeleport = plugin.getOnlinePlayers().stream().filter(user -> user.username.equalsIgnoreCase(args[0]))
                        .findFirst().orElse(plugin.getOnlinePlayers().stream().filter(user -> user.username.startsWith(args[0]))
                                .findFirst().orElse(null));
                if (userToTeleport == null) {
                    plugin.getLocales().getLocale("error_player_not_found", args[0]).ifPresent(onlineUser::sendMessage);
                    return;
                }
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
                final Position userPosition = onlineUser.getPosition().join();
                final Optional<Position> parsedPosition = Position.parse(userPosition,
                        Arrays.copyOfRange(args, coordinatesIndex, args.length - 1));
                if (parsedPosition.isEmpty()) {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/tp <target> <x> <y> <z> [world] [server]")
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                targetPosition = new TeleportCommandTarget(parsedPosition.get());
            }
        }

        // Execute the teleport
        final OnlineUser teleportingUser = userToTeleport;
        if (targetPosition.targetType == TeleportCommandTarget.TargetType.PLAYER) {
            assert targetPosition.targetPlayer != null;
            plugin.getTeleportManager().teleportToPlayer(teleportingUser, targetPosition.targetPlayer);
        } else if (targetPosition.targetType == TeleportCommandTarget.TargetType.POSITION) {
            assert targetPosition.targetPosition != null;
            plugin.getTeleportManager().teleport(teleportingUser, targetPosition.targetPosition).thenAccept(
                    result -> plugin.getTeleportManager().finishTeleport(teleportingUser, result));
        }
        throw new HuskHomesException("Attempted to execute invalid teleport command operation");
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return Collections.emptyList();
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
