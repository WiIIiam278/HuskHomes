package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HomeCommand extends Command implements TabProvider, ConsoleExecutable {

    protected HomeCommand(@NotNull HuskHomes implementor) {
        super("home", Permission.COMMAND_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(onlineUser).thenAcceptAsync(homes -> {
                // Send the home list if they have homes set. If they have just one home set, teleport the player
                switch (homes.size()) {
                    case 0 -> plugin.getLocales().getLocale("error_no_homes_set").ifPresent(onlineUser::sendMessage);
                    case 1 -> Teleport.builder(plugin, onlineUser)
                            .setTarget(homes.get(0))
                            .toTimedTeleport().thenAccept(TimedTeleport::execute);
                    default -> plugin.getCache().getHomeList(onlineUser, onlineUser,
                                    plugin.getLocales(), homes, plugin.getSettings().getListItemsPerPage(), 1)
                            .ifPresent(onlineUser::sendMessage);
                }
            });
            case 1 -> {
                // Parse the home name input and teleport the player to the home
                final String homeName = args[0];
                RegexUtil.matchDisambiguatedHomeIdentifier(homeName).ifPresentOrElse(
                        homeIdentifier -> plugin.getDatabase().getUserDataByName(homeIdentifier.ownerName())
                                .thenAccept(optionalUserData -> optionalUserData.ifPresentOrElse(
                                        userData -> teleportToNamedHome(onlineUser, userData.user(), homeIdentifier.homeName()),
                                        () -> plugin.getLocales().getLocale("error_home_invalid_other", homeIdentifier.ownerName(), homeIdentifier.homeName())
                                                .ifPresent(onlineUser::sendMessage))),
                        () -> teleportToNamedHome(onlineUser, onlineUser, homeName));
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/home [name]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    private void teleportToNamedHome(@NotNull OnlineUser teleporter, @NotNull User owner, @NotNull String homeName) {
        final boolean otherHome = !owner.equals(teleporter);
        plugin.getDatabase()
                .getHome(owner, homeName)
                .thenAccept(homeResult -> homeResult.ifPresentOrElse(home -> {
                    if (otherHome && !home.isPublic) {
                        if (!teleporter.hasPermission(Permission.COMMAND_HOME_OTHER.node)) {
                            plugin.getLocales().getLocale("error_no_permission")
                                    .ifPresent(teleporter::sendMessage);
                            return;
                        }
                    }
                    Teleport.builder(plugin, teleporter)
                            .setTarget(home)
                            .toTimedTeleport()
                            .thenAccept(TimedTeleport::execute);
                }, () -> {
                    if (otherHome) {
                        plugin.getLocales().getLocale("error_home_invalid_other", owner.getUsername(), homeName)
                                .ifPresent(teleporter::sendMessage);
                    } else {
                        plugin.getLocales().getLocale("error_home_invalid", homeName)
                                .ifPresent(teleporter::sendMessage);
                    }
                }));
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        if (args.length != 2) {
            plugin.getLoggingAdapter().log(Level.WARNING, "Invalid syntax. Usage: home <player> <home>");
            return;
        }
        CompletableFuture.runAsync(() -> {
            final OnlineUser playerToTeleport = plugin.findOnlinePlayer(args[0]).orElse(null);
            if (playerToTeleport == null) {
                plugin.getLoggingAdapter().log(Level.WARNING, "Player not found: " + args[0]);
                return;
            }
            final AtomicReference<Home> matchedHome = new AtomicReference<>(null);
            RegexUtil.matchDisambiguatedHomeIdentifier(args[1]).ifPresentOrElse(
                    identifier -> matchedHome.set(plugin.getDatabase().getUserDataByName(identifier.ownerName()).join()
                            .flatMap(user -> plugin.getDatabase().getHome(user.user(), identifier.homeName()).join())
                            .orElse(null)),
                    () -> matchedHome.set(plugin.getDatabase().getUserDataByName(playerToTeleport.getUsername()).join()
                            .flatMap(user -> plugin.getDatabase().getHome(user.user(), args[1]).join())
                            .orElse(null)));

            final Home home = matchedHome.get();
            if (home == null) {
                plugin.getLoggingAdapter().log(Level.WARNING, "Could not find home '" + args[1] + "'");
                return;
            }

            plugin.getLoggingAdapter().log(Level.INFO, "Teleporting " + playerToTeleport.getUsername() + " to "
                                                       + home.getOwner().getUsername() + "." + home.getMeta().getName());
            Teleport.builder(plugin, playerToTeleport)
                    .setTarget(home)
                    .toTeleport()
                    .thenAccept(Teleport::execute);
        });
    }

    @Override
    public @NotNull List<String> suggest(@NotNull String[] args, @Nullable OnlineUser user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return args.length > 1 ? Collections.emptyList() : plugin.getCache().getHomes()
                .getOrDefault(user.getUuid(), new ArrayList<>())
                .stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted()
                .collect(Collectors.toList());
    }
}
