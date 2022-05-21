package net.william278.huskhomes.data;

import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract representation of the plugin database, storing home, warp & player data.
 * <p>
 * Implemented by different database platforms - MySQL, SQLite, etc. - as configured by the administrator.
 */
public abstract class Database {

    /**
     * Stores data about {@link Player}s
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
     * Stores {@link PositionMeta}; metadata about saved positions
     * <ol>
     *     <li>id - Primary key, auto-increment; The id representing the position meta</li>
     *     <li>name - The name string of the position represented by this metadata</li>
     *     <li>description - A description string of the position represented by this metadata</li>
     *     <li>timestamp - A datetime timestamp representing when the position this represents was created</li>
     * </ol>
     */
    protected final String positionMetadataTableName;

    /**
     * Stores {@link Home} data
     * <ol>
     *     <li>uuid - Primary key, unique; The unique id of the home</li>
     *     <li>owner_uuid - References the players table; the uuid of the person who set the home</li>
     *     <li>position_id - References the positions table; The id of the home position data</li>
     *     <li>metadata_id - References the metadata table; The id of the home metadata</li>
     *     <li>is_public - Boolean value; represents if the home is set to public</li>
     * </ol>
     */
    protected final String homesTableName;

    /**
     * Stores {@link Warp} data
     * <ol>
     *     <li>uuid - Primary key, unique; The unique id of the warp</li>
     *     <li>position_id - References the positions table; The id of the warp location data</li>
     *     <li>metadata_id - References the metadata table; The id of the warp metadata</li>
     * </ol>
     */
    protected final String warpsTableName;

    /**
     * Stores data about current cross-server teleports being executed by {@link Player}s
     * <ol>
     *     <li>player_uuid - Primary key, unique, references player table; represents the unique id of a teleporting player</li>
     *     <li>destination_id - References positions table; the destination of the teleporting player</li>
     * </ol>
     */
    protected final String teleportsTableName;

    /**
     * Create a database instance with specified table data {@link Settings}
     *
     * @param settings {@link Settings} to fetch database configuration data from
     */
    protected Database(@NotNull Settings settings) {
        this.playerTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_PLAYER_TABLE_NAME);
        this.positionsTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_POSITIONS_TABLE_NAME);
        this.positionMetadataTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_POSITIONS_META_DATA_TABLE_NAME);
        this.homesTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_HOMES_TABLE_NAME);
        this.warpsTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_WARPS_TABLE_NAME);
        this.teleportsTableName = settings.
                getStringValue(Settings.ConfigOption.DATABASE_TELEPORTS_TABLE_NAME);
    }

    /**
     * Initialize the database and ensure tables are present; create tables if they do not exist.
     *
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> initialize();

    /**
     * <b>(Internal use only)</b> - Sets a position to the position table in the database
     *
     * @param position The {@link Position} to set
     * @return A future returning the inserted row ID when the operation has completed.
     */
    protected abstract CompletableFuture<Integer> setPosition(@NotNull Position position);

    /**
     * <b>(Internal use only)</b> - Sets position meta to the position metadata table in the database
     *
     * @param meta The {@link PositionMeta} to set
     * @return A future returning the inserted row ID when the operation has completed.
     */
    protected abstract CompletableFuture<Integer> setPositionMeta(@NotNull PositionMeta meta);

    /**
     * Ensure a {@link Player} has an entry in the database and that their username is up-to-date
     *
     * @param player The {@link Player} to ensure
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> ensurePlayer(@NotNull Player player);

    /**
     * Get a player by their username (<i>case-insensitive</i>)
     *
     * @param name Username of the {@link Player} to get (<i>case-insensitive</i>)
     * @return A future returning an optional with the {@link Player} present if they exist
     */
    public abstract CompletableFuture<Optional<Player>> getPlayerByName(@NotNull String name);

    /**
     * Get a list of {@link Home}s set by a {@link Player}
     *
     * @param player {@link Player} to get the homes of
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<List<Home>> getHomes(@NotNull Player player);

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
     * Get a {@link Home} set by a {@link Player}
     *
     * @param player   The {@link Player} who set the home
     * @param homeName The <i>case-insensitive</i> name of the home to get
     * @return A future returning an optional with the {@link Home} present if it exists
     */
    public abstract CompletableFuture<Optional<Home>> getHome(@NotNull Player player, @NotNull String homeName);

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
     * Get the current {@link Teleport} being executed by the specified {@link Player}
     *
     * @param player The {@link Player} to check
     * @return A future returning an optional with the {@link Teleport} present if they are teleporting cross-server
     */
    public abstract CompletableFuture<Optional<Teleport>> getCurrentTeleport(@NotNull Player player);

    /**
     * Sets or clears the current {@link Teleport} being executed by a {@link Player}
     *
     * @param player   The {@link Player} to set the current teleport of.
     *                 Pass as {@code null} to clear the player's current teleport.<p>
     * @param teleport The {@link Teleport} to set as their current cross-server teleport
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setCurrentTeleport(@NotNull Player player, @Nullable Teleport teleport);

    /**
     * Get the last teleport {@link Position} of a specified {@link Player}
     *
     * @param player The {@link Player} to check
     * @return A future returning an optional with the {@link Position} present if it has been set
     */
    public abstract CompletableFuture<Optional<Position>> getLastPosition(@NotNull Player player);

    /**
     * Sets the last teleport {@link Position} of a {@link Player}
     *
     * @param player   The {@link Player} to set the last position of
     * @param position The {@link Position} to set as their last position
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setLastPosition(@NotNull Player player, @NotNull Position position);

    /**
     * Get the offline {@link Position} of a specified {@link Player}
     *
     * @param player The {@link Player} to check
     * @return A future returning an optional with the {@link Position} present if it has been set
     */
    public abstract CompletableFuture<Optional<Position>> getOfflinePosition(@NotNull Player player);

    /**
     * Sets the offline {@link Position} of a {@link Player}
     *
     * @param player   The {@link Player} to set the offline position of
     * @param position The {@link Position} to set as their offline position
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setOfflinePosition(@NotNull Player player, @NotNull Position position);

    /**
     * Get the respawn {@link Position} of a specified {@link Player}
     *
     * @param player The {@link Player} to check
     * @return A future returning an optional with the {@link Position} present if it has been set
     */
    public abstract CompletableFuture<Optional<Position>> getRespawnPosition(@NotNull Player player);

    /**
     * Sets or clears the respawn {@link Position} of a {@link Player}
     *
     * @param player   The {@link Player} to set the respawn position of
     * @param position The {@link Position} to set as their respawn position
     *                 Pass as {@code null} to clear the player's current respawn position.<p>
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setRespawnPosition(@NotNull Player player, @Nullable Position position);

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
    public abstract CompletableFuture<Void> deleteWarp(@NotNull Warp uuid);

}
