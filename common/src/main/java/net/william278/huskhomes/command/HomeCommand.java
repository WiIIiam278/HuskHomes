package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.PrivateHomeList;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HomeCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    public HomeCommand(@NotNull HuskHomes implementor) {
        super("home", Permission.COMMAND_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(onlineUser).thenAccept(homes -> {
                switch (homes.size()) {
                    case 0 -> plugin.getLocales().getLocale("error_no_homes_set").ifPresent(onlineUser::sendMessage);
                    case 1 -> plugin.getTeleportManager().teleport(onlineUser, homes.get(0)).thenAccept(result ->
                            plugin.getTeleportManager().finishTeleport(onlineUser, result));
                    default -> {
                        final PrivateHomeList homeList = new PrivateHomeList(homes, onlineUser, plugin);
                        plugin.getCache().positionLists.put(onlineUser.uuid, homeList);
                        homeList.getDisplay(1).forEach(onlineUser::sendMessage);
                    }
                }
            });
            case 1 -> {
                final String homeName = args[0];
                if (RegexUtil.OWNER_NAME_PATTERN.matcher(homeName).matches()) {
                    final String ownerName = homeName.split("\\.")[0];
                    final String ownersHomeName = homeName.split("\\.")[1];
                    plugin.getDatabase().getUserByName(ownerName).thenAccept(optionalUserData -> optionalUserData
                            .ifPresentOrElse(userData -> plugin.getTeleportManager().teleportToHome(onlineUser, userData, ownersHomeName),
                                    () -> plugin.getLocales().getLocale("error_home_invalid_other", ownerName, homeName)
                                            .ifPresent(onlineUser::sendMessage)));
                } else {
                    plugin.getTeleportManager().teleportToHome(onlineUser, onlineUser, homeName);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/home [name]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }

    @Override
    public List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return plugin.getCache().homes.get(onlineUser.uuid).stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
