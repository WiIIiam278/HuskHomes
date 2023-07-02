/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

    private static final float MEAN = 0.75f;
    private static final float STANDARD_DEVIATION = 2f;
    private static final float SPAWN_RADIUS = 500f;
    private static final float MAX_RADIUS = 3000f;

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
     * Plots location distribution to an image for testing.
     */
    public static class DistributionModeller {

        /**
         * Plot a demo of the normal distribution algorithm.
         *
         * @param args path to save image to
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
                throw new RuntimeException(e);
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
