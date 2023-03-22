package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin hook, where HuskHomes hooks into another plugin
 */
public abstract class Hook {

    /**
     * The plugin that this hook is for
     */
    protected final HuskHomes plugin;

    /**
     * The name of the hook
     */
    protected final String name;

    /**
     * Construct a new {@link Hook}
     *
     * @param plugin the {@link HuskHomes} instance
     */
    protected Hook(@NotNull HuskHomes plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Initialize the hook and return {@code true} if it could be enabled
     */
    public abstract void initialize() ;

    @NotNull
    public String getName() {
        return name;
    }


}
