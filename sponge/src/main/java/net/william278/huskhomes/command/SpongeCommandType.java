package net.william278.huskhomes.command;

import net.william278.huskhomes.SpongeHuskHomes;
import org.jetbrains.annotations.NotNull;

/**
 * Commands available on the Bukkit HuskHomes implementation
 */
public enum SpongeCommandType {

    HOME_COMMAND(new HomeCommand(SpongeHuskHomes.getInstance())),
    SET_HOME_COMMAND(new SetHomeCommand(SpongeHuskHomes.getInstance())),
    HOME_LIST_COMMAND(new HomeListCommand(SpongeHuskHomes.getInstance())),
    DEL_HOME_COMMAND(new DelHomeCommand(SpongeHuskHomes.getInstance())),
    EDIT_HOME_COMMAND(new EditHomeCommand(SpongeHuskHomes.getInstance())),
    PUBLIC_HOME_COMMAND(new PublicHomeCommand(SpongeHuskHomes.getInstance())),
    PUBLIC_HOME_LIST_COMMAND(new PublicHomeListCommand(SpongeHuskHomes.getInstance())),
    WARP_COMMAND(new WarpCommand(SpongeHuskHomes.getInstance())),
    SET_WARP_COMMAND(new SetWarpCommand(SpongeHuskHomes.getInstance())),
    WARP_LIST_COMMAND(new WarpListCommand(SpongeHuskHomes.getInstance())),
    DEL_WARP_COMMAND(new DelWarpCommand(SpongeHuskHomes.getInstance())),
    EDIT_WARP_COMMAND(new EditWarpCommand(SpongeHuskHomes.getInstance())),
    TP_COMMAND(new TpCommand(SpongeHuskHomes.getInstance())),
    TP_HERE_COMMAND(new TpHereCommand(SpongeHuskHomes.getInstance())),
    TPA_COMMAND(new TpaCommand(SpongeHuskHomes.getInstance())),
    TPA_HERE_COMMAND(new TpaHereCommand(SpongeHuskHomes.getInstance())),
    TPACCEPT_COMMAND(new TpRespondCommand(SpongeHuskHomes.getInstance(), true)),
    TPDECLINE_COMMAND(new TpRespondCommand(SpongeHuskHomes.getInstance(), false)),
    RTP_COMMAND(new RtpCommand(SpongeHuskHomes.getInstance())),
    TP_IGNORE_COMMAND(new TpIgnoreCommand(SpongeHuskHomes.getInstance())),
    TP_OFFLINE_COMMAND(new TpOfflineCommand(SpongeHuskHomes.getInstance())),
    TP_ALL_COMMAND(new TpAllCommand(SpongeHuskHomes.getInstance())),
    TPA_ALL_COMMAND(new TpaAllCommand(SpongeHuskHomes.getInstance())),
    SPAWN_COMMAND(new SpawnCommand(SpongeHuskHomes.getInstance())),
    SET_SPAWN_COMMAND(new SetSpawnCommand(SpongeHuskHomes.getInstance())),
    BACK_COMMAND(new BackCommand(SpongeHuskHomes.getInstance())),
    HUSKHOMES_COMMAND(new HuskHomesCommand(SpongeHuskHomes.getInstance()));

    public final CommandBase commandBase;

    SpongeCommandType(@NotNull CommandBase commandBase) {
        this.commandBase = commandBase;
    }
}
