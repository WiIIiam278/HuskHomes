package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class SavedPositionCommand<T extends SavedPosition> extends Command {

    private final Class<T> positionType;
    protected final List<String> arguments;

    protected SavedPositionCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull Class<T> positionType,
                                   @NotNull List<String> arguments, @NotNull HuskHomes plugin) {
        super(name, aliases, "<name>" + ((arguments.size() > 0) ? " [" + String.join("|", arguments) + "]" : ""), plugin);
        this.positionType = positionType;
        this.arguments = arguments;
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
        if (homeName.contains(".")) {
            final String[] splitHomeName = homeName.split("\\.");
            final Optional<Home> optionalHome = plugin.getDatabase().getUserDataByName(splitHomeName[0])
                    .flatMap(owner -> plugin.getDatabase().getHome(owner.getUser(), splitHomeName[1]));

            if (optionalHome.isEmpty()) {
                plugin.getLocales().getLocale(executor.hasPermission(getOtherPermission())
                                        ? "error_home_invalid_other" : "error_public_home_invalid",
                                splitHomeName[0], splitHomeName[1])
                        .ifPresent(executor::sendMessage);
                return Optional.empty();
            }

            final Home home = optionalHome.get();
            if (executor instanceof OnlineUser user && !home.isPublic() && !user.equals(home.getOwner())
                && !user.hasPermission(getOtherPermission())) {
                plugin.getLocales().getLocale("error_public_home_invalid",
                                splitHomeName[0], splitHomeName[1])
                        .ifPresent(executor::sendMessage);
                return Optional.empty();
            }

            return optionalHome;
        } else if (executor instanceof OnlineUser owner) {
            final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, homeName);
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

    private Optional<Warp> resolveWarp(@NotNull CommandUser executor, @NotNull String warpName) {
        final Optional<Warp> warp = plugin.getDatabase().getWarp(warpName);
        if (warp.isPresent() && executor instanceof OnlineUser user && plugin.getSettings().isPermissionRestrictWarps()
            && (!user.hasPermission(Warp.getWildcardPermission()) && !user.hasPermission(Warp.getPermission(warpName)))) {
            plugin.getLocales().getLocale("error_warp_invalid", warpName)
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }
        return warp;
    }

    protected void teleport(@NotNull CommandUser executor, @NotNull Teleportable teleporter, @NotNull T position) {
        if (!teleporter.equals(executor) && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        Teleport.builder(plugin)
                .teleporter(teleporter)
                .target(position)
                .toTimedTeleport()
                .execute();
    }

    @Override
    @NotNull
    public Map<String, Boolean> getAdditionalPermissions() {
        return Map.of(getOtherPermission(), true);
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser executor, @NotNull String[] args) {
        if (positionType == Home.class) {
            return switch (args.length) {
                case 0, 1 -> {
                    if (args.length == 1 && args[0].contains(".")) {
                        if (executor.hasPermission(getOtherPermission())) {
                            yield filter(reduceHomeList(plugin.getCache().getHomes()), args);
                        }
                        yield filter(reduceHomeList(plugin.getCache().getHomes()), args);
                    }
                    if (executor instanceof OnlineUser user) {
                        yield filter(plugin.getCache().getHomes().get(user.getUsername()), args);
                    }
                    yield filter(reduceHomeList(plugin.getCache().getHomes()), args);
                }
                case 2 -> filter(arguments.stream().toList(), args);
                default -> List.of();
            };
        } else {
            return switch (args.length) {
                case 0, 1 -> filter(plugin.getCache().getWarps(), args);
                case 2 -> filter(arguments.stream().toList(), args);
                default -> List.of();
            };
        }
    }

    @NotNull
    protected static List<String> reduceHomeList(@NotNull Map<String, List<String>> cachedMap) {
        final List<String> homeNames = new ArrayList<>();
        cachedMap.forEach((key, value) -> value.forEach(home -> homeNames.add(key + "." + home)));
        return homeNames;
    }

}
