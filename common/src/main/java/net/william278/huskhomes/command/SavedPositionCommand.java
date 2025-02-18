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
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class SavedPositionCommand<T extends SavedPosition> extends Command implements TabCompletable {

    protected final List<String> arguments;
    protected final PositionCommandType positionType;

    protected SavedPositionCommand(@NotNull List<String> aliases,
                                   @NotNull PositionCommandType positionType,
                                   @NotNull List<String> arguments, @NotNull HuskHomes plugin) {
        super(aliases, "<name>" + formatUsage(arguments), plugin);
        this.positionType = positionType;
        this.arguments = arguments;

        addAdditionalPermissions(Map.of("other", true));
        addAdditionalPermissions(arguments.stream().collect(HashMap::new, (m, s) -> m.put(s, false), HashMap::putAll));
    }

    @NotNull
    public String getOtherPermission() {
        if (positionType != PositionCommandType.WARP) {
            return super.getPermission("other");
        }
        return super.getPermission("warp");
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

        // Resolve and execute
        final Optional<?> position = (positionType == PositionCommandType.WARP
                ? resolveWarp(executor, name.get()) // Resolve warps
                : resolveHome(executor, name.get())); // Resolve homes & public homes
        position.ifPresent(p -> execute(executor, (T) p, removeFirstArg(args)));
    }

    public abstract void execute(@NotNull CommandUser executor, @NotNull T position, @NotNull String[] arguments);

    private Optional<Home> resolveHome(@NotNull CommandUser executor, @NotNull String homeName) {
        if (homeName.contains(Home.IDENTIFIER_DELIMITER)) {
            return resolveDelimitedHome(executor, homeName);
        } else if (positionType == PositionCommandType.PUBLIC_HOME) {
            return resolvePublicHome(executor, homeName);
        } else if (executor instanceof OnlineUser owner) {
            return resolveOwnerHome(owner, homeName);
        }
        plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                .ifPresent(executor::sendMessage);
        return Optional.empty();
    }

    private Optional<Home> resolveDelimitedHome(@NotNull CommandUser executor, @NotNull String homeName) {
        final String ownerUsername = homeName.substring(0, homeName.indexOf(Home.IDENTIFIER_DELIMITER));
        final String ownerHome = homeName.substring(homeName.indexOf(Home.IDENTIFIER_DELIMITER) + 1);
        if (ownerUsername.isBlank() || ownerHome.isBlank()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        final Optional<Home> optionalHome = plugin.getDatabase().getUser(ownerUsername)
                .flatMap(owner -> resolveHomeByName(owner.getUser(), ownerHome));
        if (optionalHome.isEmpty()) {
            plugin.getLocales().getLocale(executor.hasPermission(getOtherPermission())
                            ? "error_home_invalid_other" : "error_public_home_invalid", ownerUsername, ownerHome)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        final Home home = optionalHome.get();
        if (executor instanceof OnlineUser user && !home.isPublic() && !user.equals(home.getOwner())
                && !user.hasPermission(getOtherPermission())) {
            plugin.getLocales().getLocale("error_public_home_invalid", ownerUsername, ownerHome)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        return optionalHome;
    }

    private Optional<Home> resolvePublicHome(@NotNull CommandUser executor, @NotNull String homeName) {
        final List<Home> publicHomes = plugin.getDatabase().getPublicHomes(homeName);
        if (publicHomes.isEmpty()) {
            plugin.getLocales().getLocale("error_unknown_public_home", homeName)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        if (publicHomes.size() > 1) {
            plugin.getCommand(PublicHomeListCommand.class)
                    .ifPresent(command -> command.showPublicHomeList(executor, homeName, 1));
            return Optional.empty();
        }
        return publicHomes.stream().findFirst();
    }

    private Optional<Home> resolveOwnerHome(@NotNull OnlineUser owner, @NotNull String homeName) {
        final Optional<Home> optionalHome = resolveHomeByName(owner, homeName);
        if (optionalHome.isEmpty()) {
            plugin.getLocales().getLocale("error_home_invalid", homeName)
                    .ifPresent(owner::sendMessage);
            return Optional.empty();
        }
        return optionalHome;
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
        if (executor instanceof OnlineUser user
                && plugin.getSettings().getGeneral().isPermissionRestrictWarps()
                && !Warp.hasPermission(user, warpName)) {
            plugin.getLocales().getLocale("error_warp_invalid", warpName)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        return warp;
    }

    private Optional<Warp> resolveWarpByName(@NotNull String warpName) {
        return plugin.getDatabase().getWarp(warpName)
                .or(() -> {
                    try {
                        return plugin.getDatabase().getWarp(UUID.fromString(warpName));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }

    protected void teleport(@NotNull CommandUser executor, @NotNull Teleportable teleporter, @NotNull T position,
                            @NotNull TransactionResolver.Action... actions) {
        if (!teleporter.equals(executor) && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        Teleport.builder(plugin)
                .teleporter(teleporter)
                .actions(actions)
                .target(position)
                .buildAndComplete(executor.equals(teleporter), teleporter.getName());
    }

    protected boolean isInvalidOperation(String operation, CommandUser executor) {
        if (!arguments.contains(operation.toLowerCase())) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return true;
        }
        if (!executor.hasPermission(getPermission(operation.toLowerCase(Locale.ENGLISH)))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return true;
        }
        return false;
    }

    @NotNull
    private static String formatUsage(Collection<String> arguments) {
        return ((!arguments.isEmpty()) ? " [" + String.join("|", arguments) + "]" : "");
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser executor, @NotNull String[] args) {
        return switch (positionType) {
            case HOME, PUBLIC_HOME -> suggestHome(executor, args);
            case WARP -> suggestWarp(executor, args);
        };
    }

    @NotNull
    private List<String> suggestWarp(@NotNull CommandUser executor, @NotNull String[] args) {
        return switch (args.length) {
            case 0, 1 -> plugin.getManager().warps().getUsableWarps(executor);
            case 2 -> arguments.stream().filter(a -> executor.hasPermission(getPermission(a))).toList();
            default -> List.of();
        };
    }

    @NotNull
    private List<String> suggestHome(@NotNull CommandUser executor, @NotNull String[] args) {
        return switch (args.length) {
            case 0, 1 -> {
                if (args.length == 1 && args[0].contains(Home.IDENTIFIER_DELIMITER)
                        && executor.hasPermission(getOtherPermission())) {
                    yield plugin.getManager().homes().getUserHomeIdentifiers();
                }
                if (executor instanceof OnlineUser user) {
                    yield plugin.getManager().homes().getUserHomes().get(user.getName());
                }
                yield plugin.getManager().homes().getUserHomeIdentifiers();
            }
            case 2 -> arguments.stream().filter(a -> executor.hasPermission(getPermission(a))).toList();
            default -> List.of();
        };
    }

    protected enum PositionCommandType {
        HOME,
        PUBLIC_HOME,
        WARP,
    }

}
