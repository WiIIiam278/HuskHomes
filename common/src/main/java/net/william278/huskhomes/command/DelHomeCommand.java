package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DelHomeCommand extends SavedPositionCommand<Home> {

    public DelHomeCommand(@NotNull HuskHomes implementor) {
        super("delhome", List.of(), Home.class, List.of(), implementor);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (executor instanceof OnlineUser user && handleDeleteAll(user, args)) {
            return;
        }
        super.execute(executor, args);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Home home, @NotNull String[] args) {
        if (executor instanceof OnlineUser user && !home.getOwner().equals(user) && !user.hasPermission(getOtherPermission())) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(user::sendMessage);
            return;
        }
        try {
            plugin.getManager().homes().deleteHome(home);
        } catch (ValidationException e) {
            e.dispatchHomeError(executor, !home.getOwner().equals(executor), plugin, home.getName());
            return;
        }
        plugin.getLocales().getLocale("home_deleted", home.getName())
                .ifPresent(executor::sendMessage);
    }

    private boolean handleDeleteAll(@NotNull OnlineUser user, @NotNull String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
            if (!parseStringArg(args, 1)
                    .map(confirm -> confirm.equalsIgnoreCase("confirm"))
                    .orElse(false)) {
                plugin.getLocales().getLocale("delete_all_homes_confirm")
                        .ifPresent(user::sendMessage);
                return true;
            }

            final int deleted = plugin.getManager().homes().deleteAllHomes(user);
            if (deleted == 0) {
                plugin.getLocales().getLocale("error_no_homes_set")
                        .ifPresent(user::sendMessage);
                return true;
            }

            plugin.getLocales().getLocale("delete_all_homes_success", Integer.toString(deleted))
                    .ifPresent(user::sendMessage);
            return true;
        }
        return false;
    }

}
