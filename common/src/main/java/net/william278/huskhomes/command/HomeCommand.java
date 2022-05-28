package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.PrivateHomeList;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HomeCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    private final static String PERMISSION = "huskhomes.command.home";

    public HomeCommand(@NotNull HuskHomes implementor) {
        super("home", PERMISSION, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        final User user = new User(player);
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(user).thenAccept(homes -> {
                switch (homes.size()) {
                    case 0 -> plugin.getLocales().getLocale("error_no_homes_set").ifPresent(player::sendMessage);
                    case 1 -> plugin.getTeleportManager().teleport(player, homes.get(0)).thenAccept(result ->
                            plugin.getTeleportManager().finishTeleport(player, result));
                    default -> {
                        final PrivateHomeList homeList = new PrivateHomeList(homes, user, plugin);
                        plugin.getCache().positionLists.put(user.uuid, homeList);
                        homeList.getDisplay(1).forEach(player::sendMessage);
                    }
                }
            });
            case 1 -> {
                final String homeName = args[0];
                if (RegexUtil.OWNER_NAME_PATTERN.matcher(homeName).matches()) {
                    final String ownerName = homeName.split("\\.")[0];
                    final String ownersHomeName = homeName.split("\\.")[1];
                    plugin.getDatabase().getUserByName(ownerName).thenAccept(optionalUserData -> optionalUserData
                            .ifPresentOrElse(userData -> plugin.getTeleportManager().teleportToHome(player, userData, ownersHomeName),
                                    () -> plugin.getLocales().getLocale("error_home_invalid_other", ownerName, homeName)
                                            .ifPresent(player::sendMessage)));
                } else {
                    plugin.getTeleportManager().teleportToHome(player, user, homeName);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/home [name]")
                    .ifPresent(player::sendMessage);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull Player player, @NotNull String[] args) {
        return plugin.getCache().homes.get(player.getUuid()).stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
