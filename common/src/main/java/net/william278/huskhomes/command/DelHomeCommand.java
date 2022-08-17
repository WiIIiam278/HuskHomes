package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DelHomeCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    public DelHomeCommand(@NotNull HuskHomes implementor) {
        super("delhome", Permission.COMMAND_DELETE_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {

        switch (args.length) { //todo delhome all
            case 0 -> plugin.getDatabase().getHomes(onlineUser).thenAccept(homes -> {
                if (homes.size() == 1) {
                    homes.stream().findFirst().ifPresent(home -> deletePlayerHome(onlineUser, onlineUser, home.meta.name));
                } else {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                            .ifPresent(onlineUser::sendMessage);
                }
            });
            case 1 -> {
                final String homeName = args[0];
                RegexUtil.matchDisambiguatedHomeIdentifier(homeName).ifPresentOrElse(homeIdentifier ->
                                plugin.getDatabase().getUserDataByName(homeIdentifier.ownerName()).thenAccept(
                                        optionalUserData -> optionalUserData.ifPresentOrElse(userData -> {
                                                    if (!userData.getUserUuid().equals(onlineUser.uuid)) {
                                                        if (!onlineUser.hasPermission(Permission.COMMAND_DELETE_HOME_OTHER.node)) {
                                                            plugin.getLocales().getLocale("error_no_permission")
                                                                    .ifPresent(onlineUser::sendMessage);
                                                            return;
                                                        }
                                                    }
                                                    deletePlayerHome(onlineUser, userData.user(), homeIdentifier.homeName());
                                                },
                                                () -> plugin.getLocales().getLocale("error_home_invalid_other",
                                                        homeIdentifier.ownerName(), homeIdentifier.homeName()).ifPresent(onlineUser::sendMessage))),
                        () -> deletePlayerHome(onlineUser, onlineUser, homeName));
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Delete a player's home
     *
     * @param deleter   the player who is deleting the home
     * @param homeOwner The player who owns the home
     * @param homeName  The home name to delete
     */
    private void deletePlayerHome(@NotNull OnlineUser deleter, @NotNull User homeOwner, @NotNull String homeName) {
        plugin.getSavedPositionManager().deleteHome(homeOwner, homeName).thenAccept(deleted -> {
            if (deleter.uuid.equals(homeOwner.uuid)) {
                if (deleted) {
                    plugin.getLocales().getLocale("home_deleted", homeName).ifPresent(deleter::sendMessage);
                } else {
                    plugin.getLocales().getLocale("error_home_invalid", homeName).ifPresent(deleter::sendMessage);
                }
            } else {
                if (deleted) {
                    plugin.getLocales().getLocale("home_deleted_other", homeOwner.username, homeName).ifPresent(deleter::sendMessage);
                } else {
                    plugin.getLocales().getLocale("error_home_invalid_other", homeOwner.username, homeName).ifPresent(deleter::sendMessage);
                }
            }
        });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {

    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return args.length > 1 ? Collections.emptyList() : plugin.getCache().homes.get(onlineUser.uuid).stream()
                .filter(s -> s.startsWith(args.length == 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
