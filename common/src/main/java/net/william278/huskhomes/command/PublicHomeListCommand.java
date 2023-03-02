package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PublicHomeListCommand extends CommandBase implements ConsoleExecutable {

    protected PublicHomeListCommand(@NotNull HuskHomes implementor) {
        super("publichomelist", Permission.COMMAND_HOME, implementor, "phomelist", "phomes");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> showPublicHomeList(onlineUser, 1);
            case 1 -> {
                try {
                    int pageNumber = Integer.parseInt(args[0]);
                    showPublicHomeList(onlineUser, pageNumber);
                } catch (NumberFormatException e) {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/publichomelist [page]")
                            .ifPresent(onlineUser::sendMessage);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/publichomelist [page]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Show a (cached) list of public homes
     *
     * @param onlineUser the user to display the homes to
     * @param pageNumber page number to display
     */
    private void showPublicHomeList(@NotNull OnlineUser onlineUser, int pageNumber) {
        if (plugin.getCache().getPublicHomeLists().containsKey(onlineUser.getUuid())) {
            onlineUser.sendMessage(plugin.getCache().getPublicHomeLists().get(onlineUser.getUuid()).getNearestValidPage(pageNumber));
            return;
        }

        plugin.getDatabase().getPublicHomes().thenAcceptAsync(publicHomes -> {
            if (publicHomes.isEmpty()) {
                plugin.getLocales().getLocale("error_no_public_homes_set").ifPresent(onlineUser::sendMessage);
                return;
            }
            plugin.getCache().getPublicHomeList(onlineUser,
                            plugin.getLocales(), publicHomes,
                            plugin.getSettings().getListItemsPerPage(), pageNumber)
                    .ifPresent(onlineUser::sendMessage);
        });

    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        CompletableFuture.runAsync(() -> {
            final List<Home> homes = plugin.getDatabase().getPublicHomes().join();
            StringJoiner rowJoiner = new StringJoiner("\t");

            plugin.getLoggingAdapter().log(Level.INFO, "List of " + homes.size() + " public homes:");
            for (int i = 0; i < homes.size(); i++) {
                final String ownerUsername = homes.get(i).getOwner().getUsername();
                final String homeName = homes.get(i).getMeta().getName();
                final String home = ownerUsername + "." + homeName;
                int spacingSize = (16 - ownerUsername.length()) + 17;
                rowJoiner.add(home.length() < spacingSize ? home + " ".repeat(spacingSize - home.length()) : home);
                if ((i + 1) % 3 == 0) {
                    plugin.getLoggingAdapter().log(Level.INFO, rowJoiner.toString());
                    rowJoiner = new StringJoiner("\t");
                }
            }
            plugin.getLoggingAdapter().log(Level.INFO, rowJoiner.toString());
        });
    }
}
