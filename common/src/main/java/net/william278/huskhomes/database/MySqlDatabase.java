package net.william278.huskhomes.database;

import com.zaxxer.hikari.HikariDataSource;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

/**
 * A MySQL implementation of the plugin {@link Database}
 */
@SuppressWarnings("DuplicatedCode")
public class MySqlDatabase extends Database {

    public String host;
    public int port;
    public String database;
    public String username;
    public String password;
    public String connectionParameters;

    public int connectionPoolSize;
    public int connectionPoolIdle;
    public long connectionPoolLifetime;
    public long connectionPoolKeepAlive;
    public long connectionPoolTimeout;

    private static final String DATA_POOL_NAME = "HuskHomesHikariPool";
    private HikariDataSource dataSource;

    public MySqlDatabase(@NotNull HuskHomes plugin) {
        super(plugin);
        this.host = plugin.getSettings().getMySqlHost();
        this.port = plugin.getSettings().getMySqlPort();
        this.database = plugin.getSettings().getMySqlDatabase();
        this.username = plugin.getSettings().getMySqlUsername();
        this.password = plugin.getSettings().getMySqlPassword();
        this.connectionParameters = plugin.getSettings().getMySqlConnectionParameters();
        this.connectionPoolSize = plugin.getSettings().getMySqlConnectionPoolSize();
        this.connectionPoolIdle = plugin.getSettings().getMySqlConnectionPoolIdle();
        this.connectionPoolLifetime = plugin.getSettings().getMySqlConnectionPoolLifetime();
        this.connectionPoolKeepAlive = plugin.getSettings().getMySqlConnectionPoolKeepAlive();
        this.connectionPoolTimeout = plugin.getSettings().getMySqlConnectionPoolTimeout();
    }

    /**
     * Fetch the auto-closeable connection from the hikariDataSource
     *
     * @return The {@link Connection} to the MySQL database
     * @throws SQLException if the connection fails for some reason
     */
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public boolean initialize() {
        try {
            // Create jdbc driver connection url
            final String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + connectionParameters;
            dataSource = new HikariDataSource();
            dataSource.setJdbcUrl(jdbcUrl);

            // Authenticate
            dataSource.setUsername(username);
            dataSource.setPassword(password);

            // Set connection pool options
            dataSource.setMaximumPoolSize(connectionPoolSize);
            dataSource.setMinimumIdle(connectionPoolIdle);
            dataSource.setMaxLifetime(connectionPoolLifetime);
            dataSource.setKeepaliveTime(connectionPoolKeepAlive);
            dataSource.setConnectionTimeout(connectionPoolTimeout);
            dataSource.setPoolName(DATA_POOL_NAME);

            // Set additional connection pool properties
            dataSource.setDataSourceProperties(new Properties() {{
                put("cachePrepStmts", "true");
                put("prepStmtCacheSize", "250");
                put("prepStmtCacheSqlLimit", "2048");
                put("useServerPrepStmts", "true");
                put("useLocalSessionState", "true");
                put("useLocalTransactionState", "true");
                put("rewriteBatchedStatements", "true");
                put("cacheResultSetMetadata", "true");
                put("cacheServerConfiguration", "true");
                put("elideSetAutoCommits", "true");
                put("maintainTimeStats", "false");
            }});

            // Prepare database schema; make tables if they don't exist
            try (Connection connection = dataSource.getConnection()) {
                // Load database schema CREATE statements from schema file
                final String[] databaseSchema = getSchemaStatements("database/mysql_schema.sql");
                try (Statement statement = connection.createStatement()) {
                    for (String tableCreationStatement : databaseSchema) {
                        statement.execute(tableCreationStatement);
                    }
                }
                return true;
            } catch (SQLException | IOException e) {
                getLogger().log(Level.SEVERE, "An error occurred creating tables on the MySQL database: ", e);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An unhandled exception occurred during database setup!", e);
        }
        return false;
    }

    @Override
    public void runScript(@NotNull InputStream inputStream, @NotNull Map<String, String> replacements) {
        try (Connection connection = getConnection()) {
            final String[] scriptString;
            scriptString = new String[]{new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)};
            replacements.forEach((key, value) -> scriptString[0] = scriptString[0].replaceAll(key, value));
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                for (String statementString : scriptString[0].split(";")) {
                    statement.addBatch(statementString);
                }
                statement.executeBatch();
            }
        } catch (IOException | SQLException e) {
            getLogger().log(Level.SEVERE, "An error occurred running a script on the MySQL database: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int setPosition(@NotNull Position position, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%positions_table%` (`x`,`y`,`z`,`yaw`,`pitch`,`world_name`,`world_uuid`,`server_name`)
                        VALUES (?,?,?,?,?,?,?,?);"""),
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
    protected void updatePosition(int positionId, @NotNull Position position, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                UPDATE `%positions_table%`
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
    protected int setSavedPosition(@NotNull SavedPosition position, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%saved_positions_table%` (`position_id`, `name`, `description`, `tags`, `timestamp`)
                        VALUES (?,?,?,?,?);"""),
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, setPosition(position, connection));
            statement.setString(2, position.getMeta().getName());
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
    protected void updateSavedPosition(int savedPositionId, @NotNull SavedPosition position, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement selectStatement = connection.prepareStatement(formatStatementTables("""
                SELECT `position_id`
                FROM `%saved_positions_table%`
                WHERE `id`=?;"""))) {
            selectStatement.setInt(1, savedPositionId);

            final ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                final int positionId = resultSet.getInt("position_id");
                updatePosition(positionId, position, connection);

                try (PreparedStatement updateStatement = connection.prepareStatement(formatStatementTables("""
                        UPDATE `%saved_positions_table%`
                        SET `name`=?,
                        `description`=?,
                        `tags`=?
                        WHERE `id`=?;"""))) {
                    updateStatement.setString(1, position.getMeta().getName());
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
        getUserData(onlineUser.getUuid()).ifPresentOrElse(existingUserData -> {
                    if (!existingUserData.getUsername().equals(onlineUser.getUsername())) {
                        // Update a player's name if it has changed in the database
                        try (Connection connection = getConnection()) {
                            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                                    UPDATE `%players_table%`
                                    SET `username`=?
                                    WHERE `uuid`=?"""))) {

                                statement.setString(1, onlineUser.getUsername());
                                statement.setString(2, existingUserData.getUserUuid().toString());
                                statement.executeUpdate();
                            }
                            getLogger().log(Level.INFO, "Updated " + onlineUser.getUsername() + "'s name in the database (" + existingUserData.getUsername() + " -> " + onlineUser.getUsername() + ")");
                        } catch (SQLException e) {
                            getLogger().log(Level.SEVERE, "Failed to update a player's name on the database", e);
                        }
                    }
                },
                () -> {
                    // Insert new player data into the database
                    try (Connection connection = getConnection()) {
                        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                                INSERT INTO `%players_table%` (`uuid`,`username`)
                                VALUES (?,?);"""))) {

                            statement.setString(1, onlineUser.getUuid().toString());
                            statement.setString(2, onlineUser.getUsername());
                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        getLogger().log(Level.SEVERE, "Failed to insert a player into the database", e);
                    }
                });
    }

    @Override
    public Optional<SavedUser> getUserDataByName(@NotNull String name) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`, `rtp_cooldown`
                    FROM `%players_table%`
                    WHERE `username`=?"""))) {
                statement.setString(1, name);

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new SavedUser(
                            new User(UUID.fromString(resultSet.getString("uuid")),
                                    resultSet.getString("username")),
                            resultSet.getInt("home_slots"),
                            resultSet.getBoolean("ignoring_requests"),
                            resultSet.getTimestamp("rtp_cooldown").toInstant()));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to fetch a player by name from the database", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SavedUser> getUserData(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`, `rtp_cooldown`
                    FROM `%players_table%`
                    WHERE `uuid`=?"""))) {

                statement.setString(1, uuid.toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new SavedUser(
                            new User(UUID.fromString(resultSet.getString("uuid")),
                                    resultSet.getString("username")),
                            resultSet.getInt("home_slots"),
                            resultSet.getBoolean("ignoring_requests"),
                            resultSet.getTimestamp("rtp_cooldown").toInstant()));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to fetch a player from uuid from the database", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Home> getHomes(@NotNull User user) {
        final List<Home> userHomes = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                    FROM `%homes_table%`
                    INNER JOIN `%saved_positions_table%` ON `%homes_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                    INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                    INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                    WHERE `owner_uuid`=?
                    ORDER BY `name`;"""))) {

                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    userHomes.add(new Home(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            new Server(resultSet.getString("server_name")),
                            new PositionMeta(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            user,
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query the database for home data for:" + user.getUsername());
        }
        return userHomes;
    }

    @Override
    public List<Warp> getWarps() {
        final List<Warp> warps = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `%warps_table%`.`uuid` AS `warp_uuid`, `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%warps_table%`
                    INNER JOIN `%saved_positions_table%` ON `%warps_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                    INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                    ORDER BY `name`;"""))) {

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    warps.add(new Warp(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            new Server(resultSet.getString("server_name")),
                            new PositionMeta(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("warp_uuid"))));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query the database for warp data.");
        }
        return warps;
    }

    @Override
    public List<Home> getPublicHomes() {
        final List<Home> userHomes = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                    FROM `%homes_table%`
                    INNER JOIN `%saved_positions_table%` ON `%homes_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                    INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                    INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                    WHERE `public`=true
                    ORDER BY `name`;"""))) {

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    userHomes.add(new Home(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            new Server(resultSet.getString("server_name")),
                            new PositionMeta(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            new User(UUID.fromString(resultSet.getString("owner_uuid")),
                                    resultSet.getString("owner_username")),
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query the database for public home data");
        }
        return userHomes;
    }

    @Override
    public Optional<Home> getHome(@NotNull User user, @NotNull String homeName) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                    FROM `%homes_table%`
                    INNER JOIN `%saved_positions_table%` ON `%homes_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                    INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                    INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                    WHERE `owner_uuid`=?
                    AND `name`=?;"""))) {
                statement.setString(1, user.getUuid().toString());
                statement.setString(2, homeName);

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new Home(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            new Server(resultSet.getString("server_name")),
                            new PositionMeta(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            new User(UUID.fromString(resultSet.getString("owner_uuid")),
                                    resultSet.getString("owner_username")),
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query a player's home", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Home> getHome(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                    FROM `%homes_table%`
                    INNER JOIN `%saved_positions_table%` ON `%homes_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                    INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                    INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                    WHERE `%homes_table%`.`uuid`=?;"""))) {
                statement.setString(1, uuid.toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new Home(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            new Server(resultSet.getString("server_name")),
                            new PositionMeta(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("home_uuid")),
                            new User(UUID.fromString(resultSet.getString("owner_uuid")),
                                    resultSet.getString("owner_username")),
                            resultSet.getBoolean("public")));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query a player's home by uuid", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Warp> getWarp(@NotNull String warpName) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `%warps_table%`.`uuid` AS `warp_uuid`, `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%warps_table%`
                    INNER JOIN `%saved_positions_table%` ON `%warps_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                    INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                    WHERE `name`=?;"""))) {
                statement.setString(1, warpName);

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new Warp(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            new Server(resultSet.getString("server_name")),
                            new PositionMeta(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("warp_uuid"))));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query a server warp", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Warp> getWarp(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `%warps_table%`.`uuid` AS `warp_uuid`, `name`, `description`, `tags`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%warps_table%`
                    INNER JOIN `%saved_positions_table%` ON `%warps_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                    INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                    WHERE `%warps_table%`.uuid=?;"""))) {
                statement.setString(1, uuid.toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new Warp(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            new Server(resultSet.getString("server_name")),
                            new PositionMeta(resultSet.getString("name"),
                                    resultSet.getString("description"),
                                    resultSet.getTimestamp("timestamp").toInstant(),
                                    resultSet.getString("tags")),
                            UUID.fromString(resultSet.getString("warp_uuid"))));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query a server warp", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Teleport> getCurrentTeleport(@NotNull OnlineUser onlineUser) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `type`
                    FROM `%teleports_table%`
                    INNER JOIN `%positions_table%` ON `%teleports_table%`.`destination_id` = `%positions_table%`.`id`
                    WHERE `player_uuid`=?"""))) {
                statement.setString(1, onlineUser.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(Teleport.builder(plugin)
                            .teleporter(onlineUser)
                            .target(new Position(resultSet.getDouble("x"),
                                    resultSet.getDouble("y"),
                                    resultSet.getDouble("z"),
                                    resultSet.getFloat("yaw"),
                                    resultSet.getFloat("pitch"),
                                    new World(resultSet.getString("world_name"),
                                            UUID.fromString(resultSet.getString("world_uuid"))),
                                    resultSet.getString("server_name")))
                            .type(Teleport.Type.getTeleportType(resultSet.getInt("type"))
                                    .orElse(Teleport.Type.TELEPORT))
                            .updateLastPosition(false)
                            .toTeleport());
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query the current teleport of " + onlineUser.getUsername(), e);
        }
        return Optional.empty();
    }

    @Override
    public void updateUserData(@NotNull SavedUser savedUser) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    UPDATE `%players_table%`
                    SET `home_slots`=?, `ignoring_requests`=?, `rtp_cooldown`=?
                    WHERE `uuid`=?"""))) {

                statement.setInt(1, savedUser.homeSlots());
                statement.setBoolean(2, savedUser.ignoringTeleports());
                statement.setTimestamp(3, Timestamp.from(savedUser.rtpCooldown()));
                statement.setString(4, savedUser.getUserUuid().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to update user data for " + savedUser.getUsername() + " on the database", e);
        }
    }

    @Override
    public void setCurrentTeleport(@NotNull User user, @Nullable Teleport teleport) {
        try (Connection connection = getConnection()) {
            // Clear the user's current teleport
            try (PreparedStatement deleteStatement = connection.prepareStatement(formatStatementTables("""
                    DELETE FROM `%positions_table%`
                    WHERE `id`=(
                        SELECT `destination_id`
                        FROM `%teleports_table%`
                        WHERE `%teleports_table%`.`player_uuid`=?
                    );"""))) {
                deleteStatement.setString(1, user.getUuid().toString());
                deleteStatement.executeUpdate();
            }

            // Set the user's teleport into the database (if it's not null)
            if (teleport != null) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%teleports_table%` (`player_uuid`, `destination_id`, `type`)
                        VALUES (?,?,?);"""))) {
                    statement.setString(1, user.getUuid().toString());
                    statement.setInt(2, setPosition((Position) teleport.getTarget(), connection));
                    statement.setInt(3, teleport.getType().getTypeId());

                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to clear the current teleport of " + user.getUsername(), e);
        }
    }

    @Override
    public Optional<Position> getLastPosition(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%players_table%`
                    INNER JOIN `%positions_table%` ON `%players_table%`.`last_position` = `%positions_table%`.`id`
                    WHERE `uuid`=?"""))) {
                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new Position(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name")));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query the last teleport position of " + user.getUsername(), e);
        }
        return Optional.empty();
    }

    @Override
    public void setLastPosition(@NotNull User user, @NotNull Position position) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement queryStatement = connection.prepareStatement(formatStatementTables("""
                    SELECT `last_position`
                    FROM `%players_table%`
                    INNER JOIN `%positions_table%` ON `%players_table%`.last_position = `%positions_table%`.`id`
                    WHERE `uuid`=?;"""))) {
                queryStatement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = queryStatement.executeQuery();
                if (resultSet.next()) {
                    // Update the last position
                    updatePosition(resultSet.getInt("last_position"), position, connection);
                } else {
                    // Set the last position
                    try (PreparedStatement updateStatement = connection.prepareStatement(formatStatementTables("""
                            UPDATE `%players_table%`
                            SET `last_position`=?
                            WHERE `uuid`=?;"""))) {
                        updateStatement.setInt(1, setPosition(position, connection));
                        updateStatement.setString(2, user.getUuid().toString());
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to set the last position of " + user.getUsername(), e);
        }
    }

    @Override
    public Optional<Position> getOfflinePosition(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%players_table%`
                    INNER JOIN `%positions_table%` ON `%players_table%`.`offline_position` = `%positions_table%`.`id`
                    WHERE `uuid`=?"""))) {
                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new Position(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name")));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query the offline position of " + user.getUsername(), e);
        }
        return Optional.empty();
    }

    @Override
    public void setOfflinePosition(@NotNull User user, @NotNull Position position) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement queryStatement = connection.prepareStatement(formatStatementTables("""
                    SELECT `offline_position` FROM `%players_table%`
                    INNER JOIN `%positions_table%` ON `%players_table%`.`offline_position` = `%positions_table%`.`id`
                    WHERE `uuid`=?;"""))) {
                queryStatement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = queryStatement.executeQuery();
                if (resultSet.next()) {
                    // Update the offline position
                    updatePosition(resultSet.getInt("offline_position"), position, connection);
                } else {
                    // Set the offline position
                    try (PreparedStatement updateStatement = connection.prepareStatement(formatStatementTables("""
                            UPDATE `%players_table%`
                            SET `offline_position`=?
                            WHERE `uuid`=?;"""))) {
                        updateStatement.setInt(1, setPosition(position, connection));
                        updateStatement.setString(2, user.getUuid().toString());
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to set the offline position of " + user.getUsername(), e);
        }
    }

    @Override
    public Optional<Position> getRespawnPosition(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                    FROM `%players_table%`
                    INNER JOIN `%positions_table%` ON `%players_table%`.`respawn_position` = `%positions_table%`.`id`
                    WHERE `uuid`=?"""))) {
                statement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(new Position(resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z"),
                            resultSet.getFloat("yaw"),
                            resultSet.getFloat("pitch"),
                            new World(resultSet.getString("world_name"),
                                    UUID.fromString(resultSet.getString("world_uuid"))),
                            resultSet.getString("server_name")));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to query the respawn position of " + user.getUsername(), e);
        }
        return Optional.empty();
    }

    @Override
    public void setRespawnPosition(@NotNull User user, @Nullable Position position) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement queryStatement = connection.prepareStatement(formatStatementTables("""
                    SELECT `respawn_position` FROM `%players_table%`
                    INNER JOIN `%positions_table%` ON `%players_table%`.respawn_position = `%positions_table%`.`id`
                    WHERE `uuid`=?;"""))) {
                queryStatement.setString(1, user.getUuid().toString());

                final ResultSet resultSet = queryStatement.executeQuery();
                if (resultSet.next()) {
                    if (position == null) {
                        // Delete a respawn position
                        try (PreparedStatement deleteStatement = connection.prepareStatement(formatStatementTables("""
                                DELETE FROM `%positions_table%`
                                WHERE `id`=(
                                    SELECT `respawn_position`
                                    FROM `%players_table%`
                                    WHERE `%players_table%`.`uuid`=?
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
                        try (PreparedStatement updateStatement = connection.prepareStatement(formatStatementTables("""
                                UPDATE `%players_table%`
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
            getLogger().log(Level.SEVERE, "Failed to set the respawn position of " + user.getUsername(), e);
        }
    }

    @Override
    public void saveHome(@NotNull Home home) {
        getHome(home.getUuid()).ifPresentOrElse(presentHome -> {
            try (Connection connection = getConnection()) {
                // Update the home's saved position, including metadata
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `saved_position_id` FROM `%homes_table%`
                        WHERE `uuid`=?;"""))) {
                    statement.setString(1, home.getUuid().toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        updateSavedPosition(resultSet.getInt("saved_position_id"), home, connection);
                    }
                }

                // Update the home privacy
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        UPDATE `%homes_table%`
                        SET `public`=?
                        WHERE `uuid`=?;"""))) {
                    statement.setBoolean(1, home.isPublic());
                    statement.setString(2, home.getUuid().toString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE,
                        "Failed to update a home in the database for " + home.getOwner().getUsername(), e);
            }
        }, () -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%homes_table%` (`uuid`, `saved_position_id`, `owner_uuid`, `public`)
                        VALUES (?,?,?,?);"""))) {
                    statement.setString(1, home.getUuid().toString());
                    statement.setInt(2, setSavedPosition(home, connection));
                    statement.setString(3, home.getOwner().getUuid().toString());
                    statement.setBoolean(4, home.isPublic());

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE,
                        "Failed to set a home to the database for " + home.getOwner().getUsername(), e);
            }
        });
    }

    @Override
    public void saveWarp(@NotNull Warp warp) {
        getWarp(warp.getUuid()).ifPresentOrElse(presentWarp -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `saved_position_id` FROM `%warps_table%`
                        WHERE `uuid`=?;"""))) {
                    statement.setString(1, warp.getUuid().toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        updateSavedPosition(resultSet.getInt("saved_position_id"), warp, connection);
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to update a warp in the database", e);
            }
        }, () -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%warps_table%` (`uuid`, `saved_position_id`)
                        VALUES (?,?);"""))) {
                    statement.setString(1, warp.getUuid().toString());
                    statement.setInt(2, setSavedPosition(warp, connection));

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to add a warp to the database", e);
            }
        });
    }

    @Override
    public void deleteHome(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    DELETE FROM `%positions_table%`
                    WHERE `%positions_table%`.`id`=(
                        SELECT `position_id`
                        FROM `%saved_positions_table%`
                        WHERE `%saved_positions_table%`.`id`=(
                            SELECT `saved_position_id`
                            FROM `%homes_table%`
                            WHERE `uuid`=?
                        )
                    );"""))) {
                statement.setString(1, uuid.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to delete a home from the database", e);
        }
    }

    @Override
    public int deleteAllHomes(@NotNull User user) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    DELETE FROM `%positions_table%`
                    WHERE `%positions_table%`.`id` IN (
                        SELECT `position_id`
                        FROM `%saved_positions_table%`
                        WHERE `%saved_positions_table%`.`id` IN (
                            SELECT `saved_position_id`
                            FROM `%homes_table%`
                            WHERE `owner_uuid`=?
                        )
                    );"""))) {

                statement.setString(1, user.getUuid().toString());
                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to delete all homes for " + user.getUsername() + " from the database", e);
        }
        return 0;
    }

    @Override
    public void deleteWarp(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    DELETE FROM `%positions_table%`
                    WHERE `%positions_table%`.`id`=(
                        SELECT `position_id`
                        FROM `%saved_positions_table%`
                        WHERE `%saved_positions_table%`.`id`=(
                            SELECT `saved_position_id`
                            FROM `%warps_table%`
                            WHERE `uuid`=?
                        )
                    );"""))) {
                statement.setString(1, uuid.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to delete a warp from the database", e);
        }
    }

    @Override
    public int deleteAllWarps() {

        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                    DELETE FROM `%positions_table%`
                    WHERE `%positions_table%`.`id` IN (
                        SELECT `position_id`
                        FROM `%saved_positions_table%`
                        WHERE `%saved_positions_table%`.`id` IN (
                            SELECT `saved_position_id`
                            FROM `%warps_table%`
                        )
                    );"""))) {
                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to delete all warps from the database", e);
        }
        return 0;
    }

    @Override
    public void terminate() {
        if (dataSource != null) {
            if (!dataSource.isClosed()) {
                dataSource.close();
            }
        }
    }

}