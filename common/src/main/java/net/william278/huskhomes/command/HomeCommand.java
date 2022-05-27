package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.PrivateHomeList;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
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
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(new User(player)).thenAccept(homes -> {
                switch (homes.size()) {
                    case 0 -> plugin.getLocales().getLocale("error_no_homes_set").ifPresent(player::sendMessage);
                    case 1 -> plugin.getTeleportManager().executeTeleport(player, homes.get(0)).thenAccept(result ->
                            plugin.getTeleportManager().finishTeleport(player, result));
                    default -> {
                        final User user = new User(player.getUuid(), player.getName());
                        plugin.getDatabase().getHomes(user).thenAccept(playerHomes -> {
                            final PrivateHomeList homeList = new PrivateHomeList(homes, user, plugin);
                            plugin.getCache().positionLists.put(user.uuid, homeList);
                            homeList.getDisplay(1).forEach(player::sendMessage);
                        });
                    }
                }
            });
            case 1 -> {
                //todo check if its a public home, process. Support sending a player
                plugin.getTeleportManager().teleportToHome(player, new User(player), args[0]);
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

    }
}
