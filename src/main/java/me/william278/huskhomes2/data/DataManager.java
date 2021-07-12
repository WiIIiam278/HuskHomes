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
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;");
        ps.setString(1, uuid.toString());
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final int playerID = rs.getInt("player_id");
                ps.close();
                return playerID;
            }
        }
        ps.close();
        return null;
    }

    // Return a player's ID  from their username
    private static Integer getPlayerId(String playerUsername, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?;");
        ps.setString(1, playerUsername);
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final int returnable = rs.getInt("player_id");
                ps.close();
                return returnable;
            }
        }
        ps.close();
        return null;
    }

    // Return an integer from the player table from a player ID
    private static Integer getPlayerInteger(Integer playerID, String column, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `player_id`=?;");
        ps.setInt(1, playerID);
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final int returnable = rs.getInt(column);
                ps.close();
                return returnable;
            }
        }
        ps.close();
        return null;
    }

    // Return an integer from the player table from a player object
    private static Integer getPlayerInteger(Player p, String column, Connection connection) throws SQLException {
        return getPlayerInteger(getPlayerId(p.getUniqueId(), connection), column, connection);
    }

    // Return an integer from the player table from a player ID
    private static String getPlayerString(Integer playerID, String column, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `player_id`=?;");
        ps.setInt(1, playerID);
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final String player = rs.getString(column);
                ps.close();
                return player;
            }
        } else {
            Bukkit.getLogger().severe("Result set for a player returned null; perhaps player ID was null");
        }
        ps.close();
        return null;
    }

    // Return if the player is teleporting
    public static Boolean getPlayerTeleporting(Player p, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;");
        ps.setString(1, p.getUniqueId().toString());
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final boolean isTeleporting = rs.getBoolean("is_teleporting");
                ps.close();
                return isTeleporting;
            }
        } else {
            Bukkit.getLogger().severe("Failed to retrieve if player was teleporting");
        }
        ps.close();
        return null;
    }

    // Return if the player is ignoring teleport requests
    public static Boolean getPlayerIgnoringRequests(Player p, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;");
        ps.setString(1, p.getUniqueId().toString());
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final boolean isIgnoringRequests = rs.getBoolean("is_ignoring_requests");
                ps.close();
                return isIgnoringRequests;
            }
        } else {
            Bukkit.getLogger().severe("Failed to retrieve if player was ignoring teleport requests");
        }
        ps.close();
        return null;
    }

    // Return a player's UUID
    public static String getPlayerUUID(int playerID, Connection connection) throws SQLException {
        return getPlayerString(playerID, "user_uuid", connection);
    }

    // Return a player's username
    public static String getPlayerUsername(int playerID, Connection connection) throws SQLException {
        return getPlayerString(playerID, "username", connection);
    }

    // Return how many homes the player has set
    public static int getPlayerHomeCount(Player p, Connection connection) throws SQLException {
        List<Home> playerHomes = getPlayerHomes(p.getName(), connection);
        if (playerHomes != null) {
            return playerHomes.size();
        } else {
            return 0;
        }
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
    public static Long getPlayerRtpCooldown(Player p, Connection connection) throws SQLException {
        return (long) getPlayerInteger(p, "rtp_cooldown", connection);
    }

    // Return a player's homes.
    public static List<Home> getPlayerHomes(String playerName, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);");
        ps.setString(1, playerName);
        rs = ps.executeQuery();
        if (rs != null) {
            final List<Home> playerHomes = new ArrayList<>();
            while (rs.next()) {
                int locationID = rs.getInt("location_id");
                TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                playerHomes.add(new Home(teleportationPoint,
                        playerName, getPlayerUUID(rs.getInt("player_id"), connection),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("public")));
            }
            ps.close();
            return playerHomes;
        }
        ps.close();
        return null;
    }

    // Return all the public homes
    public static List<Home> getPublicHomes(Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `public`;");
        rs = ps.executeQuery();
        final List<Home> publicHomes = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                int playerID = rs.getInt("player_id");
                int locationID = rs.getInt("location_id");
                TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                publicHomes.add(new Home(teleportationPoint, getPlayerUsername(playerID, connection),
                        getPlayerUUID(playerID, connection), rs.getString("name"),
                        rs.getString("description"), true));
            }
            ps.close();
        }
        ps.close();
        return publicHomes;
    }

    // Return an array of all the warps
    public static List<Warp> getWarps(Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + ";");
        rs = ps.executeQuery();
        final List<Warp> warps = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                int locationID = rs.getInt("location_id");
                TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                warps.add(new Warp(teleportationPoint,
                        rs.getString("name"),
                        rs.getString("description")));
            }
        } else {
            Bukkit.getLogger().severe("Result set of warps returned null!");
        }
        ps.close();
        return warps;
    }

    // Return a warp with a given name (warp names are unique)
    public static Warp getWarp(String name, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;");
        ps.setString(1, name);
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                int locationID = rs.getInt("location_id");
                TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                return new Warp(teleportationPoint,
                        rs.getString("name"),
                        rs.getString("description"));
            }
        }
        ps.close();
        return null;
    }

    // Obtain the teleportation location ID from a home
    public static Integer getHomeLocationID(int ownerID, String homeName, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=? AND `name`=?;");
        ps.setInt(1, ownerID);
        ps.setString(2, homeName);
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final int locationID = rs.getInt("location_id");
                ps.close();
                return locationID;
            }
        } else {
            Bukkit.getLogger().severe("Failed to obtain home teleportation location ID");
        }
        ps.close();
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
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;");
        ps.setString(1, warpName);
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final int locationID = rs.getInt("location_id");
                ps.close();
                return locationID;
            }
        } else {
            Bukkit.getLogger().severe("Failed to obtain warp teleportation location ID");
        }
        ps.close();
        return null;
    }

    // Delete a warp
    public static void deleteWarp(String warpName, Connection connection) throws SQLException {
        Integer warpLocationID = getWarpLocationID(warpName, connection);
        if (warpLocationID != null) {
            deleteTeleportationPoint(warpLocationID, connection);

            PreparedStatement ps;
            // Delete the warp with the given name
            ps = connection.prepareStatement("DELETE FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;");
            ps.setString(1, warpName);
            ps.executeUpdate();
            ps.close();
        }
    }


    public static void deleteHome(String homeName, String ownerUsername, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Delete the home with the given name and player ID
        ps = connection.prepareStatement("DELETE FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);");
        ps.setString(1, homeName);
        ps.setString(2, ownerUsername);
        ps.executeUpdate();
        ps.close();
    }

    public static void addPlayer(Player p, Connection connection) throws SQLException {
        PreparedStatement ps;

        ps = connection.prepareStatement("INSERT INTO " + HuskHomes.getSettings().getPlayerDataTable() + " (user_uuid,username,home_slots,rtp_cooldown,is_teleporting) VALUES(?,?,?,?,?);");
        ps.setString(1, p.getUniqueId().toString());
        ps.setString(2, p.getName());
        ps.setInt(3, Home.getFreeHomes(p));
        ps.setInt(4, 0);
        ps.setBoolean(5, false);

        ps.executeUpdate();
        ps.close();
    }

    // Insert a player into the database
    public static void addPlayer(UUID playerUUID, String playerName, Connection connection) throws SQLException {
        PreparedStatement ps;

        ps = connection.prepareStatement("INSERT INTO " + HuskHomes.getSettings().getPlayerDataTable() + " (user_uuid,username,home_slots,rtp_cooldown,is_teleporting) VALUES(?,?,?,?,?);");
        ps.setString(1, playerUUID.toString());
        ps.setString(2, playerName);
        ps.setInt(3, HuskHomes.getSettings().getFreeHomeSlots());
        ps.setInt(4, 0);
        ps.setBoolean(5, false);

        ps.executeUpdate();
        ps.close();
    }

    public static void updatePlayerUsername(UUID uuid, String newName, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `username`=? WHERE `user_uuid`=?;");
        ps.setString(1, newName);
        ps.setString(2, uuid.toString());
        ps.executeUpdate();
        ps.close();
    }

    private static void setPlayerTeleportingData(UUID uuid, boolean value, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `is_teleporting`=? WHERE `user_uuid`=?;");
        ps.setBoolean(1, value);
        ps.setString(2, uuid.toString());
        ps.executeUpdate();
        ps.close();
    }

    private static void setPlayerIgnoringRequestsData(UUID uuid, boolean value, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `is_ignoring_requests`=? WHERE `user_uuid`=?;");
        ps.setBoolean(1, value);
        ps.setString(2, uuid.toString());
        ps.executeUpdate();
        ps.close();
    }

    public static void setPlayerHomeSlots(UUID uuid, int newValue, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `home_slots`=? WHERE `user_uuid`=?;");
        ps.setInt(1, newValue);
        ps.setString(2, uuid.toString());
        ps.executeUpdate();
        ps.close();
    }

    public static void setRtpCoolDown(UUID uuid, int newTime, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `rtp_cooldown`=? WHERE `user_uuid`=?;");
        ps.setInt(1, newTime);
        ps.setString(2, uuid.toString());
        ps.executeUpdate();
        ps.close();
    }


    // Update the location of a home
    public static void setHomePrivacy(String homeName, String ownerName, boolean isPublic, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Set the home location ID to the new teleport point for the given home
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `public`=? WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);");
        ps.setBoolean(1, isPublic);
        ps.setString(2, homeName);
        ps.setString(3, ownerName);
        ps.executeUpdate();
        ps.close();
    }


    // Update the location of a home
    public static void setWarpTeleportPoint(String warpName, TeleportationPoint point, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Add the teleportation point
        Integer locationID = addTeleportationPoint(point, connection);

        if (locationID != null) {
            // Set the warp location ID to the new teleport point
            ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getWarpsDataTable() + " SET `location_id`=? WHERE `name`=?;");
            ps.setInt(1, locationID);
            ps.setString(2, warpName);
            ps.executeUpdate();
            ps.close();
        }
    }

    // Update the description of a home
    public static void setHomeDescription(String homeName, String ownerName, String newDescription, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Update the home description
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `description`=? WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);");
        ps.setString(1, newDescription);
        ps.setString(2, homeName);
        ps.setString(3, ownerName);
        ps.executeUpdate();
        ps.close();
    }

    // Update the description of a warp
    public static void setWarpDescription(String warpName, String newDescription, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Update the warp description
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getWarpsDataTable() + " SET `description`=? WHERE `name`=?;");
        ps.setString(1, newDescription);
        ps.setString(2, warpName);
        ps.executeUpdate();
        ps.close();
    }

    // Update the name of a home
    public static void setHomeName(String oldHomeName, String ownerName, String newHomeName, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Update the home name
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `name`=? WHERE `name`=? AND `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?);");
        ps.setString(1, newHomeName);
        ps.setString(2, oldHomeName);
        ps.setString(3, ownerName);

        ps.executeUpdate();

    }

    // Update the name of a warp
    public static void setWarpName(String oldWarpName, String newWarpName, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Update the warp name
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getWarpsDataTable() + " SET `name`=? WHERE `name`=?;");
        ps.setString(1, newWarpName);
        ps.setString(2, oldWarpName);

        ps.executeUpdate();
        ps.close();
    }

    // Update a player's destination teleport point
    public static void setTeleportationDestinationData(String username, TeleportationPoint point, Connection connection) throws SQLException {
        PreparedStatement ps;

        // Add the teleportation point
        Integer locationID = addTeleportationPoint(point, connection);

        if (locationID != null) {
            // Set the destination location with the location_id of the last inserted teleport point
            ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `dest_location_id`=? WHERE `username`=?;");
            ps.setInt(1, locationID);
            ps.setString(2, username);
            ps.executeUpdate();
            ps.close();
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
        PreparedStatement ps;
        ResultSet rs;


        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?) AND `name`=?;");
        ps.setString(1, ownerUsername);
        ps.setString(2, homeName);
        rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                final int locationID = rs.getInt("location_id");
                final TeleportationPoint teleportationPoint = getTeleportationPoint(locationID, connection);
                final Home home = new Home(teleportationPoint,
                        ownerUsername,
                        getPlayerUUID(rs.getInt("player_id"), connection),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("public"));
                ps.close();
                return home;
            }
        } else {
            Bukkit.getLogger().severe("Home returned null; perhaps player ID was null?");
        }
        ps.close();
        return null;
    }

    public static boolean warpExists(String warpName, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;
        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getWarpsDataTable() + " WHERE `name`=?;");
        ps.setString(1, warpName);
        rs = ps.executeQuery();
        if (rs != null) {
            final boolean warpExists = rs.next();
            ps.close();
            return warpExists;
        } else {
            Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a warp exists from the table.");
            ps.close();
            return false;
        }
    }

    public static Boolean homeExists(Player owner, String homeName, Connection connection) throws SQLException {
        return homeExists(owner.getName(), homeName, connection);
    }

    public static Boolean homeExists(String ownerName, String homeName, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;


        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getHomesDataTable() + " WHERE `player_id`=(SELECT `player_id` FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `username`=?) AND `name`=?;");
        ps.setString(1, ownerName);
        ps.setString(2, homeName);
        rs = ps.executeQuery();
        if (rs != null) {
            final boolean homeExists = rs.next();
            ps.close();
            return homeExists;
        } else {
            Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a home exists from the table.");
            ps.close();
            return false;
        }

    }

    public static Boolean playerExists(Player player, Connection connection) throws SQLException {
        return playerExists(player.getUniqueId(), connection);
    }

    public static Boolean playerExists(UUID playerUUID, Connection connection) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;
        String playerUUIDString = playerUUID.toString();

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getPlayerDataTable() + " WHERE `user_uuid`=?;");
        ps.setString(1, playerUUIDString);
        rs = ps.executeQuery();
        if (rs != null) {
            final boolean exists = rs.next();
            ps.close();
            return exists;
        } else {
            Bukkit.getLogger().severe("An SQL exception occurred in retrieving if a player exists from the table.");
            ps.close();
            return false;
        }
    }

    public static TeleportationPoint getTeleportationPoint(Integer locationID, Connection connection) throws SQLException, IllegalArgumentException {
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement("SELECT * FROM " + HuskHomes.getSettings().getLocationsDataTable() + " WHERE `location_id`=?;");
        ps.setInt(1, locationID);
        rs = ps.executeQuery();
        if (rs.next()) {
            final TeleportationPoint point = new TeleportationPoint(rs.getString("world"),
                    rs.getDouble("x"), rs.getDouble("y"),
                    rs.getDouble("z"), rs.getFloat("yaw"),
                    rs.getFloat("pitch"), rs.getString("server"));
            ps.close();
            return point;
        } else {
            ps.close();
            throw new IllegalArgumentException("Could not return a teleportationPoint from the locations data table at (ID#" + locationID + ")");
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

    public static TeleportationPoint getPlayerOfflinePosition(Player p, Connection connection) throws SQLException {
        Integer locationID = getPlayerInteger(p, "offline_location_id", connection);
        if (locationID != null) {
            return getTeleportationPoint(locationID, connection);
        } else {
            return null;
        }
    }

    // Insert a teleportation point, returns generated id
    public static Integer addTeleportationPoint(TeleportationPoint point, Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO " + HuskHomes.getSettings().getLocationsDataTable() + " (world,server,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, point.getWorldName());
        ps.setString(2, point.getServer());
        ps.setDouble(3, point.getX());
        ps.setDouble(4, point.getY());
        ps.setDouble(5, point.getZ());
        ps.setFloat(6, point.getYaw());
        ps.setFloat(7, point.getPitch());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            final int pointID = rs.getInt(1);
            ps.close();
            return pointID;
        }
        return null;
    }

    // Update a player's offline position teleport point
    public static void setTeleportationOfflinePositionData(UUID uuid, TeleportationPoint point, Connection connection) throws SQLException {
        PreparedStatement ps;
        // Add the teleportation point
        Integer locationID = addTeleportationPoint(point, connection);

        if (locationID != null) {
            // Set the destination location with the location_id of the last inserted teleport point
            ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `offline_location_id`=? WHERE `user_uuid`=?;");
            ps.setInt(1, locationID);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            ps.close();
        }
    }

    // Update a player's last position location on SQL
    public static void setPlayerOfflinePosition(Player p, TeleportationPoint point, Connection connection) throws SQLException {
        deletePlayerOfflinePosition(p, connection);
        setTeleportationOfflinePositionData(p.getUniqueId(), point, connection);
    }

    // Update a player's last position teleport point
    public static void setTeleportationLastPositionData(UUID uuid, TeleportationPoint point, Connection connection) throws SQLException {
        PreparedStatement ps;
        // Add the teleportation point
        Integer locationID = addTeleportationPoint(point, connection);

        if (locationID != null) {
            // Set the destination location with the location_id of the last inserted teleport point
            ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `last_location_id`=? WHERE `user_uuid`=?;");
            ps.setInt(1, locationID);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            ps.close();
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
        setPlayerTeleportingData(p.getUniqueId(), teleporting, connection);
        if (teleporting) {
            HuskHomes.setTeleporting(p.getUniqueId());
        } else {
            HuskHomes.setNotTeleporting(p.getUniqueId());
        }
    }

    public static void setPlayerIgnoringRequests(Player p, boolean ignoringRequests, Connection connection) throws SQLException {
        setPlayerIgnoringRequestsData(p.getUniqueId(), ignoringRequests, connection);
    }

    public static void updateRtpCooldown(Player p, Connection connection) throws SQLException {
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
        PreparedStatement ps;
        ps = connection.prepareStatement("DELETE FROM " + HuskHomes.getSettings().getLocationsDataTable() + " WHERE `location_id`=?;");
        ps.setInt(1, locationID);
        ps.executeUpdate();
        ps.close();
    }

    public static void deletePlayerLastPosition(Player p, Connection connection) throws SQLException {
        Integer lastPositionID = getPlayerInteger(p, "last_location_id", connection);
        if (lastPositionID != null) {
            deleteTeleportationPoint(lastPositionID, connection);
            clearPlayerLastData(p.getUniqueId(), connection);
        }
    }

    public static void deletePlayerOfflinePosition(Player p, Connection connection) throws SQLException {
        Integer offlineLocationId = getPlayerInteger(p, "offline_location_id", connection);
        if (offlineLocationId != null) {
            deleteTeleportationPoint(offlineLocationId, connection);
            clearPlayerOfflineData(p.getUniqueId(), connection);
        }
    }

    // Update the location of a home
    public static void setHomeTeleportPoint(String homeName, int ownerID, TeleportationPoint point, Connection connection) throws SQLException {
        PreparedStatement ps;
        // Add the teleportation point
        Integer locationID = addTeleportationPoint(point, connection);

        if (locationID != null) {
            // Set the home location ID to the new teleport point for the given home
            ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getHomesDataTable() + " SET `location_id`=? WHERE `name`=? AND `player_id`=?;");
            ps.setInt(1, locationID);
            ps.setString(2, homeName);
            ps.setInt(3, ownerID);
            ps.executeUpdate();
            ps.close();
        }
    }

    // Clear a player's destination
    public static void clearPlayerDestData(String playerName, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `dest_location_id`=NULL WHERE `username`=?;");
        ps.setString(1, playerName);
        ps.executeUpdate();
        ps.close();
    }

    // Clear a player's last position
    public static void clearPlayerLastData(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `last_location_id`=NULL WHERE `user_uuid`=?;");
        ps.setString(1, uuid.toString());
        ps.executeUpdate();
        ps.close();
    }

    // Clear a player's last position
    public static void clearPlayerOfflineData(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + HuskHomes.getSettings().getPlayerDataTable() + " SET `offline_location_id`=NULL WHERE `user_uuid`=?;");
        ps.setString(1, uuid.toString());
        ps.executeUpdate();
        ps.close();
    }

    public static void addWarp(Warp warp, Connection connection) throws SQLException {
        PreparedStatement ps;
        // Add the teleportation point
        Integer locationID = addTeleportationPoint(warp, connection);

        if (locationID != null) {
            // Insert the warp with the location_id of the last inserted teleport point
            ps = connection.prepareStatement("INSERT INTO " + HuskHomes.getSettings().getWarpsDataTable() + " (location_id,name,description) VALUES(?,?,?);");
            ps.setInt(1, locationID);
            ps.setString(2, warp.getName());
            ps.setString(3, warp.getDescription());
            ps.executeUpdate();
            ps.close();
        }
    }

    public static void addHome(Home home, UUID playerUUID, Connection connection) throws SQLException {
        Integer playerID = getPlayerId(playerUUID, connection);
        if (playerID != null) {
            PreparedStatement ps;
            Integer locationID = addTeleportationPoint(home, connection);

            if (locationID != null) {
                ps = connection.prepareStatement("INSERT INTO " + HuskHomes.getSettings().getHomesDataTable() + " (player_id,location_id,name,description,public) VALUES(?,?,?,?,?);");
                ps.setInt(1, playerID);
                ps.setInt(2, locationID);
                ps.setString(3, home.getName());
                ps.setString(4, home.getDescription());
                ps.setBoolean(5, home.isPublic());
                ps.executeUpdate();
                ps.close();
            }
        } else {
            Bukkit.getLogger().warning("Failed to add a home for a player!");
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