package me.william278.huskhomes2.data.SQL;

import me.william278.huskhomes2.HuskHomes;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Database {
    protected HuskHomes plugin;

    public Database(HuskHomes instance) {
        plugin = instance;
    }

    public abstract Connection getConnection() throws SQLException;

    public abstract void load();

    public abstract void backup();

    public abstract void close();

    public final int hikariMaximumPoolSize = HuskHomes.getSettings().getHikariMaximumPoolSize();
    public final int hikariMinimumIdle = HuskHomes.getSettings().getHikariMinimumIdle();
    public final long hikariMaximumLifetime = HuskHomes.getSettings().getHikariMaximumLifetime();
    public final long hikariKeepAliveTime = HuskHomes.getSettings().getHikariKeepAliveTime();
    public final long hikariConnectionTimeOut = HuskHomes.getSettings().getHikariConnectionTimeOut();
}
