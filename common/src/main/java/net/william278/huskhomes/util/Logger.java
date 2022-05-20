package net.william278.huskhomes.util;

import java.util.logging.Level;

public interface Logger {

    void log(Level level, String message, Exception e);

    void log(Level level, String message);

    void info(String message);

    void severe(String message);

    void config(String message);

}
