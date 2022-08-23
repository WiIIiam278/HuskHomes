package net.william278.huskhomes.command;

import net.william278.huskhomes.BukkitHuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Commands available on the Bukkit HuskHomes implementation
 */
public enum BukkitCommandType {

    HOME_COMMAND(new HomeCommand(BukkitHuskHomes.getInstance())),
    SET_HOME_COMMAND(new SetHomeCommand(BukkitHuskHomes.getInstance())),
    HOME_LIST_COMMAND(new HomeListCommand(BukkitHuskHomes.getInstance())),
    DEL_HOME_COMMAND(new DelHomeCommand(BukkitHuskHomes.getInstance())),
    EDIT_HOME_COMMAND(new EditHomeCommand(BukkitHuskHomes.getInstance())),
    PUBLIC_HOME_COMMAND(new PublicHomeCommand(BukkitHuskHomes.getInstance())),
    PUBLIC_HOME_LIST_COMMAND(new PublicHomeListCommand(BukkitHuskHomes.getInstance())),
    WARP_COMMAND(new WarpCommand(BukkitHuskHomes.getInstance())),
    SET_WARP_COMMAND(new SetWarpCommand(BukkitHuskHomes.getInstance())),
    WARP_LIST_COMMAND(new WarpListCommand(BukkitHuskHomes.getInstance())),
    DEL_WARP_COMMAND(new DelWarpCommand(BukkitHuskHomes.getInstance())),
    EDIT_WARP_COMMAND(new EditWarpCommand(BukkitHuskHomes.getInstance())),
    TP_COMMAND(new TpCommand(BukkitHuskHomes.getInstance())),
    TP_HERE_COMMAND(new TpHereCommand(BukkitHuskHomes.getInstance())),
    TPA_COMMAND(new TpaCommand(BukkitHuskHomes.getInstance())),
    TPA_HERE_COMMAND(new TpaHereCommand(BukkitHuskHomes.getInstance())),
    TPACCEPT_COMMAND(new TpRespondCommand(BukkitHuskHomes.getInstance(), true)),
    TPDECLINE_COMMAND(new TpRespondCommand(BukkitHuskHomes.getInstance(), false)),
    RTP_COMMAND(new RtpCommand(BukkitHuskHomes.getInstance())),
    TP_IGNORE_COMMAND(new TpIgnoreCommand(BukkitHuskHomes.getInstance())),
    TP_OFFLINE_COMMAND(new TpOfflineCommand(BukkitHuskHomes.getInstance())),
    TP_ALL_COMMAND(new TpAllCommand(BukkitHuskHomes.getInstance())),
    TPA_ALL_COMMAND(new TpaAllCommand(BukkitHuskHomes.getInstance())),
    SPAWN_COMMAND(new SpawnCommand(BukkitHuskHomes.getInstance())),
    SET_SPAWN_COMMAND(new SetSpawnCommand(BukkitHuskHomes.getInstance())),
    BACK_COMMAND(new BackCommand(BukkitHuskHomes.getInstance())),
    HUSKHOMES_COMMAND(new HuskHomesCommand(BukkitHuskHomes.getInstance()));

    public final CommandBase commandBase;

    BukkitCommandType(@NotNull CommandBase commandBase) {
        this.commandBase = commandBase;
    }
}
