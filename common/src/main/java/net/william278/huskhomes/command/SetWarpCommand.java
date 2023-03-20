package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

public class SetWarpCommand extends SetPositionCommand {

    protected SetWarpCommand(@NotNull HuskHomes plugin) {
        super("setwarp", plugin);
    }

    @Override
    protected void execute(@NotNull OnlineUser setter, @NotNull String name) {
        try {
            plugin.getManager().warps().createWarp(name, setter.getPosition());
        } catch (ValidationException e) {
            e.dispatchWarpError(setter, plugin, name);
        }
    }
}
