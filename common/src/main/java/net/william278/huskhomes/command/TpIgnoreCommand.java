package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.manager.RequestsManager;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
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

        plugin.editUserData(onlineUser, (SavedUser user) -> user.setIgnoringTeleports(isIgnoringRequests));

        plugin.getLocales().getRawLocale("tpignore_toggle_" + (isIgnoringRequests ? "on" : "off"),
                        plugin.getLocales().getRawLocale("tpignore_toggle_button").orElse(""))
                .map(MineDown::new)
                .ifPresent(onlineUser::sendMessage);
    }
}
