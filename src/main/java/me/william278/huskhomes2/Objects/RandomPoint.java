package me.william278.huskhomes2.Objects;

import me.william278.huskhomes2.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Random;

public class RandomPoint extends TeleportationPoint {

    //List of all the unsafe blocks
    final static HashSet<Material> bad_blocks = new HashSet<>();

    static{
        bad_blocks.add(Material.LAVA);
        bad_blocks.add(Material.FIRE);
        bad_blocks.add(Material.CACTUS);
        bad_blocks.add(Material.WATER);
        bad_blocks.add(Material.MAGMA_BLOCK);
        bad_blocks.add(Material.JUNGLE_LEAVES);
        bad_blocks.add(Material.SPRUCE_LEAVES);
    }

    private Location randomLocation(World world) {
        //Generate Random Location
        Random random = new Random();

        int x = 0;
        int z = 0;
        int y = 0;
        int negativex = 0;
        int negativez = 0;
        x = random.nextInt(Main.settings.getRtpRange());
        z = random.nextInt(Main.settings.getRtpRange());
        negativex = random.nextInt(2);
        negativez = random.nextInt(2);
        if (negativex == 1) {
            x = x * -1;
        }
        if (negativez == 1)
            z = z * -1;
        y = 150;

        Location randomLocation = new Location(world, x, y, z);
        y = randomLocation.getWorld().getHighestBlockYAt(randomLocation);
        randomLocation.setY(y);

        return randomLocation;
    }

    private boolean isLocationSafe(Location location){

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        //Get instances of the blocks around where the player would spawn
        Block block = location.getWorld().getBlockAt(x, y, z);
        Block below = location.getWorld().getBlockAt(x, y - 1, z);
        Block above = location.getWorld().getBlockAt(x, y + 1, z);

        //Check to see if the surroundings are safe or not
        return !(bad_blocks.contains(below.getType())) || (block.getType().isSolid()) || (above.getType().isSolid());
    }

    private Location getRandomLocation(World world) {
        Location randomLocation = randomLocation(world);

        while (!isLocationSafe(randomLocation)){

            //Keep looking for a safe location
            randomLocation = randomLocation(world);
        }
        return randomLocation;
    }

    public RandomPoint(Player player) {
        super(player.getLocation(), Main.settings.getServerID());
        setLocation(getRandomLocation(player.getWorld()), Main.settings.getServerID());
    }

}
