package me.william278.huskhomes2;

import me.william278.huskhomes2.Data.SQLite.Database;
import me.william278.huskhomes2.Data.SQLite.SQLite;
import org.bukkit.Bukkit;

// This class handles the saving of data; whether that be through SQLite or SQL
public class dataManager {

    private static Database sqliteDatabase;

    private static void initializeSQLite() {
        sqliteDatabase = new SQLite(Main.getInstance());
        sqliteDatabase.load();
    }

    private static void initializeMySQL() {

    }

    public static void setupStorage(String storageType) {
        if (storageType.toLowerCase().equals("sqlite")) {
            initializeSQLite();
        } else if (storageType.toLowerCase().equals("mysql")) {
            initializeMySQL();
        } else {
            Bukkit.getLogger().warning("Invalid storage method set in config.yml; defaulting to SQLite");
            initializeSQLite();
        }
    }

}
