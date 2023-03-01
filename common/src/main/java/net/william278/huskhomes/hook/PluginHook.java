package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin hook, where HuskHomes hooks into another plugin
 */
public abstract class PluginHook {

    /**
     * The plugin that this hook is for
     */
    protected final HuskHomes plugin;

    /**
     * The name of the hook
     */
    protected final String hookName;

    /**
     * Construct a new {@link PluginHook}
     *
     * @param implementor the {@link HuskHomes} instance
     */
    protected PluginHook(@NotNull HuskHomes implementor, @NotNull String hookName) {
        this.plugin = implementor;
        this.hookName = hookName;
    }

    /**
     * Initialize the hook and return {@code true} if it could be enabled
     *
     * @return {@code true} if the hook could be enabled
     */
    public abstract boolean initialize() ;

    @NotNull
    public String getHookName() {
        return hookName;
    }


}
