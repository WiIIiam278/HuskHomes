package net.william278.huskhomes.command;

import net.william278.huskhomes.BukkitHuskHomes;
import org.jetbrains.annotations.NotNull;

/**
 * Commands available on the Bukkit HuskHomes implementation
 */
public enum BukkitCommandType {

    HOME_COMMAND(new HomeCommand(BukkitHuskHomes.getInstance())),
    SET_HOME_COMMAND(new SetHomeCommand(BukkitHuskHomes.getInstance())),
    HOME_LIST_COMMAND(new HomeListCommand(BukkitHuskHomes.getInstance())),
    DEL_HOME_COMMAND(new DelHomeCommand(BukkitHuskHomes.getInstance())),
    WARP_COMMAND(new WarpCommand(BukkitHuskHomes.getInstance())),
    SET_WARP_COMMAND(new SetWarpCommand(BukkitHuskHomes.getInstance())),
    WARP_LIST_COMMAND(new WarpListCommand(BukkitHuskHomes.getInstance())),
    DEL_WARP_COMMAND(new DelWarpCommand(BukkitHuskHomes.getInstance())),
    BACK_COMMAND(new BackCommand(BukkitHuskHomes.getInstance()));

    public final CommandBase commandBase;

    BukkitCommandType(@NotNull CommandBase commandBase) {
        this.commandBase = commandBase;
    }
}
