package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

public interface RandomTeleportProvider {

    /**
     * The {@link RandomTeleportEngine} that manages random teleports.
     *
     * @return the {@link RandomTeleportEngine} implementation
     */
    @NotNull
    RandomTeleportEngine getRandomTeleportEngine();

    /**
     * Sets the {@link RandomTeleportEngine} to be used for processing random teleports.
     *
     * @param randomTeleportEngine the {@link RandomTeleportEngine} to use
     */
    void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine);

    default void loadRandomTeleportEngine() {
        setRandomTeleportEngine(new NormalDistributionEngine(getPlugin()));
    }

    @NotNull
    HuskHomes getPlugin();

}
