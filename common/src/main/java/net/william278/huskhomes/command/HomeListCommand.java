package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.PrivateHomeList;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class HomeListCommand extends CommandBase implements ConsoleExecutable {

    public HomeListCommand(@NotNull HuskHomes implementor) {
        super("homelist", Permission.COMMAND_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> showHomeList(player, 1);
            case 1 -> {
                try {
                    int pageNumber = Integer.parseInt(args[0]);
                    showHomeList(player, pageNumber);
                } catch (NumberFormatException e) {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/homelist [page]")
                            .ifPresent(player::sendMessage);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/homelist [page]")
                    .ifPresent(player::sendMessage);
        }
    }

    /**
     * Show a (cached) list of a {@link Player}'s homes
     *
     * @param player     the user to display the homes of
     * @param pageNumber page number to display
     */
    private void showHomeList(@NotNull Player player, int pageNumber) {
        if (plugin.getCache().positionLists.containsKey(player.getUuid())) {
            if (plugin.getCache().positionLists.get(player.getUuid()) instanceof PrivateHomeList privateHomeList) {
                privateHomeList.getDisplay(pageNumber).forEach(player::sendMessage);
                return;
            }
        }
        final User user = new User(player);
        plugin.getDatabase().getHomes(user).thenAccept(homes -> {
            if (homes.isEmpty()) {
                plugin.getLocales().getLocale("error_no_homes_set").ifPresent(player::sendMessage);
                return;
            }
            final PrivateHomeList homeList = new PrivateHomeList(homes, user, plugin);
            plugin.getCache().positionLists.put(user.uuid, homeList);
            homeList.getDisplay(pageNumber).forEach(player::sendMessage);
        });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
