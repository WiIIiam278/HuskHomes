package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetSpawnCommand extends InGameCommand {

    protected SetSpawnCommand(@NotNull HuskHomes plugin) {
        super("setspawn", List.of(), "", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Position position = executor.getPosition();
        try {
            if (plugin.getSettings().isCrossServer() && plugin.getSettings().isGlobalSpawn()) {
                final String warpName = plugin.getSettings().getGlobalSpawnName();
                plugin.getManager().warps().createWarp(warpName, position, true);
                plugin.getLocales().getRawLocale("spawn_warp_default_description")
                        .ifPresent(description -> plugin.getManager().warps().setWarpDescription(warpName, description));
            } else {
                plugin.setServerSpawn(position);
            }
        } catch (ValidationException e) {
            e.dispatchWarpError(executor, plugin, plugin.getSettings().getGlobalSpawnName());
            return;
        }

        plugin.getLocales().getLocale("set_spawn_success")
                .ifPresent(executor::sendMessage);
    }

}
