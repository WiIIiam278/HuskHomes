package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DisabledCommand extends Command {

    public DisabledCommand(@NotNull String name, @NotNull HuskHomes plugin) {
        super(name, List.of(), "", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        plugin.getLocales().getLocale("error_command_disabled")
                .ifPresent(executor::sendMessage);
    }

}
