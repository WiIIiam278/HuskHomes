package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DelHomeCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    public DelHomeCommand(@NotNull HuskHomes implementor) {
        super("delhome", Permission.COMMAND_DELETE_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(onlineUser).thenAccept(homes -> {
                if (homes.size() == 1) {
                    homes.stream().findFirst().ifPresent(home -> plugin.getSavedPositionManager().deleteHome(onlineUser, home.meta.name));
                } else {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                            .ifPresent(onlineUser::sendMessage);
                }
            });
            case 1 -> {
                final String homeName = args[0];
                if (RegexUtil.OWNER_NAME_PATTERN.matcher(homeName).matches()) {
                    final String ownerName = homeName.split("\\.")[0];
                    final String ownersHomeName = homeName.split("\\.")[1];
                    plugin.getDatabase().getUserDataByName(ownerName).thenAccept(optionalUserData -> optionalUserData
                            .ifPresentOrElse(userData -> {
                                        if (!userData.getUserUuid().equals(onlineUser.uuid)) {
                                            if (!onlineUser.hasPermission(Permission.COMMAND_DELETE_HOME_OTHER.node)) {
                                                plugin.getLocales().getLocale("error_no_permission")
                                                        .ifPresent(onlineUser::sendMessage);
                                                return;
                                            }
                                        }
                                        plugin.getSavedPositionManager().deleteHome(userData.user(), ownersHomeName);
                                    },
                                    () -> plugin.getLocales().getLocale("error_home_invalid_other", ownerName, homeName)
                                            .ifPresent(onlineUser::sendMessage)));
                } else {
                    plugin.getSavedPositionManager().deleteHome(onlineUser, homeName);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {

    }

    @Override
    public List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return plugin.getCache().homes.get(onlineUser.uuid).stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
