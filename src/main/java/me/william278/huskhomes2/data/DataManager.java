package me.william278.huskhomes2.data;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import me.william278.huskhomes2.teleport.points.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataManager {

    // Return a player's ID  from their UUID
    private static Integer getPlayerId(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;")) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final int playerID = resultSet.getInt("player_id");
                statement.close();
                return playerID;
            }

        }
        return null;
    }

    // Return a player's ID  from their UUID
    public static Integer getPlayerId(String playerUsername, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?;")) {
            statement.setString(1, playerUsername);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final int playerID = resultSet.getInt("player_id");
                statement.close();
                return playerID;
            }

        }
        return null;
    }

    // Return an integer from the player table from a player ID
    private static Integer getPlayerInteger(Integer playerID, String column, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `player_id`=?;")) {
            statement.setInt(1, playerID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final int playerInteger = resultSet.getInt(column);
                statement.close();
                return playerInteger;
            }

        }
        return null;
    }

    // Return an integer from the player table from a player object
    private static Integer getPlayerInteger(Player p, String column, Connection connection) throws SQLException {
        return getPlayerInteger(getPlayerId(p.getUniqueId(), connection), column, connection);
    }

    // Return a string from the player table from a player ID
    private static String getPlayerString(Integer playerID, String column, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `player_id`=?;")) {
            statement.setInt(1, playerID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final String playerString = resultSet.getString(column);
                statement.close();
                return playerString;
            }

        }
        return null;
    }

    // Return if the player is teleporting
    public static Boolean isPlayerTeleporting(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;")) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final boolean isTeleporting = resultSet.getBoolean("is_teleporting");
                statement.close();
                return isTeleporting;
            }

        }
        return null;
    }

    // Return if the player is ignoring teleport requests
    public static Boolean isPlayerIgnoringRequests(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;")) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final boolean isIgnoringRequests = resultSet.getBoolean("is_ignoring_requests");
                statement.close();
                return isIgnoringRequests;
            }

        }
        return null;
    }

    // Return a player's UUID
    public static UUID getPlayerUUID(int playerID, Connection connection) throws SQLException {
        String uuidString = getPlayerString(playerID, "user_uuid", connection);
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        } else {
            return null;
        }
    }

    // Return a player's username
    public static String getPlayerUsername(int playerID, Connection connection) throws SQLException {
        return getPlayerString(playerID, "username", connection);
    }

    // Return how many homes the player has set
    public static int getPlayerHomeCount(Player p, Connection connection) throws SQLException {
        return getPlayerHomes(p.getName(), connection).size();
    }

    // Increment the number of home slots a player has
    public static void incrementPlayerHomeSlots(Player p, Connection connection) throws SQLException {
        setPlayerHomeSlots(p.getUniqueId(), (getPlayerHomeSlots(p, connection) + 1), connection);
    }

    // Return how many home slots a player has
    public static Integer getPlayerHomeSlots(Player p, Connection connection) throws SQLException {
        return getPlayerInteger(p, "home_slots", connection);
    }

    // Return how many home slots a player has
    public static Long getPlayerRtpCoolDown(Player p, Connection connection) throws SQLException {
        return (long) getPlayerInteger(p, "rtp_cooldown", connection);
    }

    // Returns a list of a player's homes
    public static List<Home> getPlayerHomes(String playerName, Connection connection) throws SQLException {
        final List<Home> playerHomes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);")) {
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int locationID = resultSet.getInt("location_id");
                TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                playerHomes.add(new Home(teleportationPoint,
                        playerName,
                        getPlayerUUID(resultSet.getInt("player_id"), connection),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getBoolean("public"),
                        resultSet.getTimestamp("creation_time").getTime()));
            }

        }
        return playerHomes;
    }

    // Return all the public homes
    public static List<Home> getPublicHomes(Connection connection) throws SQLException {
        final List<Home> publicHomes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `public`;")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int playerID = resultSet.getInt("player_id");
                int locationID = resultSet.getInt("location_id");
                TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                publicHomes.add(new Home(teleportationPoint,
                        getPlayerUsername(playerID, connection),
                        getPlayerUUID(playerID, connection),
                        resultSet.getString("name"),
                        resultSet.getString("description"), true,
                        resultSet.getTimestamp("creation_time").getTime()));
            }

        }
        return publicHomes;
    }

    // Return an array of all the warps
    public static List<Warp> getWarps(Connection connection) throws SQLException {
        final List<Warp> warps = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " ORDER BY `name` ASC;")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet != null) {
                while (resultSet.next()) {
                    int locationID = resultSet.getInt("location_id");
                    TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                    warps.add(new Warp(teleportationPoint,
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getTimestamp("creation_time").getTime()));
                }
            } else {
                Bukkit.getLogger().severe("Result set of warps returned null!");
            }
        }
        return warps;
    }

    // Return a warp with a given name (warp names are unique)
    public static Warp getWarp(String name, Connection connection) throws SQLException {
        try (PreparedStatement statement = (connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;"))) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int locationID = resultSet.getInt("location_id");
                TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                return new Warp(teleportationPoint,
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("creation_time").getTime());
            }
        }
        return null;
    }

    // Obtain the teleportation location ID from a home
    public static Integer getHomeLocationID(int ownerID, String homeName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=? AND `name`=?;")) {
            statement.setInt(1, ownerID);
            statement.setString(2, homeName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet != null) {
                if (resultSet.next()) {
                    final int locationID = resultSet.getInt("location_id");
                    statement.close();
                    return locationID;
                }
            } else {
                Bukkit.getLogger().severe("Failed to obtain home teleportation location ID");
            }
        }
        return null;
    }

    // Delete a home's corresponding home teleport location
    public static void deleteHomeTeleportLocation(int ownerID, String homeName, Connection connection) throws SQLException {
        Integer locationID = getHomeLocationID(ownerID, homeName, connection);
        if (locationID != null) {
            deleteTeleportationPoint(locationID, connection);
        }
    }

    // Update a home's teleport location (deletion of the old one is done afterward to prevent cascading deletion from wiping the home
    public static void updateHomeTeleportLocation(int ownerID, String homeName, TeleportationPoint teleportationPoint, Connection connection) throws SQLException {
        Integer oldLocationID = getHomeLocationID(ownerID, homeName, connection);
        setHomeTeleportPoint(homeName, ownerID, teleportationPoint, connection);
        if (oldLocationID != null) {
            deleteTeleportationPoint(oldLocationID, connection);
        }
    }

    // Get the ID of the teleportation point of the warp
    public static Integer getWarpLocationID(String warpName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;")) {
            statement.setString(1, warpName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet != null) {
                if (resultSet.next()) {
                    final int locationID = resultSet.getInt("location_id");
                    statement.close();
                    return locationID;
                }
            }
        }
        return null;
    }

    // Delete a warp
    public static void deleteWarp(String warpName, Connection connection) throws SQLException {
        Integer warpLocationID = getWarpLocationID(warpName, connection);
        if (warpLocationID != null) {
            deleteTeleportationPoint(warpLocationID, connection);

            // Delete the warp with the given name
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;")) {
                statement.setString(1, warpName);
                statement.executeUpdate();
            }
        }
    }


    // Delete the home with the given name and player ID
    public static void deleteHome(String homeName, String ownerUsername, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);")) {
            statement.setString(1, homeName);
            statement.setString(2, ownerUsername);
            statement.executeUpdate();
        }
    }

    public static void addPlayer(Player p, Connection connection) throws SQLException {
        addPlayer(p.getUniqueId(), p.getName(), connection);
    }

    // Insert a player into the database
    public static void addPlayer(UUID playerUUID, String playerName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + HuskHomes.getSettings().getPlayerDataTable() + " (user_uuid,username,home_slots,rtp_cooldown,is_teleporting) VALUES(?,?,?,0,?);")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setInt(3, HuskHomes.getSettings().getFreeHomeSlots());
            statement.setBoolean(4, false);
            statement.executeUpdate();
        }
    }

    public static void updatePlayerUsername(UUID uuid, String newName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `username`=? WHERE `user_uuid`=?;")) {
            statement.setString(1, newName);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    private static void setPlayerTeleportingData(UUID uuid, boolean value, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `is_teleporting`=? WHERE `user_uuid`=?;")) {
            statement.setBoolean(1, value);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    private static void setPlayerIgnoringRequestsData(UUID uuid, boolean value, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `is_ignoring_requests`=? WHERE `user_uuid`=?;")) {
            statement.setBoolean(1, value);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    public static void setPlayerHomeSlots(UUID uuid, int newValue, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `home_slots`=? WHERE `user_uuid`=?;")) {
            statement.setInt(1, newValue);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    public static void setRtpCoolDown(UUID uuid, int newTime, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `rtp_cooldown`=? WHERE `user_uuid`=?;")) {
            statement.setInt(1, newTime);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }


    // Update the privacy of a home
    public static void setHomePrivacy(String homeName, String ownerName, boolean isPublic, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `public`=? WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);")) {
            statement.setBoolean(1, isPublic);
            statement.setString(2, homeName);
            statement.setString(3, ownerName);
            statement.executeUpdate();
        }
    }


    // Update the location of a home
    public static void setWarpTeleportPoint(String warpName, TeleportationPoint point, Connection connection) throws SQLException {
        // Set the warp location ID to a new teleport point
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getWarpsDataTable() + " SET `location_id`=? WHERE `name`=?;")) {
            statement.setInt(1, addTeleportationPoint(point, connection));
            statement.setString(2, warpName);
            statement.executeUpdate();
        }
    }

    // Update the description of a home
    public static void setHomeDescription(String homeName, String ownerName, String newDescription, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `description`=? WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);")) {
            statement.setString(1, newDescription);
            statement.setString(2, homeName);
            statement.setString(3, ownerName);
            statement.executeUpdate();
        }
    }

    // Update the description of a warp
    public static void setWarpDescription(String warpName, String newDescription, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getWarpsDataTable() + " SET `description`=? WHERE `name`=?;")) {
            statement.setString(1, newDescription);
            statement.setString(2, warpName);
            statement.executeUpdate();
        }
    }

    // Update the name of a home
    public static void setHomeName(String oldHomeName, String ownerName, String newHomeName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `name`=? WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);")) {
            statement.setString(1, newHomeName);
            statement.setString(2, oldHomeName);
            statement.setString(3, ownerName);
            statement.executeUpdate();
        }
    }

    // Update the name of a warp
    public static void setWarpName(String oldWarpName, String newWarpName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getWarpsDataTable() + " SET `name`=? WHERE `name`=?;")) {
            statement.setString(1, newWarpName);
            statement.setString(2, oldWarpName);
            statement.executeUpdate();
        }
    }

    // Update a player's destination teleport point
    public static void setTeleportationDestinationData(String username, TeleportationPoint point, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `dest_location_id`=? WHERE `username`=?;")) {
            // Set the destination location with the location_id of the last inserted teleport point
            statement.setInt(1, addTeleportationPoint(point, connection));
            statement.setString(2, username);
            statement.executeUpdate();
        }
    }

    // Update a warp's teleport location (deletion of the old one is done afterward to prevent cascading deletion from wiping the warp
    public static void updateWarpTeleportLocation(String warpName, TeleportationPoint teleportationPoint, Connection connection) throws SQLException {
        Integer oldLocationID = getWarpLocationID(warpName, connection);
        setWarpTeleportPoint(warpName, teleportationPoint, connection);
        if (oldLocationID != null) {
            deleteTeleportationPoint(oldLocationID, connection);
        }
    }

    public static void updateHomePrivacy(String ownerName, String homeName, boolean isPublic, Connection connection) throws SQLException {
        setHomePrivacy(homeName, ownerName, isPublic, connection);
    }

    public static void updateHomeName(String ownerName, String homeName, String newName, Connection connection) throws SQLException {
        setHomeName(homeName, ownerName, newName, connection);
    }

    public static void updateHomeDescription(String ownerName, String homeName, String newDescription, Connection connection) throws SQLException {
        setHomeDescription(homeName, ownerName, newDescription, connection);
    }

    public static void updateHomeLocation(String ownerName, String homeName, Location newLocation, Connection connection) throws SQLException {
        Integer playerID = getPlayerId(ownerName, connection);
        if (playerID != null) {
            updateHomeTeleportLocation(playerID, homeName, new TeleportationPoint(newLocation,
                    HuskHomes.getSettings().getServerID()), connection);
        }
    }

    public static void updateWarpName(String warpName, String newName, Connection connection) throws SQLException {
        setWarpName(warpName, newName, connection);
    }

    public static void updateWarpDescription(String warpName, String newDescription, Connection connection) throws SQLException {
        setWarpDescription(warpName, newDescription, connection);
    }

    public static void updateWarpLocation(String warpName, Location newLocation, Connection connection) throws SQLException {
        updateWarpTeleportLocation(warpName, new TeleportationPoint(newLocation,
                HuskHomes.getSettings().getServerID()), connection);
    }

    // Return a home with a given owner username and home name
    public static Home getHome(String ownerUsername, String homeName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?) AND `name`=?;")) {
            statement.setString(1, ownerUsername);
            statement.setString(2, homeName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet != null) {
                if (resultSet.next()) {
                    final int locationID = resultSet.getInt("location_id");
                    final TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                    final Home home = new Home(teleportationPoint,
                            ownerUsername,
                            getPlayerUUID(resultSet.getInt("player_id"), connection),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBoolean("public"),
                            resultSet.getTimestamp("creation_time").getTime());
                    statement.close();
                    return home;
                }
            }
        }
        return null;
    }

    public static boolean warpExists(String warpName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;")) {
            statement.setString(1, warpName);
            ResultSet resultSet = statement.executeQuery();
            final boolean warpExists = resultSet.next();
            statement.close();
            return warpExists;
        }
    }

    public static Boolean homeExists(Player owner, String homeName, Connection connection) throws SQLException {
        return homeExists(owner.getName(), homeName, connection);
    }

    public static Boolean homeExists(String ownerName, String homeName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?) AND `name`=?;")) {
            statement.setString(1, ownerName);
            statement.setString(2, homeName);
            ResultSet resultSet = statement.executeQuery();
            final boolean homeExists = resultSet.next();
            statement.close();
            return homeExists;
        }
    }

    public static Boolean playerExists(Player player, Connection connection) throws SQLException {
        return playerExists(player.getUniqueId(), connection);
    }

    public static Boolean playerExists(UUID playerUUID, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;")) {
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            final boolean exists = resultSet.next();
            statement.close();
            return exists;
        }
    }

    public static TeleportationPoint getTeleportationPoint(Integer locationID, Connection connection) throws SQLException, IllegalArgumentException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + HuskHomes.getSettings().getLocationsDataTable() + " WHERE `location_id`=?;")) {
            statement.setInt(1, locationID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final TeleportationPoint point = new TeleportationPoint(
                        resultSet.getString("world"),
                        resultSet.getDouble("x"),
                        resultSet.getDouble("y"),
                        resultSet.getDouble("z"),
                        resultSet.getFloat("yaw"),
                        resultSet.getFloat("pitch"),
                        resultSet.getString("server"));
                statement.close();
                return point;
            } else {
                statement.close();
                throw new IllegalArgumentException("Could not return a teleportationPoint from the locations data table at (ID#" + locationID + ")");
            }
        }
    }

    public static TeleportationPoint getPlayerDestination(Player p, Connection connection) throws SQLException {
        Integer locationID = getPlayerInteger(p, "dest_location_id", connection);
        if (locationID != null) {
            return getTeleportationPoint(locationID, connection);
        } else {
            return null;
        }
    }

    public static TeleportationPoint getPlayerLastPosition(Player p, Connection connection) throws SQLException {
        Integer locationID = getPlayerInteger(p, "last_location_id", connection);
        if (locationID != null) {
            return getTeleportationPoint(locationID, connection);
        } else {
            return null;
        }
    }

    public static TeleportationPoint getPlayerOfflinePosition(int playerID, Connection connection) throws SQLException {
        try {
            Integer locationID = getPlayerInteger(playerID, "offline_location_id", connection);
            if (locationID != null) {
                return getTeleportationPoint(locationID, connection);
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Insert a teleportation point, returns generated id
    public static Integer addTeleportationPoint(TeleportationPoint point, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + HuskHomes.getSettings().getLocationsDataTable() + " (world,server,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, point.getWorldName());
            statement.setString(2, point.getServer());
            statement.setDouble(3, point.getX());
            statement.setDouble(4, point.getY());
            statement.setDouble(5, point.getZ());
            statement.setFloat(6, point.getYaw());
            statement.setFloat(7, point.getPitch());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            final int pointID = resultSet.getInt(1);
            statement.close();
            return pointID;
        }
    }

    // Update a player's offline position teleport point
    public static void setTeleportationOfflinePositionData(UUID uuid, TeleportationPoint point, Connection connection) throws SQLException {
        // Set the destination location with the location_id of the newly inserted teleport point
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `offline_location_id`=? WHERE `user_uuid`=?;")) {
            statement.setInt(1, addTeleportationPoint(point, connection));
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    // Update a player's last position location on SQL
    public static void setPlayerOfflinePosition(UUID uuid, TeleportationPoint point, Connection connection) throws SQLException {
        deletePlayerOfflinePosition(uuid, connection);
        setTeleportationOfflinePositionData(uuid, point, connection);
    }

    // Update a player's last position teleport point
    public static void setTeleportationLastPositionData(UUID uuid, TeleportationPoint point, Connection connection) throws SQLException {
        // Set the destination location with the location_id of the last inserted teleport point
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `last_location_id`=? WHERE `user_uuid`=?;")) {
            statement.setInt(1, addTeleportationPoint(point, connection));
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    // Update a player's last position location on SQL
    public static void setPlayerLastPosition(Player p, TeleportationPoint point, Connection connection) throws SQLException {
        deletePlayerLastPosition(p, connection);
        setTeleportationLastPositionData(p.getUniqueId(), point, connection);
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(String playerName, TeleportationPoint point, Connection connection) throws SQLException {
        deletePlayerDestination(playerName, connection);
        setTeleportationDestinationData(playerName, point, connection);
    }

    // Update a player's destination location on SQL
    public static void setPlayerDestinationLocation(Player p, TeleportationPoint point, Connection connection) throws SQLException {
        setPlayerDestinationLocation(p.getName(), point, connection);
    }

    public static void setPlayerTeleporting(Player p, boolean teleporting, Connection connection) throws SQLException {
        if (teleporting) {
            HuskHomes.setTeleporting(p.getUniqueId());
        } else {
            HuskHomes.setNotTeleporting(p.getUniqueId());
        }
        setPlayerTeleportingData(p.getUniqueId(), teleporting, connection);
    }

    public static void setPlayerIgnoringRequests(Player p, boolean ignoringRequests, Connection connection) throws SQLException {
        setPlayerIgnoringRequestsData(p.getUniqueId(), ignoringRequests, connection);
    }

    public static void updateRtpCoolDown(Player p, Connection connection) throws SQLException {
        long currentTime = Instant.now().getEpochSecond();
        int newCoolDownTime = (int) currentTime + (60 * HuskHomes.getSettings().getRtpCoolDown());
        setRtpCoolDown(p.getUniqueId(), newCoolDownTime, connection);
    }

    public static void deletePlayerDestination(String playerName, Connection connection) throws SQLException {
        Integer destinationID = getPlayerInteger(getPlayerId(playerName, connection), "dest_location_id", connection);
        if (destinationID != null) {
            deleteTeleportationPoint(destinationID, connection);
            clearPlayerDestData(playerName, connection);
        }
    }

    // Delete a teleportation point from SQL
    public static void deleteTeleportationPoint(int locationID, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + HuskHomes.getSettings().getLocationsDataTable() + " WHERE `location_id`=?;")) {
            statement.setInt(1, locationID);
            statement.executeUpdate();
        }
    }

    public static void deletePlayerLastPosition(Player p, Connection connection) throws SQLException {
        Integer lastPositionID = getPlayerInteger(p, "last_location_id", connection);
        if (lastPositionID != null) {
            deleteTeleportationPoint(lastPositionID, connection);
            clearPlayerLastData(p.getUniqueId(), connection);
        }
    }

    public static void deletePlayerOfflinePosition(UUID uuid, Connection connection) throws SQLException {
        Integer offlineLocationId = getPlayerInteger(getPlayerId(uuid, connection), "offline_location_id", connection);
        if (offlineLocationId != null) {
            deleteTeleportationPoint(offlineLocationId, connection);
            clearPlayerOfflineData(uuid, connection);
        }
    }

    // Update the location of a home
    public static void setHomeTeleportPoint(String homeName, int ownerID, TeleportationPoint point, Connection connection) throws SQLException {
        // Set the home location ID to the new teleport point for the given home
        try (PreparedStatement statement = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `location_id`=? WHERE `name`=? AND `player_id`=?;")) {
            statement.setInt(1, addTeleportationPoint(point, connection));
            statement.setString(2, homeName);
            statement.setInt(3, ownerID);
            statement.executeUpdate();
        }
    }

    // Clear a player's destination
    public static void clearPlayerDestData(String playerName, Connection connection) throws SQLException {
        try (PreparedStatement statement = (connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `dest_location_id`=NULL WHERE `username`=?;"))) {
            statement.setString(1, playerName);
            statement.executeUpdate();
        }
    }

    // Clear a player's last position
    public static void clearPlayerLastData(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `last_location_id`=NULL WHERE `user_uuid`=?;")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
    }

    // Clear a player's last position
    public static void clearPlayerOfflineData(UUID uuid, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `offline_location_id`=NULL WHERE `user_uuid`=?;")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
    }

    // Insert the warp with the location_id of the last inserted teleport point
    public static void addWarp(Warp warp, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + HuskHomes.getSettings().getWarpsDataTable() + " (location_id,name,description,creation_time) VALUES(?,?,?,?);")) {
            statement.setInt(1, addTeleportationPoint(warp, connection));
            statement.setString(2, warp.getName());
            statement.setString(3, warp.getDescription());
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        }
    }

    public static void addHome(Home home, UUID playerUUID, Connection connection) throws SQLException {
        Integer playerID = getPlayerId(playerUUID, connection);
        if (playerID != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + HuskHomes.getSettings().getHomesDataTable() + " (player_id,location_id,name,description,public,creation_time) VALUES(?,?,?,?,?,?);")) {
                statement.setInt(1, playerID);
                statement.setInt(2, addTeleportationPoint(home, connection));
                statement.setString(3, home.getName());
                statement.setString(4, home.getDescription());
                statement.setBoolean(5, home.isPublic());
                statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        }
    }

    public static void addHome(Home home, Player p, Connection connection) throws SQLException {
        addHome(home, p.getUniqueId(), connection);
    }

    public static void deleteHome(String homeName, Player p, Connection connection) throws SQLException {
        Integer playerID = getPlayerId(p.getUniqueId(), connection);
        if (playerID != null) {
            deleteHomeTeleportLocation(playerID, homeName, connection);
            deleteHome(homeName, p.getName(), connection);
        } else {
            Bukkit.getLogger().warning("Player ID returned null when deleting a home");
        }
    }


    public static void createPlayer(Player p, Connection connection) throws SQLException {
        addPlayer(p, connection);
    }

    public static void createPlayer(UUID playerUUID, String playerUsername, Connection connection) throws SQLException {
        addPlayer(playerUUID, playerUsername, connection);
    }

    public static void checkPlayerNameChange(Player p, Connection connection) throws SQLException {
        final Integer playerID = getPlayerId(p.getUniqueId(), connection);
        if (playerID != null) {
            if (!getPlayerUsername(playerID, connection).equals(p.getName())) {
                updatePlayerUsername(p.getUniqueId(), p.getName(), connection);
            }
        }
    }
}