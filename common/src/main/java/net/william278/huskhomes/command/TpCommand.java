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
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.*;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TpCommand extends Command implements TabProvider {

    protected TpCommand(@NotNull HuskHomes plugin) {
        super("tp", List.of("tpo"), "[<player|position>] [target]", plugin);
        addAdditionalPermissions(Map.of("coordinates", true, "other", true));
        setOperatorCommand(true);
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
            case 2 -> this.execute(executor, Teleportable.username(args[0]), Target.username(args[1]), args);
            default -> {
                final Position basePosition = getBasePosition(executor);
                Optional<Position> target = executor.hasPermission(getPermission("coordinates"))
                        ? parsePositionArgs(basePosition, args, 0) : Optional.empty();
                if (target.isPresent()) {
                    if (!(executor instanceof OnlineUser user)) {
                        plugin.getLocales().getLocale("error_in_game_only")
                                .ifPresent(executor::sendMessage);
                        return;
                    }

                    this.execute(executor, user, target.get(), args);
                    return;
                }

                target = executor.hasPermission(getPermission("coordinates"))
                        ? parsePositionArgs(basePosition, args, 1) : Optional.empty();
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
        // Build and execute the teleport
        final TeleportBuilder builder = Teleport.builder(plugin)
                .teleporter(teleportable)
                .target(target);

        if (executor instanceof OnlineUser onlineUser) {
            if (!onlineUser.hasPermission(getPermission("other"))) {
                plugin.getLocales().getLocale("error_no_permission")
                        .ifPresent(executor::sendMessage);
                return;
            }
            if (target instanceof Teleportable teleportableTarget) {
                if (onlineUser.getUsername().equals(teleportableTarget.getUsername())) {
                    plugin.getLocales().getLocale("error_cannot_teleport_self")
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
            }
            builder.executor(onlineUser);
        }

        builder.buildAndComplete(false, args);

        // Display a teleport completion message
        final String teleporterName = teleportable instanceof OnlineUser user
                ? user.getUsername() : ((Username) teleportable).name();
        if (target instanceof Position position) {
            plugin.getLocales().getLocale("teleporting_other_complete_position", teleporterName,
                            Integer.toString((int) position.getX()), Integer.toString((int) position.getY()),
                            Integer.toString((int) position.getZ()))
                    .ifPresent(executor::sendMessage);
        } else {
            plugin.getLocales().getLocale("teleporting_other_complete", teleporterName, ((Username) target).name())
                    .ifPresent(executor::sendMessage);
        }
    }

    @Override
    @NotNull
    public final List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        final Position relative = getBasePosition(user);
        final boolean serveCoordinateCompletions = user.hasPermission(getPermission("coordinates"));
        final boolean servePlayerCompletions = user.hasPermission(getPermission("other"));
        switch (args.length) {
            case 0, 1 -> {
                final ArrayList<String> completions = new ArrayList<>();
                completions.addAll(serveCoordinateCompletions
                        ? List.of("~", "~ ~", "~ ~ ~",
                        Integer.toString((int) relative.getX()),
                        ((int) relative.getX() + " " + (int) relative.getY()),
                        ((int) relative.getX() + " " + (int) relative.getY() + " " + (int) relative.getZ()))
                        : List.of());
                if (servePlayerCompletions) {
                    completions.addAll(plugin.getPlayerList(false));
                }
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                        .sorted().collect(Collectors.toList());
            }
            case 2 -> {
                final ArrayList<String> completions = new ArrayList<>();
                if (isCoordinate(args, 0)) {
                    completions.addAll(List.of("~", Integer.toString((int) relative.getY())));
                    completions.addAll(List.of("~ ~", (int) relative.getY() + " " + (int) relative.getZ()));
                } else {
                    completions.addAll(
                            serveCoordinateCompletions
                                    ? List.of("~", "~ ~", "~ ~ ~",
                                    Integer.toString((int) relative.getX()),
                                    ((int) relative.getX() + " " + (int) relative.getY()),
                                    ((int) relative.getX() + " " + (int) relative.getY() + " " + (int) relative.getZ()))
                                    : List.of()
                    );
                    if (servePlayerCompletions) {
                        completions.addAll(plugin.getPlayerList(false));
                    }
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
                    completions.addAll(List.of("~", Integer.toString((int) relative.getZ())));
                } else if (isCoordinate(args, 1)) {
                    if (!serveCoordinateCompletions) {
                        return completions;
                    }
                    completions.addAll(List.of("~", Integer.toString((int) relative.getY())));
                    completions.addAll(List.of("~ ~", (int) relative.getY() + " " + (int) relative.getZ()));
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
                    completions.addAll(List.of("~", Integer.toString((int) relative.getZ())));
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
