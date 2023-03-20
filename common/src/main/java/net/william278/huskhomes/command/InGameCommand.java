package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class InGameCommand extends Command {

    protected InGameCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull String usage,
                            @NotNull HuskHomes plugin) {
        super(name, aliases, usage, plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (!(executor instanceof OnlineUser user)) {
            plugin.getLocales().getLocale("error_in_game_only")
                    .ifPresent(executor::sendMessage);
            return;
        }
        this.execute(user, args);
    }

    public abstract void execute(@NotNull OnlineUser executor, @NotNull String[] args);

}
