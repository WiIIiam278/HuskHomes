package net.william278.huskhomes.command;

import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

public interface Executable {

    void onExecuted(@NotNull CommandUser executor, @NotNull String[] args);

}