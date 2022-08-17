package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract cross-platform representation for a plugin command
 */
public abstract class CommandBase {

    /**
     * The input string to match for this command
     */
    @NotNull
    public final String command;

    /**
     * The permission node required to use this command
     */
    @NotNull
    protected final String permission;

    /**
     * Alias input strings for this command
     */
    @NotNull
    protected final String[] aliases;

    /**
     * Instance of the implementing plugin
     */
    @NotNull
    protected final HuskHomes plugin;

    protected CommandBase(@NotNull String command, @NotNull Permission permission, @NotNull HuskHomes implementor,
                          @NotNull String... aliases) {
        this.command = command;
        this.permission = permission.node;
        this.plugin = implementor;
        this.aliases = aliases;
    }

    /**
     * Fires when the command is executed
     *
     * @param onlineUser {@link OnlineUser} executing the command
     * @param args       Command arguments
     */
    public abstract void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args);

    /**
     * Returns the localised description string of this command
     *
     * @return the command description
     */
    @NotNull
    public String getDescription() {
        return plugin.getLocales().getRawLocale(command + "_command_description")
                .orElse("A HuskHomes command");
    }

}
