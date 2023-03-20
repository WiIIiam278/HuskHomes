package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.teleport.*;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TpCommand extends Command implements TabProvider {

    protected TpCommand(@NotNull HuskHomes implementor) {
        super("tp", List.of("tpo"), "[<player|position>] [target]", implementor);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                if (!(executor instanceof OnlineUser user)) {
                    plugin.getLocales().getLocale("error_in_game_only")
                            .ifPresent(executor::sendMessage);
                    return;
                }

                this.execute(executor, user, Target.username(args[0]), args);
            }
            case 2 -> execute(executor, Teleportable.username(args[0]), Target.username(args[1]), args);
            default -> {
                final Position basePosition = getBasePosition(executor);
                Optional<Position> target = parsePositionArgs(basePosition, args, 0);
                if (target.isPresent()) {
                    if (!(executor instanceof OnlineUser user)) {
                        plugin.getLocales().getLocale("error_in_game_only")
                                .ifPresent(executor::sendMessage);
                        return;
                    }

                    this.execute(executor, user, target.get(), args);
                    return;
                }

                target = parsePositionArgs(basePosition, args, 1);
                if (target.isPresent() && args.length >= 1) {
                    this.execute(executor, Teleportable.username(args[0]), target.get(), args);
                    return;
                }

                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
            }
        }
    }

    // Execute a teleport
    private void execute(@NotNull CommandUser executor, @NotNull Teleportable teleportable, @NotNull Target target,
                         @NotNull String[] args) {
        try {
            final TeleportBuilder builder = Teleport.builder(plugin)
                    .teleporter(teleportable)
                    .target(target);
            if (executor instanceof OnlineUser user) {
                builder.executor(user);
            }
            builder.toTeleport().execute();
        } catch (TeleportationException e) {
            e.displayMessage(executor, plugin, args);
        }
    }

    @Override
    @NotNull
    public final List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        final Position basePosition = getBasePosition(user);
        final boolean serveCoordinateCompletions = user.hasPermission(getPermission("coordinates"));
        switch (args.length) {
            case 0, 1 -> {
                final ArrayList<String> completions = new ArrayList<>();
                completions.addAll(serveCoordinateCompletions
                        ? List.of("~", "~ ~", "~ ~ ~",
                        Integer.toString((int) basePosition.getX()),
                        ((int) basePosition.getX() + " " + (int) basePosition.getY()),
                        ((int) basePosition.getX() + " " + (int) basePosition.getY() + " " + (int) basePosition.getZ()))
                        : List.of());
                completions.addAll(plugin.getCache().getPlayers());
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                        .sorted().collect(Collectors.toList());
            }
            case 2 -> {
                final ArrayList<String> completions = new ArrayList<>();
                if (isCoordinate(args, 0)) {
                    completions.addAll(List.of("~", Integer.toString((int) basePosition.getY())));
                    completions.addAll(List.of("~ ~", (int) basePosition.getY() + " " + (int) basePosition.getZ()));
                } else {
                    completions.addAll(serveCoordinateCompletions
                            ? List.of("~", "~ ~", "~ ~ ~",
                            Integer.toString((int) basePosition.getX()),
                            ((int) basePosition.getX() + " " + (int) basePosition.getY()),
                            ((int) basePosition.getX() + " " + (int) basePosition.getY() + " " + (int) basePosition.getZ()))
                            : List.of());
                    completions.addAll(plugin.getCache().getPlayers());
                }
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .sorted().collect(Collectors.toList());
            }
            case 3 -> {
                final ArrayList<String> completions = new ArrayList<>();
                if (isCoordinate(args, 1) && isCoordinate(args, 2)) {
                    if (!serveCoordinateCompletions) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) basePosition.getZ())));
                } else if (isCoordinate(args, 1)) {
                    if (!serveCoordinateCompletions) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) basePosition.getY())));
                    completions.addAll(List.of("~ ~", (int) basePosition.getY() + " " + (int) basePosition.getZ()));
                }
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .sorted().collect(Collectors.toList());
            }
            case 4 -> {
                final ArrayList<String> completions = new ArrayList<>();
                if (isCoordinate(args, 1) && isCoordinate(args, 2) && !isCoordinate(args, 0)) {
                    if (!serveCoordinateCompletions) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) basePosition.getZ())));
                }
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                        .sorted().collect(Collectors.toList());
            }
            default -> {
                return List.of();
            }
        }
    }

    private boolean isCoordinate(@NotNull String[] args, int index) {
        return parseCoordinateArg(args, index, 0d).isPresent();
    }

}
