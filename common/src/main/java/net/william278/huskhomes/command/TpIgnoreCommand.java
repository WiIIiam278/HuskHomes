package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class TpIgnoreCommand extends Command {

    protected TpIgnoreCommand(@NotNull HuskHomes implementor) {
        super("tpignore", Permission.COMMAND_TPIGNORE, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length != 0) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpignore")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Update local value
        final boolean isIgnoringRequests = !plugin.getRequestManager().isIgnoringRequests(onlineUser);
        plugin.getRequestManager().setIgnoringRequests(onlineUser, isIgnoringRequests);

        // Update value on the database and send a message | todo: Clean this up
        plugin.getDatabase().getUserData(onlineUser.getUuid())
                .thenAcceptAsync(userData -> userData.ifPresent(data -> plugin.getDatabase()
                        .updateUserData(new SavedUser(onlineUser, data.homeSlots(), isIgnoringRequests, data.rtpCooldown()))
                        .thenRun(() -> plugin.getLocales().getRawLocale("tpignore_toggle_" + (isIgnoringRequests ? "on" : "off"),
                                        plugin.getLocales().getRawLocale("tpignore_toggle_button").orElse(""))
                                .ifPresent(locale -> onlineUser.sendMessage(new MineDown(locale))))));
    }
}
