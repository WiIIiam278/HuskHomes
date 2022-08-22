package net.william278.huskhomes.database;

import com.zaxxer.hikari.HikariDataSource;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportType;
import net.william278.huskhomes.util.Logger;
import net.william278.huskhomes.util.ResourceReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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

    private final Settings.ConnectionPoolOptions poolOptions;

    private static final String DATA_POOL_NAME = "HuskHomesHikariPool";

    private HikariDataSource dataSource;

    public MySqlDatabase(@NotNull Settings settings, @NotNull Logger logger, @NotNull ResourceReader resourceReader) {
        super(settings, logger, resourceReader);
        this.host = settings.mySqlHost;
        this.port = settings.mySqlPort;
        this.database = settings.mySqlDatabase;
        this.username = settings.mySqlUsername;
        this.password = settings.mySqlPassword;
        this.connectionParameters = settings.mySqlConnectionParameters;
        this.poolOptions = settings.mySqlPoolOptions;
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

            // Set various additional parameters
            dataSource.setMaximumPoolSize(poolOptions.size);
            dataSource.setMinimumIdle(poolOptions.idle);
            dataSource.setMaxLifetime(poolOptions.lifetime);
            dataSource.setKeepaliveTime(poolOptions.keepalive);
            dataSource.setConnectionTimeout(poolOptions.timeout);
            dataSource.setPoolName(DATA_POOL_NAME);

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
    protected int setPosition(@NotNull Position position, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%positions_table%` (`x`,`y`,`z`,`yaw`,`pitch`,`world_name`,`world_uuid`,`server_name`)
                        VALUES (?,?,?,?,?,?,?,?);"""),
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setDouble(1, position.x);
            statement.setDouble(2, position.y);
            statement.setDouble(3, position.z);
            statement.setFloat(4, position.yaw);
            statement.setFloat(5, position.pitch);
            statement.setString(6, position.world.name);
            statement.setString(7, position.world.uuid.toString());
            statement.setString(8, position.server.name);
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
            statement.setDouble(1, position.x);
            statement.setDouble(2, position.y);
            statement.setDouble(3, position.z);
            statement.setFloat(4, position.yaw);
            statement.setFloat(5, position.pitch);
            statement.setString(6, position.world.uuid.toString());
            statement.setString(7, position.world.name);
            statement.setString(8, position.server.name);
            statement.setDouble(9, positionId);
            statement.executeUpdate();
        }
    }

    @Override
    protected int setSavedPosition(@NotNull SavedPosition position, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%saved_positions_table%` (`position_id`, `name`, `description`, `timestamp`)
                        VALUES (?,?,?,?);"""),
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, setPosition(position, connection));
            statement.setString(2, position.meta.name);
            statement.setString(3, position.meta.description);
            statement.setTimestamp(4, Timestamp.from(position.meta.creationTime));
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
                        `description`=?
                        WHERE `id`=?;"""))) {
                    updateStatement.setString(1, position.meta.name);
                    updateStatement.setString(2, position.meta.description);
                    updateStatement.setInt(3, savedPositionId);
                    updateStatement.executeUpdate();
                }
            }
        }
    }

    @Override
    public CompletableFuture<Void> ensureUser(@NotNull User onlineUser) {
        return CompletableFuture.runAsync(() -> getUserData(onlineUser.uuid).thenAccept(optionalUser ->
                optionalUser.ifPresentOrElse(existingUserData -> {
                            if (!existingUserData.getUsername().equals(onlineUser.username)) {
                                // Update a player's name if it has changed in the database
                                try (Connection connection = getConnection()) {
                                    try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                                            UPDATE `%players_table%`
                                            SET `username`=?
                                            WHERE `uuid`=?"""))) {

                                        statement.setString(1, onlineUser.username);
                                        statement.setString(2, existingUserData.getUserUuid().toString());
                                        statement.executeUpdate();
                                    }
                                    getLogger().log(Level.INFO, "Updated " + onlineUser.username + "'s name in the database (" + existingUserData.getUsername() + " -> " + onlineUser.username + ")");
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

                                    statement.setString(1, onlineUser.uuid.toString());
                                    statement.setString(2, onlineUser.username);
                                    statement.executeUpdate();
                                }
                            } catch (SQLException e) {
                                getLogger().log(Level.SEVERE, "Failed to insert a player into the database", e);
                            }
                        })));
    }

    @Override
    public CompletableFuture<Optional<UserData>> getUserDataByName(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`, `rtp_cooldown`
                        FROM `%players_table%`
                        WHERE `username`=?"""))) {
                    statement.setString(1, name);

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new UserData(
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
        });
    }

    @Override
    public CompletableFuture<Optional<UserData>> getUserData(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`, `rtp_cooldown`
                        FROM `%players_table%`
                        WHERE `uuid`=?"""))) {

                    statement.setString(1, uuid.toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new UserData(
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
        });
    }

    @Override
    public CompletableFuture<List<Home>> getHomes(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            final List<Home> userHomes = new ArrayList<>();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                        FROM `%homes_table%`
                        INNER JOIN `%saved_positions_table%` ON `%homes_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                        INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                        WHERE `owner_uuid`=?
                        ORDER BY `name`;"""))) {

                    statement.setString(1, user.uuid.toString());

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
                                        resultSet.getTimestamp("timestamp").toInstant()),
                                UUID.fromString(resultSet.getString("home_uuid")),
                                user,
                                resultSet.getBoolean("public")));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query the database for home data for:" + user.username);
            }
            return userHomes;
        });
    }

    @Override
    public CompletableFuture<List<Warp>> getWarps() {
        return CompletableFuture.supplyAsync(() -> {
            final List<Warp> warps = new ArrayList<>();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%warps_table%`.`uuid` AS `warp_uuid`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
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
                                        resultSet.getTimestamp("timestamp").toInstant()),
                                UUID.fromString(resultSet.getString("warp_uuid"))));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query the database for warp data.");
            }
            return warps;
        });
    }

    @Override
    public CompletableFuture<List<Home>> getPublicHomes() {
        return CompletableFuture.supplyAsync(() -> {
            final List<Home> userHomes = new ArrayList<>();
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
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
                                        resultSet.getTimestamp("timestamp").toInstant()),
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
        });
    }

    @Override
    public CompletableFuture<Optional<Home>> getHome(@NotNull User user, @NotNull String homeName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                        FROM `%homes_table%`
                        INNER JOIN `%saved_positions_table%` ON `%homes_table%`.`saved_position_id`=`%saved_positions_table%`.`id`
                        INNER JOIN `%positions_table%` ON `%saved_positions_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                        WHERE `owner_uuid`=?
                        AND `name`=?;"""))) {
                    statement.setString(1, user.uuid.toString());
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
                                        resultSet.getTimestamp("timestamp").toInstant()),
                                UUID.fromString(resultSet.getString("home_uuid")),
                                new User(UUID.fromString(resultSet.getString("owner_uuid")),
                                        resultSet.getString("owner_username")),
                                resultSet.getBoolean("public")));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query a player's home", e);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<Home>> getHome(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
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
                                        resultSet.getTimestamp("timestamp").toInstant()),
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
        });
    }

    @Override
    public CompletableFuture<Optional<Warp>> getWarp(@NotNull String warpName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%warps_table%`.`uuid` AS `warp_uuid`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
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
                                        resultSet.getTimestamp("timestamp").toInstant()),
                                UUID.fromString(resultSet.getString("warp_uuid"))));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query a server warp", e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<Warp>> getWarp(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%warps_table%`.`uuid` AS `warp_uuid`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
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
                                        resultSet.getTimestamp("timestamp").toInstant()),
                                UUID.fromString(resultSet.getString("warp_uuid"))));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query a server warp", e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<Teleport>> getCurrentTeleport(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `type`
                        FROM `%teleports_table%`
                        INNER JOIN `%positions_table%` ON `%teleports_table%`.`destination_id` = `%positions_table%`.`id`
                        WHERE `player_uuid`=?"""))) {
                    statement.setString(1, user.uuid.toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new Teleport(user,
                                new Position(resultSet.getDouble("x"),
                                        resultSet.getDouble("y"),
                                        resultSet.getDouble("z"),
                                        resultSet.getFloat("yaw"),
                                        resultSet.getFloat("pitch"),
                                        new World(resultSet.getString("world_name"),
                                                UUID.fromString(resultSet.getString("world_uuid"))),
                                        new Server(resultSet.getString("server_name"))),
                                TeleportType.getTeleportType(resultSet.getInt("type")).orElse(TeleportType.TELEPORT)));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query the current teleport of " + user.username, e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> updateUserData(@NotNull UserData userData) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        UPDATE `%players_table%`
                        SET `home_slots`=?, `ignoring_requests`=?, `rtp_cooldown`=?
                        WHERE `uuid`=?"""))) {

                    statement.setInt(1, userData.homeSlots());
                    statement.setBoolean(2, userData.ignoringTeleports());
                    statement.setTimestamp(3, Timestamp.from(userData.rtpCooldown()));
                    statement.setString(4, userData.getUserUuid().toString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to update user data for " + userData.getUsername() + " on the database", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setCurrentTeleport(@NotNull User user, @Nullable Teleport teleport) {
        return CompletableFuture.runAsync(() -> {
            // Clear the user's current teleport
            try (Connection connection = getConnection()) {
                try (PreparedStatement deleteStatement = connection.prepareStatement(formatStatementTables("""
                        DELETE FROM `%positions_table%`
                        WHERE `id`=(
                            SELECT `destination_id`
                            FROM `%teleports_table%`
                            WHERE `%teleports_table%`.`player_uuid`=?
                        );"""))) {
                    deleteStatement.setString(1, user.uuid.toString());
                    deleteStatement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to clear the current teleport of " + user.username, e);
            }

            // Set the user's teleport into the database (if it's not null)
            if (teleport != null) {
                try (Connection connection = getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                            INSERT INTO `%teleports_table%` (`player_uuid`, `destination_id`, `type`)
                            VALUES (?,?,?);"""))) {
                        statement.setString(1, user.uuid.toString());
                        statement.setInt(2, setPosition(teleport.target, connection));
                        statement.setInt(3, teleport.type.typeId);

                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    getLogger().log(Level.SEVERE, "Failed to set the current teleport of " + user.username, e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Position>> getLastPosition(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                        FROM `%players_table%`
                        INNER JOIN `%positions_table%` ON `%players_table%`.`last_position` = `%positions_table%`.`id`
                        WHERE `uuid`=?"""))) {
                    statement.setString(1, user.uuid.toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new Position(resultSet.getDouble("x"),
                                resultSet.getDouble("y"),
                                resultSet.getDouble("z"),
                                resultSet.getFloat("yaw"),
                                resultSet.getFloat("pitch"),
                                new World(resultSet.getString("world_name"),
                                        UUID.fromString(resultSet.getString("world_uuid"))),
                                new Server(resultSet.getString("server_name"))));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query the last teleport position of " + user.username, e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> setLastPosition(@NotNull User user, @NotNull Position position) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement queryStatement = connection.prepareStatement(formatStatementTables("""
                        SELECT `last_position`
                        FROM `%players_table%`
                        INNER JOIN `%positions_table%` ON `%players_table%`.last_position = `%positions_table%`.`id`
                        WHERE `uuid`=?;"""))) {
                    queryStatement.setString(1, user.uuid.toString());

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
                            updateStatement.setString(2, user.uuid.toString());
                            updateStatement.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to set the last position of " + user.username, e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Position>> getOfflinePosition(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                        FROM `%players_table%`
                        INNER JOIN `%positions_table%` ON `%players_table%`.`offline_position` = `%positions_table%`.`id`
                        WHERE `uuid`=?"""))) {
                    statement.setString(1, user.uuid.toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new Position(resultSet.getDouble("x"),
                                resultSet.getDouble("y"),
                                resultSet.getDouble("z"),
                                resultSet.getFloat("yaw"),
                                resultSet.getFloat("pitch"),
                                new World(resultSet.getString("world_name"),
                                        UUID.fromString(resultSet.getString("world_uuid"))),
                                new Server(resultSet.getString("server_name"))));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query the offline position of " + user.username, e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> setOfflinePosition(@NotNull User user, @NotNull Position position) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement queryStatement = connection.prepareStatement(formatStatementTables("""
                        SELECT `offline_position` FROM `%players_table%`
                        INNER JOIN `%positions_table%` ON `%players_table%`.`offline_position` = `%positions_table%`.`id`
                        WHERE `uuid`=?;"""))) {
                    queryStatement.setString(1, user.uuid.toString());

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
                            updateStatement.setString(2, user.uuid.toString());
                            updateStatement.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to set the offline position of " + user.username, e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Position>> getRespawnPosition(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
                        FROM `%players_table%`
                        INNER JOIN `%positions_table%` ON `%players_table%`.`respawn_position` = `%positions_table%`.`id`
                        WHERE `uuid`=?"""))) {
                    statement.setString(1, user.uuid.toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new Position(resultSet.getDouble("x"),
                                resultSet.getDouble("y"),
                                resultSet.getDouble("z"),
                                resultSet.getFloat("yaw"),
                                resultSet.getFloat("pitch"),
                                new World(resultSet.getString("world_name"),
                                        UUID.fromString(resultSet.getString("world_uuid"))),
                                new Server(resultSet.getString("server_name"))));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query the respawn position of " + user.username, e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> setRespawnPosition(@NotNull User user, @Nullable Position position) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement queryStatement = connection.prepareStatement(formatStatementTables("""
                        SELECT `respawn_position` FROM `%players_table%`
                        INNER JOIN `%positions_table%` ON `%players_table%`.respawn_position = `%positions_table%`.`id`
                        WHERE `uuid`=?;"""))) {
                    queryStatement.setString(1, user.uuid.toString());

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
                                deleteStatement.setString(1, user.uuid.toString());
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
                                updateStatement.setString(2, user.uuid.toString());
                                updateStatement.executeUpdate();
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to set the respawn position of " + user.username, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setHome(@NotNull Home home) {
        return CompletableFuture.runAsync(() -> getHome(home.uuid)
                .thenAccept(existingHome -> existingHome.ifPresentOrElse(presentHome -> {
                    try (Connection connection = getConnection()) {
                        // Update the home's saved position, including metadata
                        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                                SELECT `saved_position_id` FROM `%homes_table%`
                                WHERE `uuid`=?;"""))) {
                            statement.setString(1, home.uuid.toString());

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
                            statement.setBoolean(1, home.isPublic);
                            statement.setString(2, home.uuid.toString());
                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        getLogger().log(Level.SEVERE,
                                "Failed to update a home in the database for " + home.owner.username, e);
                    }
                }, () -> {
                    try (Connection connection = getConnection()) {
                        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                                INSERT INTO `%homes_table%` (`uuid`, `saved_position_id`, `owner_uuid`, `public`)
                                VALUES (?,?,?,?);"""))) {
                            statement.setString(1, home.uuid.toString());
                            statement.setInt(2, setSavedPosition(home, connection));
                            statement.setString(3, home.owner.uuid.toString());
                            statement.setBoolean(4, home.isPublic);

                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        getLogger().log(Level.SEVERE,
                                "Failed to set a home to the database for " + home.owner.username, e);
                    }
                })));
    }

    @Override
    public CompletableFuture<Void> setWarp(@NotNull Warp warp) {
        return CompletableFuture.runAsync(() -> getWarp(warp.uuid)
                .thenAccept(existingHome -> existingHome.ifPresentOrElse(presentWarp -> {
                    try (Connection connection = getConnection()) {
                        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                                SELECT `saved_position_id` FROM `%warps_table%`
                                WHERE `uuid`=?;"""))) {
                            statement.setString(1, warp.uuid.toString());

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
                            statement.setString(1, warp.uuid.toString());
                            statement.setInt(2, setSavedPosition(warp, connection));

                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        getLogger().log(Level.SEVERE, "Failed to add a warp to the database", e);
                    }
                })));
    }

    @Override
    public CompletableFuture<Void> deleteHome(@NotNull UUID uuid) {
        return CompletableFuture.runAsync(() -> {
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
        });
    }

    @Override
    public CompletableFuture<Void> deleteWarp(@NotNull UUID uuid) {
        return CompletableFuture.runAsync(() -> {
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
        });
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