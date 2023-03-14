package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrivateHomeCommand extends HomeCommand {

    protected PrivateHomeCommand(@NotNull HuskHomes plugin) {
        super("home", List.of(), plugin);
    }

}
