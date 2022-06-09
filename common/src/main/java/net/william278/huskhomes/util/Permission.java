package net.william278.huskhomes.util;

import org.jetbrains.annotations.NotNull;

/**
 * Static plugin permission nodes required to perform actions or execute commands
 */
public enum Permission {

    /*
     * Bypass permission nodes
     */

    /**
     * Lets the user bypass the warmup time when a player must stand still before teleportation
     */
    BYPASS_TELEPORT_WARMUP("huskhomes.bypass.teleport_warmup", DefaultAccess.NOBODY),
    /**
     * Lets the user bypass the cooldown on the {@code /rtp} command
     */
    BYPASS_RTP_COOLDOWN("huskhomes.bypass.rtp_cooldown", DefaultAccess.NOBODY),

    /*
     * Command permission nodes
     */

    /**
     * Lets the user teleport to their homes ({@code /home [home_name]})
     */
    COMMAND_HOME("huskhomes.command.home", DefaultAccess.EVERYONE),
    /**
     * Lets the user teleport to other users' homes ({@code /home [player_name.home_name]})
     */
    COMMAND_HOME_OTHER("huskhomes.command.home.other", DefaultAccess.OPERATORS),
    /**
     * Lets the user set a home ({@code /sethome [home_name]})
     */
    COMMAND_SET_HOME("huskhomes.command.sethome", DefaultAccess.EVERYONE),
    /**
     * Lets the user delete a home ({@code /delhome [home_name]})
     */
    COMMAND_DELETE_HOME("huskhomes.command.delhome", DefaultAccess.EVERYONE),
    /**
     * Lets the user delete other users' homes ({@code /delhome [player_name.home_name]})
     */
    COMMAND_DELETE_HOME_OTHER("huskhomes.command.delhome.other", DefaultAccess.OPERATORS),
    /**
     * Lets the user edit their homes ({@code /edithome [home_name]})
     */
    COMMAND_EDIT_HOME("huskhomes.command.edithome", DefaultAccess.EVERYONE),
    /**
     * Lets the user edit other users' homes ({@code /edithome [player_name.home_name]})
     */
    COMMAND_EDIT_HOME_OTHER("huskhomes.command.edithome.other", DefaultAccess.OPERATORS),
    /**
     * Lets the user edit the privacy of their home ({@code /edithome [home_name] [public/private]})
     */
    COMMAND_EDIT_HOME_PRIVACY("huskhomes.command.edithome.privacy", DefaultAccess.EVERYONE),
    /**
     * Lets the user teleport to a public home ({@code /phome [player_name.home_name]})
     */
    COMMAND_PUBLIC_HOME("huskhomes.command.phome", DefaultAccess.EVERYONE),
    /**
     * Lets the user teleport to a warp ({@code /warp [warp_name]})
     */
    COMMAND_WARP("huskhomes.command.warp", DefaultAccess.EVERYONE),
    /**
     * Lets the user set a warp ({@code /setwarp [warp_name]})
     */
    COMMAND_SET_WARP("huskhomes.command.setwarp", DefaultAccess.OPERATORS),
    /**
     * Lets the user delete a warp ({@code /delwarp [warp_name]})
     */
    COMMAND_DELETE_WARP("huskhomes.command.delwarp", DefaultAccess.OPERATORS),
    /**
     * Lets the user edit a warp ({@code /editwarp [warp_name]})
     */
    COMMAND_EDIT_WARP("huskhomes.command.editwarp", DefaultAccess.OPERATORS),
    /**
     * Lets the user instantly tp to another player ({@code /tp [player_name]})
     */
    COMMAND_TP("huskhomes.command.tp", DefaultAccess.OPERATORS),
    /**
     * Lets the user instantly tp to a set of coordinates ({@code /tp [x] [y] [z] [yaw] [pitch] [world] [server]})
     */
    COMMAND_TP_TO_COORDINATES("huskhomes.command.tp.coordinates", DefaultAccess.OPERATORS),
    /**
     * Lets the user instantly tp another player to them ({@code /tphere [player_name]})
     */
    COMMAND_TPHERE("huskhomes.command.tphere", DefaultAccess.OPERATORS),
    /**
     * Lets the user request to tp to another player ({@code /tpa [player_name]})
     */
    COMMAND_TPA("huskhomes.command.tpa", DefaultAccess.EVERYONE),
    /**
     * Lets the user request another player to tp to them ({@code /tpahere [player_name]})
     */
    COMMAND_TPAHERE("huskhomes.command.tpahere", DefaultAccess.EVERYONE),
    /**
     * Lets the user accept a tp request ({@code /tpaccept})
     */
    COMMAND_TPACCEPT("huskhomes.command.tpaccept", DefaultAccess.EVERYONE),
    /**
     * Lets the user deny a tp request ({@code /tpdeny})
     */
    COMMAND_TPDENY("huskhomes.command.tpdeny", DefaultAccess.EVERYONE),
    /**
     * Lets the user ignore incoming tp requests ({@code /tpignore})
     */
    COMMAND_TPIGNORE("huskhomes.command.tpignore", DefaultAccess.EVERYONE),
    /**
     * Lets the user tp to where a user logged out ({@code /tpoffline [player_name]})
     */
    COMMAND_TPOFFLINE("huskhomes.command.tpoffline", DefaultAccess.OPERATORS),
    /**
     * Lets the user randomly teleport in the world they are in ({@code /rtp})
     */
    COMMAND_RTP("huskhomes.command.rtp", DefaultAccess.EVERYONE),
    /**
     * Lets the user teleport to the defined spawn position ({@code /spawn})
     */
    COMMAND_SPAWN("huskhomes.command.spawn", DefaultAccess.EVERYONE),
    /**
     * Lets the user set the spawn position ({@code /setspawn})
     */
    COMMAND_SETSPAWN("huskhomes.command.setspawn", DefaultAccess.OPERATORS),
    /**
     * Lets the user return to where they last teleported from ({@code /back})
     */
    COMMAND_BACK("huskhomes.command.back", DefaultAccess.EVERYONE),
    /**
     * Lets the user use {@code /back} to return to where they died
     */
    COMMAND_BACK_ON_DEATH("huskhomes.command.back.death", DefaultAccess.EVERYONE),
    /**
     * Lets the user view plugin information
     */
    COMMAND_HUSKHOMES_ABOUT("huskhomes.command.huskhomes.about", DefaultAccess.EVERYONE),
    /**
     * Lets the user reload the plugin config and message files
     */
    COMMAND_HUSKHOMES_RELOAD("huskhomes.command.huskhomes.reload", DefaultAccess.OPERATORS),
    /**
     * Lets the user check for plugin updates
     */
    COMMAND_HUSKHOMES_UPDATE("huskhomes.command.huskhomes.update", DefaultAccess.OPERATORS);

    public final String node;
    public final DefaultAccess defaultAccess;

    Permission(@NotNull String node, @NotNull DefaultAccess defaultAccess) {
        this.node = node;
        this.defaultAccess = defaultAccess;
    }

    /**
     * Identifies who gets what permissions by default
     */
    public enum DefaultAccess {
        /**
         * Everyone gets this permission node by default
         */
        EVERYONE,
        /**
         * Nobody gets this permission node by default
         */
        NOBODY,
        /**
         * Server operators ({@code /op}) get this permission node by default
         */
        OPERATORS
    }
}
