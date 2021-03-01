package me.william278.huskhomes2.data;

import me.william278.huskhomes2.HuskHomes;

import java.util.logging.Level;

public class Error {
    public static final String SQL_CONNECTION_EXECUTE = "Couldn't execute MySQL statement: ";
    public static final String SQL_CONNECTION_CLOSE = "Failed to close MySQL connection: ";
    public static final String SQL_NO_CONNECTION = "Unable to retrieve MYSQL connection: ";
    public static final String SQL_NO_TABLE_FOUND = "Database Error: No Table Found";

    public static void execute(HuskHomes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }

    public static void close(HuskHomes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}