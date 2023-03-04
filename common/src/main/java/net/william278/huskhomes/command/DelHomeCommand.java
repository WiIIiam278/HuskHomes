package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DelHomeCommand extends CommandBase implements TabCompletable {

    protected DelHomeCommand(@NotNull HuskHomes implementor) {
        super("delhome", Permission.COMMAND_DELETE_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length == 0) {
            plugin.getDatabase().getHomes(onlineUser).thenAccept(homes -> {
                if (homes.size() == 1) {
                    homes.stream().findFirst().ifPresent(home -> deletePlayerHome(onlineUser, onlineUser, home.meta.name, false));
                } else {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                            .ifPresent(onlineUser::sendMessage);
                }
            });
            return;
        }

        if (args.length <= 2) {
            final String homeName = args[0];
            final boolean confirm = args.length == 2 && args[1].equalsIgnoreCase("confirm");
            RegexUtil.matchDisambiguatedHomeIdentifier(homeName).ifPresentOrElse(homeIdentifier ->
                            plugin.getDatabase().getUserDataByName(homeIdentifier.ownerName()).thenAccept(
                                    optionalUserData -> optionalUserData.ifPresentOrElse(userData -> {
                                                if (!userData.getUserUuid().equals(onlineUser.getUuid())) {
                                                    if (!onlineUser.hasPermission(Permission.COMMAND_DELETE_HOME_OTHER.node)) {
                                                        plugin.getLocales().getLocale("error_no_permission")
                                                                .ifPresent(onlineUser::sendMessage);
                                                        return;
                                                    }
                                                }
                                                deletePlayerHome(onlineUser, userData.user(), homeIdentifier.homeName(), confirm);
                                            },
                                            () -> plugin.getLocales().getLocale("error_home_invalid_other",
                                                    homeIdentifier.ownerName(), homeIdentifier.homeName()).ifPresent(onlineUser::sendMessage))),
                    () -> deletePlayerHome(onlineUser, onlineUser, homeName, confirm));
            return;
        }

        plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                .ifPresent(onlineUser::sendMessage);
    }

    /**
     * Delete a player's home
     *
     * @param deleter           the player who is deleting the home
     * @param homeOwner         The player who owns the home
     * @param homeName          The home name to delete
     * @param delHomeAllConfirm Whether to skip the confirmation prompt for deleting all homes
     */
    private void deletePlayerHome(@NotNull OnlineUser deleter, @NotNull User homeOwner, @NotNull String homeName,
                                  final boolean delHomeAllConfirm) {
        plugin.getManager().deleteHome(homeOwner, homeName).thenAccept(deleted -> {
            if (deleter.equals(homeOwner)) {
                if (deleted) {
                    plugin.getLocales().getLocale("home_deleted", homeName).ifPresent(deleter::sendMessage);
                    return;
                }
                if (homeName.equalsIgnoreCase("all")) {
                    deleteAllHomes(deleter, homeOwner, delHomeAllConfirm);
                    return;
                }
                plugin.getLocales().getLocale("error_home_invalid", homeName).ifPresent(deleter::sendMessage);
            } else {
                if (deleted) {
                    plugin.getLocales().getLocale("home_deleted_other", homeOwner.getUsername(), homeName).ifPresent(deleter::sendMessage);
                    return;
                }
                if (homeName.equalsIgnoreCase("all")) {
                    deleteAllHomes(deleter, homeOwner, delHomeAllConfirm);
                    return;
                }
                plugin.getLocales().getLocale("error_home_invalid_other", homeOwner.getUsername(), homeName).ifPresent(deleter::sendMessage);
            }
        });
    }

    /**
     * Delete all of a player's homes
     *
     * @param deleter   the player who is deleting the homes
     * @param homeOwner the player who owns the homes
     * @param confirm   whether to skip the confirmation prompt
     */
    private void deleteAllHomes(@NotNull OnlineUser deleter, @NotNull User homeOwner,
                                final boolean confirm) {
        if (!confirm) {
            plugin.getLocales().getLocale("delete_all_homes_confirm")
                    .ifPresent(deleter::sendMessage);
            return;
        }

        plugin.getManager().deleteAllHomes(homeOwner).thenAccept(deleted -> {
            if (deleted == 0) {
                plugin.getLocales().getLocale("error_no_warps_set")
                        .ifPresent(deleter::sendMessage);
                return;
            }

            plugin.getLocales().getLocale("delete_all_homes_success", Integer.toString(deleted))
                    .ifPresent(deleter::sendMessage);
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return args.length > 1 ? Collections.emptyList() : plugin.getCache().getHomes()
                .getOrDefault(user.getUuid(), new ArrayList<>())
                .stream()
                .filter(s -> s.startsWith(args.length == 1 ? args[0] : ""))
                .sorted()
                .collect(Collectors.toList());
    }
}
