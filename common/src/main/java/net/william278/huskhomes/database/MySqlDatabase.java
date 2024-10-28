/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.database;

import com.zaxxer.hikari.HikariDataSource;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

import static net.william278.huskhomes.config.Settings.DatabaseSettings;
import static net.william278.huskhomes.database.DatabaseProvider.DATA_POOL_NAME;

/**
 * A MySQL / MariaDB implementation of the plugin {@link Database}.
 */
@SuppressWarnings("DuplicatedCode")
public class MySqlDatabase extends Database {

    private final String flavor;
    private final String driverClass;
    private HikariDataSource dataSource;

    public MySqlDatabase(@NotNull HuskHomes plugin) {
        super(plugin);
        this.flavor = plugin.getSettings().getDatabase().getType() == Type.MARIADB
                ? "mariadb" : "mysql";
        this.driverClass = plugin.getSettings().getDatabase().getType() == Type.MARIADB
                ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void setConnection() {
        // Initialize the Hikari pooled connection
        final DatabaseSettings databaseSettings = plugin.getSettings().getDatabase();
        final DatabaseSettings.DatabaseCredentials credentials = databaseSettings.getCredentials();

        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s%s",
                flavor,
                credentials.getHost(),
                credentials.getPort(),
                credentials.getDatabase(),
                credentials.getParameters()
        ));

        // Authenticate with the database
        dataSource.setUsername(credentials.getUsername());
        dataSource.setPassword(credentials.getPassword());

        // Set connection pool options
        final DatabaseSettings.PoolOptions poolOptions = databaseSettings.getPoolOptions();
        dataSource.setMaximumPoolSize(poolOptions.getSize());
        dataSource.setMinimumIdle(poolOptions.getIdle());
        dataSource.setMaxLifetime(poolOptions.getLifetime());
        dataSource.setKeepaliveTime(poolOptions.getKeepAlive());
        dataSource.setConnectionTimeout(poolOptions.getTimeout());
        dataSource.setPoolName(DATA_POOL_NAME);

        // Set additional connection pool properties
        final Properties properties = new Properties();
        properties.putAll(
                Map.of("cachePrepStmts", "true",
                        "prepStmtCacheSize", "250",
                        "prepStmtCacheSqlLimit", "2048",
                        "useServerPrepStmts", "true",
                        "useLocalSessionState", "true",
                        "useLocalTransactionState", "true"
                ));
        properties.putAll(
                Map.of(
                        "rewriteBatchedStatements", "true",
                        "cacheResultSetMetadata", "true",
                        "cacheServerConfiguration", "true",
                        "elideSetAutoCommits", "true",
                        "maintainTimeStats", "false")
        );
        dataSource.setDataSourceProperties(properties);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    protected void executeScript(@NotNull Connection connection, @NotNull String name) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String schemaStatement : getScript(name)) {
                statement.execute(schemaStatement);
            }
        }
    }

    @Override
    public void initialize() throws RuntimeException {
        // Establish connection
        this.setConnection();

        // Create tables
        if (!isCreated()) {
            plugin.log(Level.INFO, "Creating MySQL database tables");
            try {
                executeScript(getConnection(), String.format("%s_schema.sql", flavor));
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to create MySQL database tables", e);
                setLoaded(false);
                return;
            }
            setSchemaVersion(Migration.getLatestVersion());
            plugin.log(Level.INFO, "MySQL database tables created!");
            setLoaded(true);
            return;
        }

        // Perform migrations
        try {
            performMigrations(getConnection(), plugin.getSettings().getDatabase().getType());
            setLoaded(true);
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to perform MySQL database migrations", e);
            setLoaded(false);
        }
    }

    @Override
    public boolean isCreated() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `uuid`
                    FROM `%player_data%`
                    LIMIT 1;"""))) {
                statement.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int getSchemaVersion() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `schema_version`
                    FROM `%meta_data%`
                    LIMIT 1;"""))) {
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("schema_version");
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.WARNING, "The database schema version could not be fetched; "
                                      + "migrations will be carried out.");
        }
        return -1;
    }

    @Override
    public void setSchemaVersion(int version) {
        if (getSchemaVersion() == -1) {
            try (Connection connection = getConnection()) {
                try (PreparedStatement insertStatement = connection.prepareStatement(format("""
                        INSERT INTO `%meta_data%` (`schema_version`)
                        VALUES (?)"""))) {
                    insertStatement.setInt(1, version);
                    insertStatement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to insert schema version in table", e);
            }
            return;
        }

        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%meta_data%`
                    SET `schema_version` = ?;"""))) {
                statement.setInt(1, version);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update schema version in table", e);
        }
    }

    @Override
    protected int setPosition(@NotNull Position position, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(format("""
                        INSERT INTO `%position_data%`
                            (`x`,`y`,`z`,`yaw`,`pitch`,`world_name`,`world_uuid`,`server_name`)
                        VALUES
                            (?,?,?,?,?,?,?,?);"""),
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setDouble(1, position.getX());
            statement.setDouble(2, position.getY());
            statement.setDouble(3, position.getZ());
            statement.setFloat(4, position.getYaw());
            statement.setFloat(5, position.getPitch());
            statement.setString(6, position.getWorld().getName());
            statement.setString(7, position.getWorld().getUuid().toString());
            statement.setString(8, position.getServer());
            statement.executeUpdate();

            final ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            throw new SQLException("Failed to insert position into database");
        }
    }

    @Override
    protected void updatePosition(int positionId, @NotNull Position position,
                                  @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(format("""
                UPDATE `%position_data%`
                SET `x`=?,
                `y`=?,
                `z`=?,
                `yaw`=?,
                `pitch`=?,
                `world_uuid`=?,
                `world_name`=?,
                `server_name`=?
                WHERE `id`=?"""))) {
            statement.setDouble(1, position.getX());
            statement.setDouble(2, position.getY());
            statement.setDouble(3, position.getZ());
            statement.setFloat(4, position.getYaw());
            statement.setFloat(5, position.getPitch());
            statement.setString(6, position.getWorld().getUuid().toString());
            statement.setString(7, position.getWorld().getName());
            statement.setString(8, position.getServer());
            statement.setDouble(9, positionId);
            statement.executeUpdate();
        }
    }

    @Override
    protected int setSavedPosition(@NotNull SavedPosition position,
                                   @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(format("""
                        INSERT INTO `%saved_position_data%`
                            (`position_id`, `name`, `description`, `tags`, `timestamp`)
                        VALUES
                            (?,?,?,?,?);"""),
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, setPosition(position, connection));
            statement.setString(2, position.getName());
            statement.setString(3, position.getMeta().getDescription());
            statement.setString(4, position.getMeta().getSerializedTags());
            statement.setTimestamp(5, Timestamp.from(position.getMeta().getCreationTime()));
            statement.executeUpdate();

            final ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            throw new SQLException("Failed to insert saved position into database");
        }
    }

    @Override
    protected void updateSavedPosition(int savedPositionId, @NotNull SavedPosition position,
                                       @NotNull Connection connection) throws SQLException {
        try (PreparedStatement selectStatement = connection.prepareStatement(format("""
                SELECT `position_id`
                FROM `%saved_position_data%`
                WHERE `id`=?;"""))) {
            selectStatement.setInt(1, savedPositionId);

            final ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                final int positionId = resultSet.getInt("position_id");
                updatePosition(positionId, position, connection);

                try (PreparedStatement updateStatement = connection.prepareStatement(format("""
                        UPDATE `%saved_position_data%`
                        SET `name`=?,
                        `description`=?,
                        `tags`=?
                        WHERE `id`=?;"""))) {
                    updateStatement.setString(1, position.getName());
                    updateStatement.setString(2, position.getMeta().getDescription());
                    updateStatement.setString(3, position.getMeta().getSerializedTags());
                    updateStatement.setInt(4, savedPositionId);
                    updateStatement.executeUpdate();
                }
            }
        }
    }

    @Override
    public void ensureUser(@NotNull User onlineUser) {
        getUser(onlineUser.getUuid()).ifPresentOrElse(
                existingUserData -> {
                    if (!existingUserData.getUsername().equals(onlineUser.getName())) {
                        // Update a player's name if it has changed in the database
                        try (Connection connection = getConnection()) {
                            try (PreparedStatement statement = connection.prepareStatement(format("""
                                    UPDATE `%player_data%`
                                    SET `username`=?
                                    WHERE `uuid`=?"""))) {

                                statement.setString(1, onlineUser.getName());
                                statement.setString(2, existingUserData.getUserUuid().toString());
                                statement.executeUpdate();
                            }
                            plugin.log(Level.INFO, "Updated " + onlineUser.getName()
                                                   + "'s name in the database (" + existingUserData.getUsername()
                                                   + " -> " + onlineUser.getName() + ")");
                        } catch (SQLException e) {
                            plugin.log(Level.SEVERE, "Failed to update a player's name on the database", e);
                        }
                    }
                },
                () -> {
                    // Insert new player data into the database
                    try (Connection connection = getConnection()) {
                        try (PreparedStatement statement = connection.prepareStatement(format("""
                                INSERT INTO `%player_data%` (`uuid`,`username`)
                                VALUES (?,?);"""))) {

                            statement.setString(1, onlineUser.getUuid().toString());
                            statement.setString(2, onlineUser.getName());
                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        plugin.log(Level.SEVERE, "Failed to insert a player into the database", e);
                    }
                });
    }

    @Override
    public Optional<SavedUser> getUser(@NotNull String name) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`
                    FROM `%player_data%`
                    WHERE `username`=?"""))) {
                statement.setString(1, name);

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new SavedUser(
                            User.of(UUID.fromString(resultSet.getString("uuid")),
                                    resultSet.getString("username")),
                            resultSet.getInt("home_slots"),
                            resultSet.getBoolean("ignoring_requests")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch a player by name from the database", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SavedUser> getUser(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`
                    FROM `%player_data%`
                    WHERE `uuid`=?"""))) {

                statement.setString(1, uuid.toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new SavedUser(
                            User.of(UUID.fromString(resultSet.getString("uuid")),
                                    resultSet.getString("username")),
                            resultSet.getInt("home_slots"),
                            resultSet.getBoolean("ignoring_requests")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch a player from uuid from the database", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteUser(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `id`
                        IN ((SELECT `last_position` FROM `%player_data%` WHERE `uuid` = ?),
                            (SELECT `offline_position` FROM `%player_data%` WHERE `uuid` = ?),
                            (SELECT `respawn_position` FROM `%player_data%` WHERE `uuid` = ?));"""))) {
                statement.setString(1, uuid.toString());
                statement.setString(2, uuid.toString());
                statement.setString(3, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to delete player positions from the database", e);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%player_data%`
                    WHERE `uuid`=?;"""))) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to delete a player from the database", e);
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete user data", e);
        }
    }

    @Override
    public Optional<Instant> getCooldown(@NotNull TransactionResolver.Action action, @NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `type`, `start_timestamp`, `end_timestamp`
                    FROM `%player_cooldowns_data%`
                    WHERE `player_uuid`=? AND `type`=?
                    ORDER BY `start_timestamp` DESC
                    LIMIT 1;"""))) {
                statement.setString(1, user.getUuid().toString());
                statement.setString(2, action.name().toLowerCase(Locale.ENGLISH));

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(resultSet.getTimestamp("end_timestamp").toInstant());
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to fetch a player's cooldown from the database", e);
        }
        return Optional.empty();
    }

    @Override
    public void setCooldown(@NotNull TransactionResolver.Action action, @NotNull User user,
                            @NotNull Instant cooldownExpiry) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    INSERT INTO `%player_cooldowns_data%` (`player_uuid`, `type`, `start_timestamp`, `end_timestamp`)
                    VALUES (?,?,?,?);"""))) {
                statement.setString(1, user.getUuid().toString());
                statement.setString(2, action.name().toLowerCase(Locale.ENGLISH));
                statement.setTimestamp(3, Timestamp.from(Instant.now()));
                statement.setTimestamp(4, Timestamp.from(cooldownExpiry));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to set a player's cooldown in the database", e);
        }
    }

    @Override
    public void removeCooldown(@NotNull TransactionResolver.Action action, @NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%player_cooldowns_data%`
                    WHERE `player_uuid`=? AND `type`=?;"""))) {

                statement.setString(1, user.getUuid().toString());
                statement.setString(2, action.name().toLowerCase(Locale.ENGLISH));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to remove a player's cooldown from the database", e);
        }
    }

    @Override
    public List<Home> getHomes(@NotNull User user) {
        final List<Home> userHomes = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%home_data%`.`uuid` AS `home_uuid`, `owner_uuid`, `name`, `description`, `tags`,
                        `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                    FROM `%home_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%home_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    INNER JOIN `%player_data%`
                        ON `%home_data%`.`owner_uuid`=`%player_data%`.`uuid`
                    WHERE `owner_uuid`=?
                    ORDER BY `name`;"""))) {

                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    userHomes.add(Home.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            user,
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the database for home data for:" + user.getName());
        }
        return userHomes;
    }

    @Override
    public List<Warp> getWarps() {
        final List<Warp> warps = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%warp_data%`.`uuid` AS `warp_uuid`, `name`, `description`, `tags`, `timestamp`,
                        `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%warp_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%warp_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    ORDER BY `name`;"""))) {

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    warps.add(Warp.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("warp_uuid"))));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the database for warp data.");
        }
        return warps;
    }

    @Override
    public List<Home> getPublicHomes() {
        final List<Home> userHomes = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%home_data%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`,
                        `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`,
                        `server_name`, `public`
                    FROM `%home_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%home_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    INNER JOIN `%player_data%`
                        ON `%home_data%`.`owner_uuid`=`%player_data%`.`uuid`
                    WHERE `public`=true
                    ORDER BY `name`;"""))) {
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    userHomes.add(Home.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            User.of(UUID.fromString(resultSet.getString("owner_uuid")),
                                    resultSet.getString("owner_username")),
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the database for public home data");
        }
        return userHomes;
    }

    @Override
    public List<Home> getPublicHomes(@NotNull String name, boolean caseInsensitive) {
        final List<Home> userHomes = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%home_data%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`,
                        `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`,
                        `server_name`, `public`
                    FROM `%home_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%home_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    INNER JOIN `%player_data%`
                        ON `%home_data%`.`owner_uuid`=`%player_data%`.`uuid`
                    WHERE `public`=true
                    AND ((? AND UPPER(`name`) LIKE UPPER(?)) OR (`name`=?))
                    ORDER BY `name`;"""))) {
                statement.setBoolean(1, caseInsensitive);
                statement.setString(2, name);
                statement.setString(3, name);

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    userHomes.add(Home.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            User.of(UUID.fromString(resultSet.getString("owner_uuid")),
                                    resultSet.getString("owner_username")),
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the database for public home data");
        }
        return userHomes;
    }

    @Override
    public Optional<Home> getHome(@NotNull User user, @NotNull String homeName, boolean caseInsensitive) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%home_data%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`,
                        `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`,
                        `world_uuid`, `server_name`, `public`
                    FROM `%home_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%home_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    INNER JOIN `%player_data%`
                        ON `%home_data%`.`owner_uuid`=`%player_data%`.`uuid`
                    WHERE `owner_uuid`=?
                    AND ((? AND UPPER(`name`) LIKE UPPER(?)) OR (`name`=?))"""))) {
                statement.setString(1, user.getUuid().toString());
                statement.setBoolean(2, caseInsensitive);
                statement.setString(3, homeName);
                statement.setString(4, homeName);

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Home.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            user,
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query a player's home", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Home> getHome(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%home_data%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`,
                        `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`,
                        `world_uuid`, `server_name`, `public`
                    FROM `%home_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%home_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    INNER JOIN `%player_data%`
                        ON `%home_data%`.`owner_uuid`=`%player_data%`.`uuid`
                    WHERE `%home_data%`.`uuid`=?;"""))) {
                statement.setString(1, uuid.toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Home.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            User.of(UUID.fromString(resultSet.getString("owner_uuid")),
                                    resultSet.getString("owner_username")),
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query a player's home by uuid", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Warp> getWarp(@NotNull String warpName, boolean caseInsensitive) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%warp_data%`.`uuid` AS `warp_uuid`, `name`, `description`, `tags`, `timestamp`,
                        `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%warp_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%warp_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    AND ((? AND UPPER(`name`) LIKE UPPER(?)) OR (`name`=?))"""))) {
                statement.setBoolean(1, caseInsensitive);
                statement.setString(2, warpName);
                statement.setString(3, warpName);

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Warp.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("warp_uuid"))));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query a server warp", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Warp> getWarp(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `%warp_data%`.`uuid` AS `warp_uuid`, `name`, `description`, `tags`, `timestamp`,
                        `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%warp_data%`
                    INNER JOIN `%saved_position_data%`
                        ON `%warp_data%`.`saved_position_id`=`%saved_position_data%`.`id`
                    INNER JOIN `%position_data%`
                        ON `%saved_position_data%`.`position_id`=`%position_data%`.`id`
                    WHERE `%warp_data%`.uuid=?;"""))) {
                statement.setString(1, uuid.toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Warp.from(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name"),
                            PositionMeta.from(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("warp_uuid"))));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query a server warp", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Teleport> getCurrentTeleport(@NotNull OnlineUser onlineUser) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `type`
                    FROM `%teleport_data%`
                    INNER JOIN `%position_data%` ON `%teleport_data%`.`destination_id` = `%position_data%`.`id`
                    WHERE `player_uuid`=?"""))) {
                statement.setString(1, onlineUser.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Teleport.builder(plugin)
                            .teleporter(onlineUser)
                            .target(Position.at(resultSet.getDouble("x"),
                                    resultSet.getDouble("y"),
                                    resultSet.getDouble("z"),
                                    resultSet.getFloat("yaw"),
                                    resultSet.getFloat("pitch"),
                                    World.from(resultSet.getString("world_name"),
                                            UUID.fromString(resultSet.getString("world_uuid"))),
                                    resultSet.getString("server_name")))
                            .type(Teleport.Type.getTeleportType(resultSet.getInt("type"))
                                    .orElse(Teleport.Type.TELEPORT))
                            .updateLastPosition(false)
                            .toTeleport());
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the current teleport of " + onlineUser.getName(), e);
        } catch (TeleportationException e) {
            e.displayMessage(onlineUser);
        }
        return Optional.empty();
    }

    @Override
    public void updateUserData(@NotNull SavedUser savedUser) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%player_data%`
                    SET `home_slots`=?, `ignoring_requests`=?
                    WHERE `uuid`=?"""))) {

                statement.setInt(1, savedUser.getHomeSlots());
                statement.setBoolean(2, savedUser.isIgnoringTeleports());
                statement.setString(3, savedUser.getUserUuid().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update user data for " + savedUser.getUsername(), e);
        }
    }

    @Override
    public void setCurrentTeleport(@NotNull User user, @Nullable Teleport teleport) {
        try (Connection connection = getConnection()) {
            // Clear the user's current teleport
            try (PreparedStatement deleteStatement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `id`=(
                        SELECT `destination_id`
                        FROM `%teleport_data%`
                        WHERE `%teleport_data%`.`player_uuid`=?
                    );"""))) {
                deleteStatement.setString(1, user.getUuid().toString());
                deleteStatement.executeUpdate();
            }

            // Set the user's teleport into the database (if it's not null)
            if (teleport != null) {
                try (PreparedStatement statement = connection.prepareStatement(format("""
                        INSERT INTO `%teleport_data%` (`player_uuid`, `destination_id`, `type`)
                        VALUES (?,?,?);"""))) {
                    statement.setString(1, user.getUuid().toString());
                    statement.setInt(2, setPosition((Position) teleport.getTarget(), connection));
                    statement.setInt(3, teleport.getType().getTypeId());

                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to clear the current teleport of " + user.getName(), e);
        }
    }

    @Override
    public Optional<Position> getLastPosition(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%player_data%`
                    INNER JOIN `%position_data%` ON `%player_data%`.`last_position` = `%position_data%`.`id`
                    WHERE `uuid`=?"""))) {
                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Position.at(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the last teleport position of " + user.getName(), e);
        }
        return Optional.empty();
    }

    @Override
    public void setLastPosition(@NotNull User user, @NotNull Position position) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement queryStatement = connection.prepareStatement(format("""
                    SELECT `last_position`
                    FROM `%player_data%`
                    INNER JOIN `%position_data%` ON `%player_data%`.last_position = `%position_data%`.`id`
                    WHERE `uuid`=?;"""))) {
                queryStatement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = queryStatement.executeQuery();
                if (resultSet.next()) {
                    // Update the last position
                    updatePosition(resultSet.getInt("last_position"), position, connection);
                } else {
                    // Set the last position
                    try (PreparedStatement updateStatement = connection.prepareStatement(format("""
                            UPDATE `%player_data%`
                            SET `last_position`=?
                            WHERE `uuid`=?;"""))) {
                        updateStatement.setInt(1, setPosition(position, connection));
                        updateStatement.setString(2, user.getUuid().toString());
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to set the last position of " + user.getName(), e);
        }
    }

    @Override
    public Optional<Position> getOfflinePosition(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%player_data%`
                    INNER JOIN `%position_data%` ON `%player_data%`.`offline_position` = `%position_data%`.`id`
                    WHERE `uuid`=?"""))) {
                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Position.at(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the offline position of " + user.getName(), e);
        }
        return Optional.empty();
    }

    @Override
    public void setOfflinePosition(@NotNull User user, @NotNull Position position) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement queryStatement = connection.prepareStatement(format("""
                    SELECT `offline_position` FROM `%player_data%`
                    INNER JOIN `%position_data%` ON `%player_data%`.`offline_position` = `%position_data%`.`id`
                    WHERE `uuid`=?;"""))) {
                queryStatement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = queryStatement.executeQuery();
                if (resultSet.next()) {
                    // Update the offline position
                    updatePosition(resultSet.getInt("offline_position"), position, connection);
                } else {
                    // Set the offline position
                    try (PreparedStatement updateStatement = connection.prepareStatement(format("""
                            UPDATE `%player_data%`
                            SET `offline_position`=?
                            WHERE `uuid`=?;"""))) {
                        updateStatement.setInt(1, setPosition(position, connection));
                        updateStatement.setString(2, user.getUuid().toString());
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to set the offline position of " + user.getName(), e);
        }
    }

    @Override
    public Optional<Position> getRespawnPosition(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%player_data%`
                    INNER JOIN `%position_data%` ON `%player_data%`.`respawn_position` = `%position_data%`.`id`
                    WHERE `uuid`=?"""))) {
                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Position.at(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            World.from(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name")));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to query the respawn position of " + user.getName(), e);
        }
        return Optional.empty();
    }

    @Override
    public void setRespawnPosition(@NotNull User user, @Nullable Position position) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement queryStatement = connection.prepareStatement(format("""
                    SELECT `respawn_position` FROM `%player_data%`
                    INNER JOIN `%position_data%` ON `%player_data%`.respawn_position = `%position_data%`.`id`
                    WHERE `uuid`=?;"""))) {
                queryStatement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = queryStatement.executeQuery();
                if (resultSet.next()) {
                    if (position == null) {
                        // Delete a respawn position
                        try (PreparedStatement deleteStatement = connection.prepareStatement(format("""
                                DELETE FROM `%position_data%`
                                WHERE `id`=(
                                    SELECT `respawn_position`
                                    FROM `%player_data%`
                                    WHERE `%player_data%`.`uuid`=?
                                );"""))) {
                            deleteStatement.setString(1, user.getUuid().toString());
                            deleteStatement.executeUpdate();
                        }
                    } else {
                        // Update the respawn position
                        updatePosition(resultSet.getInt("respawn_position"), position, connection);
                    }
                } else {
                    if (position != null) {
                        // Set a respawn position
                        try (PreparedStatement updateStatement = connection.prepareStatement(format("""
                                UPDATE `%player_data%`
                                SET `respawn_position`=?
                                WHERE `uuid`=?;"""))) {
                            updateStatement.setInt(1, setPosition(position, connection));
                            updateStatement.setString(2, user.getUuid().toString());
                            updateStatement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to set the respawn position of " + user.getName(), e);
        }
    }

    @Override
    public void saveHome(@NotNull Home home) {
        getHome(home.getUuid()).ifPresentOrElse(presentHome -> {
            try (Connection connection = getConnection()) {
                // Update the home's saved position, including metadata
                try (PreparedStatement statement = connection.prepareStatement(format("""
                        SELECT `saved_position_id` FROM `%home_data%`
                        WHERE `uuid`=?;"""))) {
                    statement.setString(1, home.getUuid().toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        updateSavedPosition(resultSet.getInt("saved_position_id"), home, connection);
                    }
                }

                // Update the home privacy
                try (PreparedStatement statement = connection.prepareStatement(format("""
                        UPDATE `%home_data%`
                        SET `public`=?
                        WHERE `uuid`=?;"""))) {
                    statement.setBoolean(1, home.isPublic());
                    statement.setString(2, home.getUuid().toString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE,
                        "Failed to update a home in the database for " + home.getOwner().getName(), e);
            }
        }, () -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(format("""
                        INSERT INTO `%home_data%` (`uuid`, `saved_position_id`, `owner_uuid`, `public`)
                        VALUES (?,?,?,?);"""))) {
                    statement.setString(1, home.getUuid().toString());
                    statement.setInt(2, setSavedPosition(home, connection));
                    statement.setString(3, home.getOwner().getUuid().toString());
                    statement.setBoolean(4, home.isPublic());

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE,
                        "Failed to set a home to the database for " + home.getOwner().getName(), e);
            }
        });
    }

    @Override
    public void saveWarp(@NotNull Warp warp) {
        getWarp(warp.getUuid()).ifPresentOrElse(presentWarp -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(format("""
                        SELECT `saved_position_id` FROM `%warp_data%`
                        WHERE `uuid`=?;"""))) {
                    statement.setString(1, warp.getUuid().toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        updateSavedPosition(resultSet.getInt("saved_position_id"), warp, connection);
                    }
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to update a warp in the database", e);
            }
        }, () -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(format("""
                        INSERT INTO `%warp_data%` (`uuid`, `saved_position_id`)
                        VALUES (?,?);"""))) {
                    statement.setString(1, warp.getUuid().toString());
                    statement.setInt(2, setSavedPosition(warp, connection));

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "Failed to add a warp to the database", e);
            }
        });
    }

    @Override
    public void deleteHome(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `%position_data%`.`id`=(
                        SELECT `position_id`
                        FROM `%saved_position_data%`
                        WHERE `%saved_position_data%`.`id`=(
                            SELECT `saved_position_id`
                            FROM `%home_data%`
                            WHERE `uuid`=?
                        )
                    );"""))) {
                statement.setString(1, uuid.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete a home from the database", e);
        }
    }

    @Override
    public int deleteAllHomes(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `%position_data%`.`id` IN (
                        SELECT `position_id`
                        FROM `%saved_position_data%`
                        WHERE `%saved_position_data%`.`id` IN (
                            SELECT `saved_position_id`
                            FROM `%home_data%`
                            WHERE `owner_uuid`=?
                        )
                    );"""))) {

                statement.setString(1, user.getUuid().toString());
                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete all homes for " + user.getName() + " from the database", e);
        }
        return 0;
    }

    @Override
    public int deleteAllHomes(@NotNull String worldName, @NotNull String serverName) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `%position_data%`.`id` IN (
                        SELECT `position_id`
                        FROM `%saved_position_data%`
                        WHERE `%saved_position_data%`.`id` IN (
                            SELECT `saved_position_id`
                            FROM `%home_data%`
                            WHERE `world_name`=?
                            AND `server_name`=?
                        )
                    );"""))) {
                statement.setString(1, worldName);
                statement.setString(2, serverName);

                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete homes in the world " + worldName + " on the server "
                                     + serverName + " from the database", e);
        }
        return 0;
    }

    @Override
    public void deleteWarp(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `%position_data%`.`id`=(
                        SELECT `position_id`
                        FROM `%saved_position_data%`
                        WHERE `%saved_position_data%`.`id`=(
                            SELECT `saved_position_id`
                            FROM `%warp_data%`
                            WHERE `uuid`=?
                        )
                    );"""))) {
                statement.setString(1, uuid.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete a warp from the database", e);
        }
    }

    @Override
    public int deleteAllWarps() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `%position_data%`.`id` IN (
                        SELECT `position_id`
                        FROM `%saved_position_data%`
                        WHERE `%saved_position_data%`.`id` IN (
                            SELECT `saved_position_id`
                            FROM `%warp_data%`
                        )
                    );"""))) {
                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete all warps from the database", e);
        }
        return 0;
    }

    @Override
    public int deleteAllWarps(@NotNull String worldName, @NotNull String serverName) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%position_data%`
                    WHERE `%position_data%`.`id` IN (
                        SELECT `position_id`
                        FROM `%saved_position_data%`
                        WHERE `%saved_position_data%`.`id` IN (
                            SELECT `saved_position_id`
                            FROM `%warp_data%`
                            WHERE `world_name`=?
                            AND `server_name`=?
                        )
                    );"""))) {
                statement.setString(1, worldName);
                statement.setString(2, serverName);

                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete warps in the world " + worldName + " on the server "
                                     + serverName + " from the database", e);
        }
        return 0;
    }

    @Override
    public void close() {
        if (dataSource != null) {
            if (!dataSource.isClosed()) {
                dataSource.close();
            }
        }
    }

}
