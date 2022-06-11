package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
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
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        final User user = new User(player);
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(user).thenAccept(homes -> {
                if (homes.size() == 1) {
                    homes.stream().findFirst().ifPresent(home -> plugin.getSavedPositionManager().deleteHome(user, home.meta.name));
                } else {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                            .ifPresent(player::sendMessage);
                }
            });
            case 1 -> {
                final String homeName = args[0];
                if (RegexUtil.OWNER_NAME_PATTERN.matcher(homeName).matches()) {
                    final String ownerName = homeName.split("\\.")[0];
                    final String ownersHomeName = homeName.split("\\.")[1];
                    plugin.getDatabase().getUserByName(ownerName).thenAccept(optionalUserData -> optionalUserData
                            .ifPresentOrElse(userData -> {
                                        if (!userData.uuid.equals(player.getUuid())) {
                                            if (!player.hasPermission(Permission.COMMAND_DELETE_HOME_OTHER.node)) {
                                                plugin.getLocales().getLocale("error_no_permission")
                                                        .ifPresent(player::sendMessage);
                                                return;
                                            }
                                        }
                                        plugin.getSavedPositionManager().deleteHome(userData, ownersHomeName);
                                    },
                                    () -> plugin.getLocales().getLocale("error_home_invalid_other", ownerName, homeName)
                                            .ifPresent(player::sendMessage)));
                } else {
                    plugin.getSavedPositionManager().deleteHome(user, homeName);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/delhome <name>")
                    .ifPresent(player::sendMessage);
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {

    }

    @Override
    public List<String> onTabComplete(@NotNull Player player, @NotNull String[] args) {
        return plugin.getCache().homes.get(player.getUuid()).stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
