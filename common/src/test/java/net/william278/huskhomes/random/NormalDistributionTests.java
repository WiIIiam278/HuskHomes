package net.william278.huskhomes.random;

import com.github.plot.Plot;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DisplayName("Normal Distribution Tests")
public class NormalDistributionTests {

    private final static float MEAN = 0.75f;
    private final static float STANDARD_DEVIATION = 2f;
    private final static float SPAWN_RADIUS = 500f;
    private final static float MAX_RADIUS = 3000f;

    @Test
    @DisplayName("Test Point Generation")
    public void testPointGenerator() {
        Assertions.assertEquals(1000, generateLocations(1000).size());
    }

    // Test points are within range
    @Test
    @DisplayName("Test Points Within Range")
    public void testPointRange() {
        final List<Location> locations = generateLocations(1000);
        for (Location location : locations) {
            Assertions.assertTrue(location.getX() >= -MAX_RADIUS && location.getX() <= MAX_RADIUS);
            Assertions.assertTrue(location.getZ() >= -MAX_RADIUS && location.getZ() <= MAX_RADIUS);
        }
    }

    /**
     * Plots location distribution to an image for testing
     */
    public static class DistributionModeller {

        /**
         * Plot a demo of the normal distribution algorithm
         *
         * @param args file path to save image to
         */
        @SuppressWarnings("unused")
        public static void main(String[] args) {
            final List<Location> locations = generateLocations(2000);
            final double[] xValues = new double[locations.size()];
            final double[] zValues = new double[locations.size()];
            for (int i = 0; i < locations.size(); i++) {
                xValues[i] = locations.get(i).getX();
                zValues[i] = locations.get(i).getZ();
            }
            final Plot plot = Plot.plot(Plot.plotOpts()
                            .title("RTP Normal Distribution demo")
                            .legend(Plot.LegendFormat.BOTTOM))
                    .xAxis("x", Plot.axisOpts().range(-3250, 3250))
                    .yAxis("z", Plot.axisOpts().range(-3250, 3250))
                    .series("Data", Plot.data()
                                    .xy(xValues, zValues),
                            Plot.seriesOpts()
                                    .marker(Plot.Marker.CIRCLE)
                                    .markerColor(new Color(0, 60, 255, 180))
                                    .color(new Color(0, 0, 0, 0)));

            try {
                final File output = args.length == 0 ? new File("target", "normal_distribution")
                        : new File(args[0], "normal_distribution");

                plot.save(output.getAbsolutePath(), "png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static List<Location> generateLocations(int amount) {
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            locations.add(NormalDistributionEngine.generateLocation(
                    Location.at(0, 0, 0, 0, 0, World.from("TestWorld", UUID.randomUUID())),
                    MEAN, STANDARD_DEVIATION, SPAWN_RADIUS, MAX_RADIUS));
        }
        return locations;
    }

}
