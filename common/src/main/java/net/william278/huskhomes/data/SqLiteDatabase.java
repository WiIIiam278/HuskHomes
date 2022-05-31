package net.william278.huskhomes.data;

import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.util.Logger;
import net.william278.huskhomes.util.ResourceReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * An SQLite implementation of the plugin {@link Database}
 */
@SuppressWarnings("DuplicatedCode")
public class SqLiteDatabase extends Database {

    /**
     * Path to the SQLite HuskHomesData.db file
     */
    private final File databaseFile;

    /**
     * The name of the database file
     */
    private static final String DATABASE_FILE_NAME = "HuskHomesData.db";

    /**
     * The persistent SQLite database connection
     */
    private Connection connection;


    public SqLiteDatabase(@NotNull Settings settings, @NotNull Logger logger, @NotNull ResourceReader resourceReader) {
        super(settings, logger, resourceReader);
        this.databaseFile = new File(resourceReader.getDataFolder(), DATABASE_FILE_NAME);
    }

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            setConnection();
        } else if (connection.isClosed()) {
            setConnection();
        }
        return connection;
    }

    private void setConnection() {
        try {
            // Ensures the database file exists
            if (databaseFile.createNewFile()) {
                getLogger().log(Level.INFO, "Created the SQLite database file");
            }

            // Establish the connection using the JDBC SQLite driver
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "An exception occurred creating the database file", e);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "An SQL exception occurred initializing the SQLite database", e);
        }
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            setConnection();

            // Prepare database schema; make tables if they don't exist
            try {
                // Load database schema CREATE statements from schema file
                final String[] databaseSchema = getSchemaStatements("database/sqlite_schema.sql");
                try (Statement statement = getConnection().createStatement()) {
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
    protected void updatePositionMeta(int metaId, @NotNull PositionMeta meta, @NotNull Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(formatStatementTables("""
                UPDATE `%position_metadata_table%`
                SET `name`=?,
                `description`=?
                WHERE `id`=?;"""))) {
            statement.setString(1, meta.name);
            statement.setString(2, meta.description);
            statement.setInt(3, metaId);
            statement.executeUpdate();
        }
    }

    @Override
    public CompletableFuture<Void> ensureUser(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> getUser(player.getUuid()).thenAccept(optionalUser ->
                optionalUser.ifPresentOrElse(existingUser -> {
                            if (!existingUser.username.equals(player.getName())) {
                                // Update a player's name if it has changed in the database
                                try {
                                    try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                                            UPDATE `%players_table%`
                                            SET `username`=?
                                            WHERE `uuid`=?"""))) {

                                        statement.setString(1, player.getName());
                                        statement.setString(2, existingUser.uuid.toString());
                                        statement.executeUpdate();
                                    }
                                    getLogger().log(Level.INFO, "Updated " + player.getName() + "'s name in the database (" + existingUser.username + " -> " + player.getName() + ")");
                                } catch (SQLException e) {
                                    getLogger().log(Level.SEVERE, "Failed to update a player's name on the database", e);
                                }
                            }
                        },
                        () -> {
                            // Insert new player data into the database
                            try {
                                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
    public CompletableFuture<Optional<UserData>> getUserByName(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`, `rtp_cooldown`
                        FROM `%players_table%`
                        WHERE `username`=?"""))) {
                    statement.setString(1, name);

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new UserData(
                                UUID.fromString(resultSet.getString("uuid")),
                                resultSet.getString("username"),
                                resultSet.getInt("home_slots"),
                                resultSet.getBoolean("ignoring_requests"),
                                resultSet.getTimestamp("rtp_cooldown").toInstant().getEpochSecond()));
                    }
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to fetch a player by name from the database", e);
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<UserData>> getUser(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        SELECT `uuid`, `username`, `home_slots`, `ignoring_requests`, `rtp_cooldown`
                        FROM `%players_table%`
                        WHERE `uuid`=?"""))) {

                    statement.setString(1, uuid.toString());

                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(new UserData(
                                UUID.fromString(resultSet.getString("uuid")),
                                resultSet.getString("username"),
                                resultSet.getInt("home_slots"),
                                resultSet.getBoolean("ignoring_requests"),
                                resultSet.getTimestamp("rtp_cooldown").toInstant().getEpochSecond()));
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                        FROM `%homes_table%`
                        INNER JOIN `%positions_table%` ON `%homes_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                        INNER JOIN `%position_metadata_table%` ON `%homes_table%`.`metadata_id`=`%position_metadata_table%`.`id`
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
                                        resultSet.getTimestamp("timestamp").toInstant().getEpochSecond()),
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        SELECT `%homes_table%`.`uuid` AS `home_uuid`, `owner_uuid`, `username` AS `owner_username`, `name`, `description`, `timestamp`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`, `public`
                        FROM `%homes_table%`
                        INNER JOIN `%positions_table%` ON `%homes_table%`.`position_id`=`%positions_table%`.`id`
                        INNER JOIN `%players_table%` ON `%homes_table%`.`owner_uuid`=`%players_table%`.`uuid`
                        INNER JOIN `%position_metadata_table%` ON `%homes_table%`.`metadata_id`=`%position_metadata_table%`.`id`
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
    public CompletableFuture<Optional<Teleport>> getCurrentTeleport(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        SELECT `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`
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
                                        new Server(resultSet.getString("server_name")))));
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        UPDATE `%players_table%`
                        SET `home_slots`=?, `ignoring_requests`=?, `rtp_cooldown`=?
                        WHERE `uuid`=?"""))) {

                    statement.setInt(1, userData.homeSlots);
                    statement.setBoolean(2, userData.isIgnoringTeleports);
                    statement.setLong(3, userData.rtpCooldown);
                    statement.setString(4, userData.uuid.toString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to update user data for " + userData.username + " on the database", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setCurrentTeleport(@NotNull User user, @Nullable Teleport teleport) {
        return CompletableFuture.runAsync(() -> {
            if (teleport == null) {
                // Clear the user's current teleport
                try {
                    try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                            DELETE FROM `%teleports_table%`
                            WHERE `player_uuid`=?;"""))) {
                        statement.setString(1, user.uuid.toString());

                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    getLogger().log(Level.SEVERE, "Failed to clear the current teleport of " + user.username, e);
                }
            } else {
                // Set the user's teleport into the database
                try {
                    try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                            INSERT INTO `%teleports_table%` (`player_uuid`, `destination_id`)
                            VALUES (?,?);"""))) {
                        statement.setString(1, user.uuid.toString());
                        statement.setInt(2, setPosition(teleport.target, getConnection()));

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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
        System.out.println("teleport b3.5");
        return CompletableFuture.runAsync(() -> {
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        UPDATE `%players_table%`
                        SET `last_position`=?
                        WHERE `uuid`=?;"""))) {
                    statement.setInt(1, setPosition(position, getConnection()));
                    statement.setString(2, user.uuid.toString());

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to set the last position of " + user.username, e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Position>> getOfflinePosition(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        UPDATE `%players_table%`
                        SET `offline_position`=?
                        WHERE `uuid`=?;"""))) {
                    statement.setInt(1, setPosition(position, getConnection()));
                    statement.setString(2, user.uuid.toString());

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Failed to set the offline position of " + user.username, e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Position>> getRespawnPosition(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
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
            if (position == null) {
                // Clear the respawn position
                try {
                    try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                            UPDATE `%players_table%`
                            SET `respawn_position`=NULL
                            WHERE `uuid`=?;"""))) {
                        statement.setString(1, user.uuid.toString());

                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    getLogger().log(Level.SEVERE, "Failed to set the respawn position of " + user.username, e);
                }
            } else {
                // Set the respawn position
                try {
                    try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                            UPDATE `%players_table%`
                            SET `respawn_position`=?
                            WHERE `uuid`=?;"""))) {
                        statement.setInt(1, setPosition(position, getConnection()));
                        statement.setString(2, user.uuid.toString());

                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    getLogger().log(Level.SEVERE, "Failed to set the respawn position of " + user.username, e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> setHome(@NotNull Home home) {
        return CompletableFuture.runAsync(() -> getHome(home.uuid)
                .thenAccept(existingHome -> existingHome.ifPresentOrElse(presentHome -> {
                    try {
                        try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                                SELECT `position_id`, `metadata_id` FROM `%homes_table%`
                                WHERE `uuid`=?;"""))) {
                            statement.setString(1, home.uuid.toString());

                            final ResultSet resultSet = statement.executeQuery();
                            if (resultSet.next()) {
                                updatePosition(resultSet.getInt("position_id"), home, connection);
                                updatePositionMeta(resultSet.getInt("metadata_id"), home.meta, connection);
                            }
                        }
                    } catch (SQLException e) {
                        getLogger().log(Level.SEVERE,
                                "Failed to update a home in the database for " + home.owner.username, e);
                    }
                }, () -> {
                    try {
                        try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                                INSERT INTO `%homes_table%` (`uuid`, `owner_uuid`, `position_id`, `metadata_id`, `public`)
                                VALUES (?,?,?,?,?);"""))) {
                            statement.setString(1, home.uuid.toString());
                            statement.setString(2, home.owner.uuid.toString());
                            statement.setInt(3, setPosition(home, getConnection()));
                            statement.setInt(4, setPositionMeta(home.meta, getConnection()));
                            statement.setBoolean(5, home.isPublic);

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
                    try {
                        try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                                SELECT `position_id`, `metadata_id` FROM `%warps_table%`
                                WHERE `uuid`=?;"""))) {
                            statement.setString(1, warp.uuid.toString());

                            final ResultSet resultSet = statement.executeQuery();
                            if (resultSet.next()) {
                                updatePosition(resultSet.getInt("position_id"), warp, connection);
                                updatePositionMeta(resultSet.getInt("metadata_id"), warp.meta, connection);
                            }
                        }
                    } catch (SQLException e) {
                        getLogger().log(Level.SEVERE, "Failed to update a warp in the database", e);
                    }
                }, () -> {
                    try {
                        try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                                INSERT INTO `%warps_table%` (`uuid`, `position_id`, `metadata_id`)
                                VALUES (?,?,?);"""))) {
                            statement.setString(1, warp.uuid.toString());
                            statement.setInt(2, setPosition(warp, getConnection()));
                            statement.setInt(3, setPositionMeta(warp.meta, getConnection()));

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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        DELETE FROM `%homes_table%`
                        WHERE `uuid`=?;"""))) {
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
            try {
                try (PreparedStatement statement = getConnection().prepareStatement(formatStatementTables("""
                        DELETE FROM `%warps_table%`
                        WHERE `uuid`=?;"""))) {
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
        try {
            if (connection != null) {
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to properly close the SQLite connection");
        }
    }

}