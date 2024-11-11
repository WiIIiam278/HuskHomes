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

import lombok.Getter;
import lombok.Setter;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Node implements Executable {

    protected static final String PERMISSION_PREFIX = "huskhomes.command";

    protected final HuskHomes plugin;
    @Getter
    private final List<String> aliases;
    @Getter
    @Setter
    private boolean operatorCommand = false;

    protected Node(@NotNull List<String> aliases, @NotNull HuskHomes plugin) {
        if (aliases.isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be blank");
        }
        this.aliases = aliases;
        this.plugin = plugin;
    }

    @NotNull
    public String getPermission(@NotNull String... child) {
        final StringJoiner joiner = new StringJoiner(".")
                .add(PERMISSION_PREFIX)
                .add(getName());
        for (String node : child) {
            joiner.add(node);
        }
        return joiner.toString().trim();
    }

    public boolean hasPermission(@NotNull CommandUser executor, @NotNull String... child) {
        return executor.hasPermission(getPermission(child)) || executor.hasPermission(getPermission("*"));
    }

    @NotNull
    public String getName() {
        if (aliases.isEmpty()) {
            throw new IllegalStateException("Primary alias of command node is blank");
        }
        return aliases.get(0);
    }

    protected Optional<User> resolveUser(@NotNull CommandUser executor, @NotNull String[] args) {
        return parseStringArg(args, 0)
                .flatMap(a -> plugin.getDatabase().getUser(a)).map(SavedUser::getUser)
                .or(() -> {
                    if (executor instanceof OnlineUser online) {
                        return Optional.of(online);
                    }
                    return Optional.empty();
                });
    }
    protected Optional<Integer> parseIntArg(@NotNull String[] args, int index) {
        try {
            if (args.length > index) {
                return Optional.of(Integer.parseInt(args[index]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<Float> parseFloatArg(@NotNull String[] args, int index) {
        try {
            if (args.length > index) {
                return Optional.of(Float.parseFloat(args[index]));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<Double> parseCoordinateArg(@NotNull String[] args, int index, double relativeTo) {
        try {
            if (args.length > index) {
                final String arg = args[index];
                if (arg.startsWith("~")) {
                    final String coordinate = arg.substring(1);
                    if (coordinate.isBlank()) {
                        return Optional.of(relativeTo);
                    }
                    return Optional.of(relativeTo + Double.parseDouble(coordinate));
                }
                return Optional.of(Double.parseDouble(arg));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    protected Optional<String> parseStringArg(@NotNull String[] args, int index) {
        if (args.length > index) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }

    protected Optional<World> parseWorldArg(@NotNull String[] args, int index) {
        if (args.length > index) {
            final String worldName = args[index];
            final World.Environment environment = worldName.endsWith("_the_end") ? World.Environment.THE_END :
                    worldName.endsWith("nether") ? World.Environment.NETHER : World.Environment.OVERWORLD;
            return Optional.of(plugin.getWorlds().stream()
                    .filter(world -> world.getName().equalsIgnoreCase(worldName))
                    .findFirst()
                    .orElse(World.from(worldName, new UUID(0, 0), environment)));
        }
        return Optional.empty();
    }

    protected Optional<String> parseGreedyArguments(@NotNull String[] args) {
        if (args.length > 1) {
            final StringJoiner sentence = new StringJoiner(" ");
            for (int i = 1; i < args.length; i++) {
                sentence.add(args[i]);
            }
            return Optional.of(sentence.toString().trim());
        }
        return Optional.empty();
    }

    @NotNull
    protected Position getBasePosition(@NotNull CommandUser executor) {
        return executor instanceof OnlineUser user ? user.getPosition() : plugin.getSpawn()
                .orElse(Position.at(0, 0, 0, 0, 0, plugin.getWorlds().get(0), plugin.getServerName()));
    }

    protected Optional<Position> parsePositionArgs(@NotNull Position basePosition, @NotNull String[] args, int from) {
        // Parse x, y, and z
        final Optional<Double> x = parseCoordinateArg(args, from, basePosition.getX());
        final Optional<Double> y = parseCoordinateArg(args, from + 1, basePosition.getY());
        final Optional<Double> z = parseCoordinateArg(args, from + 2, basePosition.getZ());
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return Optional.empty();
        }

        // Parse world and server name
        final Optional<World> world = parseWorldArg(args, from + 3);
        final Optional<String> server = parseStringArg(args, from + 4);

        // Parse yaw, pitch
        int angleStartIndex = (world.isEmpty() ? 3 : server.isEmpty() ? 4 : 5);
        final Optional<Float> yaw = parseFloatArg(args, from + angleStartIndex);
        final Optional<Float> pitch = parseFloatArg(args, from + angleStartIndex + 1);

        return Optional.of(Position.at(x.get(), y.get(), z.get(),
                yaw.orElse(basePosition.getYaw()), pitch.orElse(basePosition.getPitch()),
                world.orElse(basePosition.getWorld()), server.orElse(basePosition.getServer())));
    }


    protected Optional<Teleportable> resolveTeleporter(@NotNull CommandUser executor, @NotNull String[] args) {
        return parseStringArg(args, 0).map(Teleportable::username)
                .or(() -> executor instanceof Teleportable ? Optional.of((Teleportable) executor) : Optional.empty());
    }


    protected Optional<Boolean> parseBooleanArg(@NotNull String[] args, int index) {
        if (args.length <= index) {
            return Optional.empty();
        }
        final String arg = args[index].toLowerCase(Locale.ENGLISH);
        if (arg.equals("true") || arg.equals("enable") || arg.equals("on")) {
            return Optional.of(true);
        } else if (arg.equals("false") || arg.equals("disable") || arg.equals("off")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

}