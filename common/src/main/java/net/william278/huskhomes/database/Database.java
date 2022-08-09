package net.william278.huskhomes.database;

import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.util.Logger;
import net.william278.huskhomes.util.ResourceReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract representation of the plugin database, storing home, warp and player data.
 * <p>
 * Implemented by different database platforms - MySQL, SQLite, etc. - as configured by the administrator.
 */
public abstract class Database {

    /**
     * Stores data about {@link User}s
     * <ol>
     *     <li>uuid - Primary key, unique; the user's minecraft account unique ID</li>
     *     <li>username - The user's username. Checked to be updated on login</li>
     *     <li>last_position - References positions table; where the user was when they last teleported</li>
     *     <li>offline_position - References positions table; the user's last online position. Updated on disconnect</li>
     *     <li>respawn_position - References positions table; where the user last updated their spawn point</li>
     *     <li>home_slots - An integer; the number of home slots this user has consumed (bought)</li>
     *     <li>ignoring_requests - A boolean; whether or not this user is ignoring teleport requests</li>
     *     <li>rtp_cooldown - A datetime timestamp; representing when this user can randomly teleport again</li>
     * </ol>
     */
    protected final String playerTableName;

    /**
     * Stores data about {@link Position}s
     * <ol>
     *     <li>id - Primary key, auto-increment; the id representing the position</li>
     *     <li>x - The double-precision x-coordinate of the position</li>
     *     <li>y - The double-precision y-coordinate of the position</li>
     *     <li>z - The double-precision z-coordinate of the position</li>
     *     <li>yaw - The float-precision yaw facing directional value of the position</li>
     *     <li>pitch - The float-precision pitch facing directional value of the position</li>
     *     <li>world_name - String file name of the world the position is on</li>
     *     <li>world_uuid - String uuid of the world the position is on</li>
     *     <li>server_name - String name of the server the position world is on</li>
     * </ol>
     */
    protected final String positionsTableName;

    /**
     * Stores {@link SavedPosition}s, including metadata about them
     * <ol>
     *     <li>id - Primary key, auto-increment; The id representing the position meta</li>
     *     <li>position_id - References the positions table; the position location data of this saved position</li>
     *     <li>name - The name string of the position represented by this metadata</li>
     *     <li>description - A description string of the position represented by this metadata</li>
     *     <li>timestamp - A datetime timestamp representing when the position this represents was created</li>
     * </ol>
     */
    protected final String savedPositionsTableName;

    /**
     * Stores {@link Home} data
     * <ol>
     *     <li>uuid - Primary key, unique; The unique id of the home</li>
     *     <li>saved_position id - References the saved position table; The id of the home saved position data</li>
     *     <li>owner_uuid - References the players table; the uuid of the person who set the home</li>
     *     <li>is_public - Boolean value; represents if the home is set to public</li>
     * </ol>
     */
    protected final String homesTableName;

    /**
     * Stores {@link Warp} data
     * <ol>
     *     <li>uuid - Primary key, unique; The unique id of the warp</li>
     *     <li>saved_position id - References the saved position table; The id of the warp saved position data</li>
     * </ol>
     */
    protected final String warpsTableName;

    /**
     * Stores data about current cross-server teleports being executed by {@link User}s
     * <ol>
     *     <li>player_uuid - Primary key, unique, references player table; represents the unique id of a teleporting player</li>
     *     <li>destination_id - References positions table; the destination of the teleporting player</li>
     * </ol>
     */
    protected final String teleportsTableName;

    /**
     * Logger instance used for database error logging
     */
    private final Logger logger;

    /**
     * Returns the {@link Logger} used to log database errors
     *
     * @return the {@link Logger} instance
     */
    protected Logger getLogger() {
        return logger;
    }

    private final ResourceReader resourceReader;

    /**
     * Loads SQL table creation schema statements from a resource file as a string array
     *
     * @param schemaFileName database script resource file to load from
     * @return Array of string-formatted table creation schema statements
     * @throws IOException if the resource could not be read
     */
    protected final String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return formatStatementTables(
                new String(resourceReader.getResource(schemaFileName)
                        .readAllBytes(), StandardCharsets.UTF_8))
                .split(";");
    }

    /**
     * Format all table name placeholder strings in a SQL statement
     *
     * @param sql the SQL statement with unformatted table name placeholders
     * @return the formatted statement, with table placeholders replaced with the correct names
     */
    protected final String formatStatementTables(@NotNull String sql) {
        return sql
                .replaceAll("%positions_table%", positionsTableName)
                .replaceAll("%players_table%", playerTableName)
                .replaceAll("%teleports_table%", teleportsTableName)
                .replaceAll("%saved_positions_table%", savedPositionsTableName)
                .replaceAll("%homes_table%", homesTableName)
                .replaceAll("%warps_table%", warpsTableName);
    }

    /**
     * Create a database instance with specified table data {@link Settings}
     *
     * @param settings {@link Settings} to fetch database configuration data from
     */
    protected Database(@NotNull Settings settings, @NotNull Logger logger, @NotNull ResourceReader resourceReader) {
        this.playerTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_PLAYER_TABLE_NAME);
        this.positionsTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_POSITIONS_TABLE_NAME);
        this.savedPositionsTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_SAVED_POSITIONS_TABLE_NAME);
        this.homesTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_HOMES_TABLE_NAME);
        this.warpsTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_WARPS_TABLE_NAME);
        this.teleportsTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_TELEPORTS_TABLE_NAME);
        this.logger = logger;
        this.resourceReader = resourceReader;
    }

    /**
     * Initialize the database and ensure tables are present; create tables if they do not exist.
     *
     * @return A future returning void when complete
     */
    public abstract boolean initialize();

    /**
     * <b>(Internal use only)</b> - Sets a position to the position table in the database
     *
     * @param position   The {@link Position} to set
     * @param connection SQL connection
     * @return The newly inserted row ID
     * @throws SQLException if an SQL exception occurs doing this
     */
    protected abstract int setPosition(@NotNull Position position, @NotNull Connection connection) throws SQLException;

    /**
     * <b>(Internal use only)</b> - Updates position data
     *
     * @param positionId ID of the position to update
     * @param position   the new position
     * @param connection SQL connection
     * @throws SQLException if an SQL exception occurs doing this
     */
    protected abstract void updatePosition(int positionId, @NotNull Position position, @NotNull Connection connection) throws SQLException;

    /**
     * <b>(Internal use only)</b> - Sets a {@link SavedPosition} to the database
     *
     * @param position   The {@link SavedPosition} to set
     * @param connection SQL connection
     * @return The newly inserted row ID
     * @throws SQLException if an SQL exception occurs doing this
     */
    protected abstract int setSavedPosition(@NotNull SavedPosition position, @NotNull Connection connection) throws SQLException;

    /**
     * <b>(Internal use only)</b> - Updates a saved position metadata
     *
     * @param savedPositionId ID of the metadata to update
     * @param savedPosition   the new saved position
     * @param connection      SQL connection
     * @throws SQLException if an SQL exception occurs doing this
     */
    protected abstract void updateSavedPosition(int savedPositionId, @NotNull SavedPosition savedPosition, @NotNull Connection connection) throws SQLException;

    /**
     * Ensure a {@link User} has a {@link UserData} entry in the database and that their username is up-to-date
     *
     * @param user The {@link User} to ensure
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> ensureUser(@NotNull User user);

    /**
     * Get a user by their username (<i>case-insensitive</i>)
     *
     * @param name Username of the {@link UserData} to get (<i>case-insensitive</i>)
     * @return A future returning an optional with the {@link UserData} present if they exist
     */
    public abstract CompletableFuture<Optional<UserData>> getUserByName(@NotNull String name);

    /**
     * Get a player by their Minecraft account {@link UUID}
     *
     * @param uuid Minecraft account {@link UUID} of the {@link UserData} to get
     * @return A future returning an optional with the {@link UserData} present if they exist
     */
    public abstract CompletableFuture<Optional<UserData>> getUser(@NotNull UUID uuid);


    /**
     * Get a list of {@link Home}s set by a {@link User}
     *
     * @param user {@link User} to get the homes of
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<List<Home>> getHomes(@NotNull User user);

    /**
     * Get a list of all {@link Warp}s that have been set
     *
     * @return A future returning a list containing all {@link Warp}s
     */
    public abstract CompletableFuture<List<Warp>> getWarps();

    /**
     * Get a list of all publicly-set {@link Home}s
     *
     * @return A future returning a list containing all publicly-set {@link Home}s
     */
    public abstract CompletableFuture<List<Home>> getPublicHomes();

    /**
     * Get a {@link Home} set by a {@link User}
     *
     * @param user     The {@link User} who set the home
     * @param homeName The <i>case-insensitive</i> name of the home to get
     * @return A future returning an optional with the {@link Home} present if it exists
     */
    public abstract CompletableFuture<Optional<Home>> getHome(@NotNull User user, @NotNull String homeName);

    /**
     * Get a {@link Home} by its unique id
     *
     * @param uuid the {@link UUID} of the home to get
     * @return A future returning an optional with the {@link Home} present if it exists
     */
    public abstract CompletableFuture<Optional<Home>> getHome(@NotNull UUID uuid);

    /**
     * Get a {@link Warp} with the given name (<i>case-insensitive</i>)
     *
     * @param warpName The <i>case-insensitive</i> name of the warp to get
     * @return A future returning an optional with the {@link Warp} present if it exists
     */
    public abstract CompletableFuture<Optional<Warp>> getWarp(@NotNull String warpName);

    /**
     * Get a {@link Warp} by its unique id
     *
     * @param uuid the {@link UUID} of the warp to get
     * @return A future returning an optional with the {@link Warp} present if it exists
     */
    public abstract CompletableFuture<Optional<Warp>> getWarp(@NotNull UUID uuid);

    /**
     * Get the current {@link Teleport} being executed by the specified {@link User}
     *
     * @param user The {@link User} to check
     * @return A future returning an optional with the {@link Teleport} present if they are teleporting cross-server
     */
    public abstract CompletableFuture<Optional<Teleport>> getCurrentTeleport(@NotNull User user);

    /**
     * Updates a user in the database with new {@link UserData}
     *
     * @param userData The {@link UserData} to update
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> updateUserData(@NotNull UserData userData);

    /**
     * Sets or clears the current {@link Teleport} being executed by a {@link User}
     *
     * @param user     The {@link User} to set the current teleport of.
     *                 Pass as {@code null} to clear the player's current teleport.<p>
     * @param teleport The {@link Teleport} to set as their current cross-server teleport
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setCurrentTeleport(@NotNull User user, @Nullable Teleport teleport);

    /**
     * Get the last teleport {@link Position} of a specified {@link User}
     *
     * @param user The {@link User} to check
     * @return A future returning an optional with the {@link Position} present if it has been set
     */
    public abstract CompletableFuture<Optional<Position>> getLastPosition(@NotNull User user);

    /**
     * Sets the last teleport {@link Position} of a {@link User}
     *
     * @param user     The {@link User} to set the last position of
     * @param position The {@link Position} to set as their last position
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setLastPosition(@NotNull User user, @NotNull Position position);

    /**
     * Get the offline {@link Position} of a specified {@link User}
     *
     * @param user The {@link User} to check
     * @return A future returning an optional with the {@link Position} present if it has been set
     */
    public abstract CompletableFuture<Optional<Position>> getOfflinePosition(@NotNull User user);

    /**
     * Sets the offline {@link Position} of a {@link User}
     *
     * @param user     The {@link User} to set the offline position of
     * @param position The {@link Position} to set as their offline position
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setOfflinePosition(@NotNull User user, @NotNull Position position);

    /**
     * Get the respawn {@link Position} of a specified {@link User}
     *
     * @param user The {@link User} to check
     * @return A future returning an optional with the {@link Position} present if it has been set
     */
    public abstract CompletableFuture<Optional<Position>> getRespawnPosition(@NotNull User user);

    /**
     * Sets or clears the respawn {@link Position} of a {@link User}
     *
     * @param user     The {@link User} to set the respawn position of
     * @param position The {@link Position} to set as their respawn position
     *                 Pass as {@code null} to clear the player's current respawn position.<p>
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setRespawnPosition(@NotNull User user, @Nullable Position position);

    /**
     * Sets or updates a {@link Home} into the home data table on the database.
     *
     * @param home The {@link Home} to set - or update - in the database.
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setHome(@NotNull Home home);

    /**
     * Sets or updates a {@link Warp} into the warp data table on the database.
     *
     * @param warp The {@link Warp} to set - or update - in the database.
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setWarp(@NotNull Warp warp);

    /**
     * Deletes a {@link Home} by the given unique id from the home table on the database.
     *
     * @param uuid {@link UUID} of the home to delete
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> deleteHome(@NotNull UUID uuid);

    /**
     * Deletes a {@link Warp} by the given unique id from the warp table on the database.
     *
     * @param uuid {@link UUID} of the warp to delete
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> deleteWarp(@NotNull UUID uuid);

    /**
     * Close any remaining connection to the database source
     */
    public abstract void terminate();


}
