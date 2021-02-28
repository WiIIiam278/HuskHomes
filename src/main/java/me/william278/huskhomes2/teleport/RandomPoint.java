package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RandomPoint extends TeleportationPoint {

    // List containing all unsafe blocks
    private final static Set<Material> unsafeBlocks = EnumSet.noneOf(Material.class);

    // Maximum number of attempts to find a random location
    private final static int maxRandomAttempts = 8;

    static {
        unsafeBlocks.add(Material.LAVA);
        unsafeBlocks.add(Material.FIRE);
        unsafeBlocks.add(Material.CACTUS);
        unsafeBlocks.add(Material.WATER);
        unsafeBlocks.add(Material.MAGMA_BLOCK);
        unsafeBlocks.add(Material.JUNGLE_LEAVES);
        unsafeBlocks.add(Material.SPRUCE_LEAVES);
        unsafeBlocks.add(Material.OAK_LEAVES);
        unsafeBlocks.add(Material.BIRCH_LEAVES);
        unsafeBlocks.add(Material.ACACIA_LEAVES);
        unsafeBlocks.add(Material.DARK_OAK_LEAVES);
        unsafeBlocks.add(Material.OBSIDIAN);
    }

    private Location randomLocation(World world) {
        // Generate a random location
        Random random = ThreadLocalRandom.current();

        int x;
        int y = 64;
        int z;
        double blockCenterX = 0.5;
        double blockCenterZ = 0.5;

        // The furthest distance at which a player can be teleported to
        int rtpRange = HuskHomes.settings.getRtpRange();

        // Calculate random X and Z coords
        x = random.nextInt(rtpRange);
        z = random.nextInt(rtpRange);

        // Determine if the plugin should teleport to positive or negative Z
        if (random.nextBoolean()) {
            x *= -1;
            blockCenterX *= -1;
        }
        if (random.nextBoolean())  {
            z *= -1;
            blockCenterZ *= -1;
        }

        // Put together random location, get the highest block plus one to determine Y
        Location randomLocation = new Location(world, (x + blockCenterX), y, (z + blockCenterZ));
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
            MessageManager.sendMessage(player, "error_rtp_randomization_timeout");
        }
    }

}
