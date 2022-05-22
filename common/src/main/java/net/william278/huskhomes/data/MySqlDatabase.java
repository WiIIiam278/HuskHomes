package net.william278.huskhomes.data;

import com.zaxxer.hikari.HikariDataSource;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.util.Logger;
import net.william278.huskhomes.util.ResourceReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

//todo write (hikari for this; direct driver for SQLite probably)

/**
 * A MySQL implementation of the plugin {@link Database}
 */
@SuppressWarnings("DuplicatedCode")
public class MySqlDatabase extends Database {

    /**
     * MySQL server hostname
     */
    private final String mySqlHost;
    /**
     * MySQL server port
     */
    private final int mySqlPort;
    /**
     * Database to use on the MySQL server
     */
    private final String mySqlDatabaseName;
    /**
     * MySQL username for accessing the database
     */
    private final String mySqlUsername;
    /**
     * MySQL password for accessing the database
     */
    private final String mySqlPassword;
    /**
     * Additional connection parameters, formatted as a jdbc connection string
     */
    private final String mySqlConnectionParameters;

    private final int hikariMaximumPoolSize;
    private final int hikariMinimumIdle;
    private final int hikariMaximumLifetime;
    private final int hikariKeepAliveTime;
    private final int hikariConnectionTimeOut;

    private static final String DATA_POOL_NAME = "HuskHomesHikariPool";

    private HikariDataSource dataSource;

    public MySqlDatabase(@NotNull Settings settings, @NotNull Logger logger, @NotNull ResourceReader resourceReader) {
        super(settings, logger, resourceReader);
        this.mySqlHost = settings.getStringValue(Settings.ConfigOption.DATABASE_HOST);
        this.mySqlPort = settings.getIntegerValue(Settings.ConfigOption.DATABASE_PORT);
        this.mySqlDatabaseName = settings.getStringValue(Settings.ConfigOption.DATABASE_NAME);
        this.mySqlUsername = settings.getStringValue(Settings.ConfigOption.DATABASE_USERNAME);
        this.mySqlPassword = settings.getStringValue(Settings.ConfigOption.DATABASE_PASSWORD);
        this.mySqlConnectionParameters = settings.getStringValue(Settings.ConfigOption.DATABASE_CONNECTION_PARAMS);

        this.hikariMaximumPoolSize = settings.getIntegerValue(Settings.ConfigOption.DATABASE_CONNECTION_POOL_MAX_SIZE);
        this.hikariMinimumIdle = settings.getIntegerValue(Settings.ConfigOption.DATABASE_CONNECTION_POOL_MIN_IDLE);
        this.hikariMaximumLifetime = settings.getIntegerValue(Settings.ConfigOption.DATABASE_CONNECTION_POOL_MAX_LIFETIME);
        this.hikariKeepAliveTime = settings.getIntegerValue(Settings.ConfigOption.DATABASE_CONNECTION_POOL_KEEPALIVE);
        this.hikariConnectionTimeOut = settings.getIntegerValue(Settings.ConfigOption.DATABASE_CONNECTION_POOL_TIMEOUT);
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
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            // Create jdbc driver connection url
            final String jdbcUrl = "jdbc:mysql://" + mySqlHost + ":" + mySqlPort + "/" + mySqlDatabaseName + mySqlConnectionParameters;
            dataSource = new HikariDataSource();
            dataSource.setJdbcUrl(jdbcUrl);

            // Authenticate
            dataSource.setUsername(mySqlUsername);
            dataSource.setPassword(mySqlPassword);

            // Set various additional parameters
            dataSource.setMaximumPoolSize(hikariMaximumPoolSize);
            dataSource.setMinimumIdle(hikariMinimumIdle);
            dataSource.setMaxLifetime(hikariMaximumLifetime);
            dataSource.setKeepaliveTime(hikariKeepAliveTime);
            dataSource.setConnectionTimeout(hikariConnectionTimeOut);
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
            } catch (SQLException | IOException e) {
                getLogger().log(Level.SEVERE, "An error occurred creating tables on the MySQL database: ", e);
            }
        });
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

            return statement.getGeneratedKeys().getInt(1);
        }
    }

    @Override
    protected int setPositionMeta(@NotNull PositionMeta meta, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        INSERT INTO `%position_metadata_table%` (`name`, `description`, `timestamp`)
                        VALUES (?,?,?);"""),
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, meta.name);
            statement.setString(2, meta.description);
            statement.setTimestamp(3, new Timestamp(Instant.ofEpochSecond(meta.timestamp).toEpochMilli()));
            statement.executeUpdate();

            return statement.getGeneratedKeys().getInt(1);
        }
    }

    @Override
    public CompletableFuture<Void> ensurePlayer(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> getUser(player.getUuid()).thenAccept(optionalUser ->
                optionalUser.ifPresentOrElse(existingUser -> {
                            if (!existingUser.username().equals(player.getName())) {
                                // Update a player's name if it has changed in the database
                                try (Connection connection = getConnection()) {
                                    try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                                            UPDATE `%players_table%`
                                            SET `username`=?
                                            WHERE `uuid`=?"""))) {

                                        statement.setString(1, player.getName());
                                        statement.setString(2, existingUser.uuid().toString());
                                        statement.executeUpdate();
                                    }
                                    getLogger().log(Level.INFO, "Updated " + player.getName() + "'s name in the database (" + existingUser.username() + " -> " + player.getName() + ")");
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

                                    statement.setString(1, player.getUuid().toString());
                                    statement.setString(2, player.getName());
                                    statement.executeUpdate();
                                }
                            } catch (SQLException e) {
                                getLogger().log(Level.SEVERE, "Failed to insert a player into the database", e);
                            }
                        })));
    }

    @Override
    public CompletableFuture<Optional<User>> getUserByName(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `uuid`, `username`
                        FROM `%players_table%`
                        WHERE `username`=?"""))) {
                    statement.setString(1, name);

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new User(UUID.fromString(resultSet.getString("uuid")),
                                resultSet.getString("username")));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to fetch a player by name from the database", e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<User>> getUser(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `uuid`, `username`
                        FROM `%players_table%`
                        WHERE `uuid`=?"""))) {

                    statement.setString(1, uuid.toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new User(UUID.fromString(resultSet.getString("uuid")),
                                resultSet.getString("username")));
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
                        INNER JOIN `%positions_table%` ON `%homes_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                        INNER JOIN `%position_metadata_table%` ON `%homes_table%`.`metadata_id`=`%position_metadata_table%`.`id`
                        WHERE `owner_uuid`=?
                        ORDER BY `name`;"""))) {

                    statement.setString(1, user.uuid().toString());

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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
                                UUID.fromString(resultSet.getString("home_uuid")),
                                user,
                                resultSet.getBoolean("public")));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query the database for home data for:" + user.username());
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
                        INNER JOIN `%positions_table%` ON `%warps_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%position_metadata_table%` ON `%warps_table%`.`metadata_id`=`%position_metadata_table%`.`id`
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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
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
                        INNER JOIN `%positions_table%` ON `%homes_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                        INNER JOIN `%position_metadata_table%` ON `%homes_table%`.`metadata_id`=`%position_metadata_table%`.`id`
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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
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
                                                INNER JOIN `%positions_table%` ON `%homes_table%`.`position_id`=`%positions_table%`.`id`
                                                INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                                                INNER JOIN `%position_metadata_table%` ON `%homes_table%`.`metadata_id`=`%position_metadata_table%`.`id`
                                                WHERE `owner_uuid`=?
                                                AND `name`=?;"""))) {
                    statement.setString(1, user.uuid().toString());
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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
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
        });
    }

    @Override
    public CompletableFuture<Optional<Home>> getHome(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                        SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                                                FROM `%homes_table%`
                                                INNER JOIN `%positions_table%` ON `%homes_table%`.`position_id`=`%positions_table%`.`id`
                                                INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                                                INNER JOIN `%position_metadata_table%` ON `%homes_table%`.`metadata_id`=`%position_metadata_table%`.`id`
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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
                                UUID.fromString(resultSet.getString("home_uuid")),
                                new User(UUID.fromString(resultSet.getString("owner_uuid")),
                                        resultSet.getString("owner_username")),
                                resultSet.getBoolean("public")));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to query a player's home by UUID", e);
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
                        INNER JOIN `%positions_table%` ON `%warps_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%position_metadata_table%` ON `%warps_table%`.`metadata_id`=`%position_metadata_table%`.`id`
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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
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
                        INNER JOIN `%positions_table%` ON `%warps_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%position_metadata_table%` ON `%warps_table%`.`metadata_id`=`%position_metadata_table%`.`id`
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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
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
    public CompletableFuture<Optional<Teleport>> getCurrentTeleport(@NotNull User player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setCurrentTeleport(@NotNull User player, @Nullable Teleport teleport) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Position>> getLastPosition(@NotNull User player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setLastPosition(@NotNull User player, @NotNull Position position) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Position>> getOfflinePosition(@NotNull User player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setOfflinePosition(@NotNull User player, @NotNull Position position) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Position>> getRespawnPosition(@NotNull User player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setRespawnPosition(@NotNull User player, @Nullable Position position) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setHome(@NotNull Home home) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setWarp(@NotNull Warp warp) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteHome(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteWarp(@NotNull Warp uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Long> getRtpCooldown(@NotNull User player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setRtpCooldown(@NotNull User player, long position) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> getHomeSlots(@NotNull User player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setHomeSlots(@NotNull User player, int homeSlots) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> getIsIgnoringTeleports(@NotNull User player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setIgnoringTeleports(@NotNull User player, boolean ignoringTeleports) {
        return null;
    }

}