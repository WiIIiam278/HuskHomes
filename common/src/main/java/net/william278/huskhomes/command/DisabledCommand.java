package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class DisabledCommand extends Command {

    public DisabledCommand(@NotNull HuskHomes implementor) {
        super("", Permission.COMMAND_DISABLED_MESSAGE, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        plugin.getLocales().getLocale("error_command_disabled").ifPresent(onlineUser::sendMessage);
    }
}
