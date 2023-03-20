package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public abstract class SetPositionCommand extends InGameCommand {

    protected SetPositionCommand(@NotNull String name, @NotNull HuskHomes plugin) {
        super(name, List.of(), "<name>", plugin);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Optional<String> name = parseStringArg(args, 0);
        if (name.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.execute(executor, name.get());
    }

    protected abstract void execute(@NotNull OnlineUser setter, @NotNull String name);
}
