package net.william278.huskhomes.util;

import com.google.inject.Inject;
import de.themoep.minedown.MineDown;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class SpongeLogger extends Logger {

    private org.apache.logging.log4j.Logger logger;
    public SpongeLogger(@NotNull org.apache.logging.log4j.Logger logger) {
        this.logger = logger;
    }


    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable e) {
        switch (level.getName()) {
            case "WARNING" -> logger.warn(message, e);
            case "SEVERE" -> logger.error(message, e);
            case "FINE" -> logger.debug(message, e);
            case "FINER" -> logger.trace(message, e);
            default -> logger.info(message, e);
        }
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message) {
        switch (level.getName()) {
            case "WARNING" -> logger.warn(message);
            case "SEVERE" -> logger.error(message);
            case "FINE" -> logger.debug(message);
            case "FINER" -> logger.trace(message);
            default -> logger.info(message);
        }
    }

    @Override
    public void info(@NotNull String message) {
        logger.info(message);
    }

    @Override
    public void severe(@NotNull String message) {
        logger.error(message);
    }

    @Override
    public void config(@NotNull String message) {
        logger.info(message);
    }

    @Override
    public void log(@NotNull Level level, @NotNull MineDown mineDown) {
        switch (level.getName()) {
            case "WARNING" -> logger.warn(mineDown.toString());
            case "SEVERE" -> logger.error(mineDown.toString());
            case "FINE" -> logger.debug(mineDown.toString());
            case "FINER" -> logger.trace(mineDown.toString());
            default -> logger.info(mineDown.toString());
        }
    }
}
