package net.william278.huskhomes.migrator;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A migrator that imports legacy and other plugin's data into HuskHomes' format.
 */
public abstract class Migrator {

    protected final HuskHomes plugin;

    protected Migrator(@NotNull HuskHomes implementor) {
        this.plugin = implementor;
    }

    /**
     * Start the migrator
     *
     * @return A future that will be completed when the migrator is done
     */
    public abstract CompletableFuture<Boolean> start();

    /**
     * Handle a command that sets migrator configuration parameters
     *
     * @param args The command arguments
     */
    public abstract void handleConfigurationCommand(@NotNull String[] args);

    /**
     * Obfuscates a data string to prevent important data from being logged to console
     *
     * @param dataString The data string to obfuscate
     * @return The data string obfuscated with stars (*)
     */
    @NotNull
    protected final String obfuscateDataString(@NotNull String dataString) {
        return (dataString.length() > 1 ? dataString.charAt(0) + "*".repeat(dataString.length() - 1) : "");
    }

    /**
     * Get the unique identifier of the migrator
     *
     * @return The unique identifier of the migrator
     */
    @NotNull
    public abstract String getIdentifier();

    /**
     * Get the display name of the migrator
     *
     * @return The name of the migrator
     */
    @NotNull
    public abstract String getName();

    /**
     * Get the help splash menu of the migrator
     *
     * @return The help splash menu of the migrator
     */
    @NotNull
    public abstract String getHelpMenu();

}
