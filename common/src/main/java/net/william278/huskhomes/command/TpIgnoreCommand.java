package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.manager.RequestsManager;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TpIgnoreCommand extends InGameCommand {

    protected TpIgnoreCommand(@NotNull HuskHomes plugin) {
        super("tpignore", List.of(), "", plugin);
    }

    @Override
    public void execute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        final RequestsManager manager = plugin.getManager().requests();
        final boolean isIgnoringRequests = !manager.isIgnoringRequests(onlineUser);

        final SavedUser user = plugin.getDatabase().getUserData(onlineUser.getUuid())
                .orElseThrow(() -> new IllegalStateException("User data not found for " + onlineUser.getUsername()));
        user.setIgnoringTeleports(isIgnoringRequests);
        manager.setIgnoringRequests(onlineUser, isIgnoringRequests);
        plugin.getDatabase().updateUserData(user);

        plugin.getLocales().getRawLocale("tpignore_toggle_" + (isIgnoringRequests ? "on" : "off"),
                        plugin.getLocales().getRawLocale("tpignore_toggle_button").orElse(""))
                .map(MineDown::new)
                .ifPresent(onlineUser::sendMessage);
    }
}
