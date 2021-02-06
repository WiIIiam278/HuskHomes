package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.messageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Random;

public class RandomPoint extends TeleportationPoint {

    // List containing all unsafe blocks
    final static HashSet<Material> unsafeBlocks = new HashSet<>();

    // Maximum number of attempts to find a random location
    final static int maxRandomAttempts = 8;

    static {
        unsafeBlocks.add(Material.LAVA);
        unsafeBlocks.add(Material.FIRE);
        unsafeBlocks.add(Material.CACTUS);
        unsafeBlocks.add(Material.WATER);
        unsafeBlocks.add(Material.MAGMA_BLOCK);
        unsafeBlocks.add(Material.JUNGLE_LEAVES);
        unsafeBlocks.add(Material.SPRUCE_LEAVES);
    }

    private Location randomLocation(World world) {
        // Generate a random location
        Random random = new Random();

        int x;
        int y = 64;
        int z;
        int negativeX;
        int negativeZ;

        // The furthest distance at which a player can be teleported to
        int rtpRange = HuskHomes.settings.getRtpRange();

        // Calculate random X and Z coords
        x = random.nextInt(rtpRange);
        z = random.nextInt(rtpRange);

        // Determine if the plugin should teleport to positive or negative Z
        negativeX = random.nextInt(2);
        negativeZ = random.nextInt(2);
        if (negativeX == 1) {
            x = x * -1;
        }
        if (negativeZ == 1)  {
            z = z * -1;
        }

        // Put together random location, get the highest block plus one to determine Y
        Location randomLocation = new Location(world, x, y, z);
        y = world.getHighestBlockYAt(randomLocation) + 1;
        randomLocation.setY(y);

        return randomLocation;
    }

    // Checks to see if the location is safe to teleport to
    private boolean isLocationSafe(Location location){
        int x = location.getBlockX();
        int y = location.getBlockY()-1;
        int z = location.getBlockZ();

        World world = location.getWorld();

        // Failsafe check in case the world is null
        if (world != null) {

            // Get instances of the blocks around where the player would spawn
            Block block = world.getBlockAt(x, y, z);
            Block below = world.getBlockAt(x, y - 1, z);
            Block above = world.getBlockAt(x, y + 1, z);

            // Check to see if those blocks are safe to teleport to
            if (!unsafeBlocks.contains(block.getType()) && block.getType().isSolid()) {
                if (!unsafeBlocks.contains(above.getType())) {
                    return !unsafeBlocks.contains(below.getType());
                }
            }
        }
        return false;
    }

    private Location getRandomLocation(World world) {
        Location randomLocation = randomLocation(world);

        int attempts = 0;

        // Keep looking for a random location that is safe
        while (!isLocationSafe(randomLocation)){

            // Failsafe timeout in case the plugin can't find a safe location within a number of tries.
            if (attempts < maxRandomAttempts) {
                randomLocation = randomLocation(world);
            } else {
                return null;
            }
            attempts = attempts + 1;
        }
        return randomLocation;
    }

    public RandomPoint(Player player) {
        super(player.getLocation(), HuskHomes.settings.getServerID());
        Location randomLocation = getRandomLocation(player.getWorld());
        if (randomLocation != null) {
            setLocation(randomLocation, HuskHomes.settings.getServerID());
        } else {
            messageManager.sendMessage(player, "error_rtp_randomization_timeout");
        }
    }

}
