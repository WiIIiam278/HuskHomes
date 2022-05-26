package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HomeCommand extends CommandBase {

    private final static String PERMISSION = "huskhomes.command.home";

    public HomeCommand(@NotNull HuskHomes implementor) {
        super("home", PERMISSION, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        if (args.length >= 1) {
            //todo check if its a public home, process. Support sending a player
            plugin.getTeleportManager().teleportToHome(player, new User(player), args[0]);
        } else {
            plugin.getDatabase().getHomes(new User(player)).thenAccept(homes -> {
                switch (homes.size()) {
                    case 0 -> plugin.getLocales().getLocale("error_no_homes_set").ifPresent(player::sendMessage);
                    case 1 -> plugin.getTeleportManager().executeTeleport(player, homes.get(0)).thenAccept(result ->
                            plugin.getTeleportManager().finishTeleport(player, result));
                    default -> {
                        //todo show home list
                    }
                }
            });
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull Player player, @NotNull String[] args) {
        return plugin.getCache().homes.get(player.getUuid()).stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
