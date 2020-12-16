package me.william278.huskhomes2.Data.ErrorLogging;

import me.william278.huskhomes2.HuskHomes;

import java.util.logging.Level;

public class Error {

    public static void execute(HuskHomes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(HuskHomes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}