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
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class SavedPositionCommand<T extends SavedPosition> extends Command implements TabProvider {

    private final Class<T> positionType;
    protected final List<String> arguments;

    protected SavedPositionCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull Class<T> positionType,
                                   @NotNull List<String> arguments, @NotNull HuskHomes plugin) {
        super(name, aliases, "<name>" + ((arguments.size() > 0) ? " [" + String.join("|", arguments) + "]" : ""), plugin);
        this.positionType = positionType;
        this.arguments = arguments;

        addAdditionalPermissions(Map.of("other", true));
    }

    @NotNull
    public String getOtherPermission() {
        return (positionType == Home.class ? super.getPermission("other") : super.getPermission());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<String> name = parseStringArg(args, 0);
        if (name.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Optional<?> position = (positionType == Home.class
                ? resolveHome(executor, name.get())
                : resolveWarp(executor, name.get()));
        position.ifPresent(p -> execute(executor, (T) p, removeFirstArg(args)));
    }

    public abstract void execute(@NotNull CommandUser executor, @NotNull T position, @NotNull String[] arguments);

    private Optional<Home> resolveHome(@NotNull CommandUser executor, @NotNull String homeName) {
        if (homeName.contains(Home.IDENTIFIER_DELIMITER)) {
            final String ownerUsername = homeName.substring(0, homeName.indexOf(Home.IDENTIFIER_DELIMITER));
            final String ownerHomeName = homeName.substring(homeName.indexOf(Home.IDENTIFIER_DELIMITER) + 1);
            if (ownerUsername.isBlank() || ownerHomeName.isBlank()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return Optional.empty();
            }

            final Optional<Home> optionalHome = plugin.getDatabase().getUserDataByName(ownerUsername)
                    .flatMap(owner -> resolveHomeByName(owner.getUser(), ownerHomeName));
            if (optionalHome.isEmpty()) {
                plugin.getLocales().getLocale(executor.hasPermission(getOtherPermission())
                                ? "error_home_invalid_other" : "error_public_home_invalid", ownerUsername, ownerHomeName)
                        .ifPresent(executor::sendMessage);
                return Optional.empty();
            }

            final Home home = optionalHome.get();
            if (executor instanceof OnlineUser user && !home.isPublic() && !user.equals(home.getOwner())
                    && !user.hasPermission(getOtherPermission())) {
                plugin.getLocales().getLocale("error_public_home_invalid", ownerUsername, ownerHomeName)
                        .ifPresent(executor::sendMessage);
                return Optional.empty();
            }

            return optionalHome;
        } else if (executor instanceof OnlineUser owner) {
            final Optional<Home> optionalHome = resolveHomeByName(owner, homeName);
            if (optionalHome.isEmpty()) {
                plugin.getLocales().getLocale("error_home_invalid", homeName)
                        .ifPresent(executor::sendMessage);
                return Optional.empty();
            }
            return optionalHome;
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
    }

    private Optional<Home> resolveHomeByName(@NotNull User owner, @NotNull String homeName) {
        return plugin.getDatabase()
                .getHome(owner, homeName)
                .or(() -> {
                    try {
                        return plugin.getDatabase().getHome(UUID.fromString(homeName));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }

    private Optional<Warp> resolveWarp(@NotNull CommandUser executor, @NotNull String warpName) {
        final Optional<Warp> warp = resolveWarpByName(warpName);
        if (warp.isEmpty()) {
            plugin.getLocales().getLocale("error_warp_invalid", warpName)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        if (warp.isPresent() && executor instanceof OnlineUser user && plugin.getSettings().doPermissionRestrictWarps()
                && (!user.hasPermission(Warp.getWildcardPermission()) && !user.hasPermission(Warp.getPermission(warpName)))) {
            plugin.getLocales().getLocale("error_warp_invalid", warpName)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        return warp;
    }

    private Optional<Warp> resolveWarpByName(@NotNull String warpName) {
        return plugin.getDatabase().getWarps().stream().findAny()
                        .filter(warp -> warp.getName().equalsIgnoreCase(warpName))
                .or(() -> {
                    try {
                        return plugin.getDatabase().getWarp(UUID.fromString(warpName));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }

    protected void teleport(@NotNull CommandUser executor, @NotNull Teleportable teleporter, @NotNull T position) {
        if (!teleporter.equals(executor) && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final TeleportBuilder builder = Teleport.builder(plugin)
                .teleporter(teleporter)
                .target(position);
        try {
            if (executor.equals(teleporter)) {
                builder.toTimedTeleport().execute();
            } else {
                builder.toTeleport().execute();
            }
        } catch (TeleportationException e) {
            e.displayMessage(executor, plugin, new String[0]);
        }
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser executor, @NotNull String[] args) {
        if (positionType == Home.class) {
            return switch (args.length) {
                case 0, 1 -> {
                    if (args.length == 1 && args[0].contains(Home.IDENTIFIER_DELIMITER)) {
                        if (executor.hasPermission(getOtherPermission())) {
                            yield filter(plugin.getManager().homes().getUserHomeNames(), args);
                        }
                        yield filter(plugin.getManager().homes().getUserHomeNames(), args);
                    }
                    if (executor instanceof OnlineUser user) {
                        yield filter(plugin.getManager().homes().getUserHomes().get(user.getUsername()), args);
                    }
                    yield filter(plugin.getManager().homes().getUserHomeNames(), args);
                }
                case 2 -> filter(arguments.stream().toList(), args);
                default -> List.of();
            };
        } else {
            return switch (args.length) {
                case 0, 1 -> filter(plugin.getManager().warps().getUsableWarps(executor), args);
                case 2 -> filter(arguments.stream().toList(), args);
                default -> List.of();
            };
        }
    }

}
