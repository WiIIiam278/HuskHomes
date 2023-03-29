package net.william278.huskhomes;

import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.PaperCommand;
import net.william278.huskhomes.hook.Pl3xMapHook;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PaperHuskHomes extends BukkitHuskHomes {

    @Override
    protected void prepareHooks() {
        super.prepareHooks();

        if (getMapHook().isEmpty() && Bukkit.getServer().getPluginManager().getPlugin("Pl3xMap") != null) {
            getHooks().add(new Pl3xMapHook(this));
        }
    }

    @NotNull
    @Override
    protected List<Command> registerCommands() {
        return Arrays.stream(BukkitCommand.Type.values())
                .map(type -> {
                    final Command command = type.getCommand();
                    if (!getSettings().isCommandDisabled(command)) {
                        new PaperCommand(command, this).register();
                        return command;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
