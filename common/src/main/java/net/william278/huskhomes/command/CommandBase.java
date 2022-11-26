package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract cross-platform representation for a plugin command
 */
public abstract class CommandBase {

    /**
     * The primary alias input string to match for this command
     */
    @NotNull
    public final String command;

    /**
     * Additional alias input strings for this command
     */
    @NotNull
    public final String[] aliases;

    /**
     * The permission node required to use this command
     */
    @NotNull
    public final String permission;

    /**
     * Instance of the implementing plugin
     */
    @NotNull
    protected final HuskHomes plugin;

    /**
     * The command usage parameter string, excluding the alias
     */
    @NotNull
    protected String usage = "";

    /**
     * Constructor for a command
     *
     * @param command     The primary alias input string to match for this command
     * @param permission  The permission node required to use this command
     * @param implementor Instance of the implementing plugin
     * @param aliases     Additional alias input strings for this command
     */
    protected CommandBase(@NotNull String command, @NotNull Permission permission, @NotNull HuskHomes implementor,
                          @NotNull String... aliases) {
        this.command = command;
        this.permission = permission.node;
        this.plugin = implementor;
        this.aliases = aliases;
    }

    /**
     * Constructor for a command with usage parameters
     *
     * @param command     The input string (primary alias) to match for this command
     * @param permission  The permission node required to use this command
     * @param implementor Instance of the implementing plugin
     * @param usage       The command usage parameter string, excluding the alias
     * @param aliases     Additional alias input strings for this command
     */
    protected CommandBase(@NotNull String command, @NotNull String usage, @NotNull Permission permission, @NotNull HuskHomes implementor,
                          @NotNull String... aliases) {
        this.command = command;
        this.usage = usage;
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
     * Returns the standard invalid syntax error message, with the standard {@link #getUsage() usage parameter string}
     *
     * @return The standard invalid syntax error message for this command
     */
    @NotNull
    public MineDown getSyntaxErrorMessage() {
        final String usage = "/" + command + (!getUsage().isBlank() ? " " + getUsage() : "");
        return plugin.getLocales().getLocale("error_invalid_syntax", usage)
                .orElse(new MineDown("Error: Invalid syntax. Usage: " + usage));
    }

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

    /**
     * Returns the command usage string, excluding the command alias
     *
     * @return the command usage, e.g. {@code [x] [y] [z] [world] [server]}
     */
    @NotNull
    public String getUsage() {
        return usage;
    }

}
