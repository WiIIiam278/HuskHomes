package net.william278.huskhomes.database;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

//todo libby
public final class SpongeSqLiteDatabase extends SqLiteDatabase {

    private final PluginContainer pluginContainer;

    public SpongeSqLiteDatabase(@NotNull HuskHomes implementor, @NotNull PluginContainer container) {
        super(implementor);
        this.pluginContainer = container;
    }

    @Override
    protected Connection fetchDriverConnection(@NotNull File databaseFile) throws SQLException {
        return Sponge.sqlManager()
                .dataSource(pluginContainer, "jdbc:sqlite:" + databaseFile.getAbsolutePath() + "?foreign_keys=ON")
                .getConnection();
    }


}
