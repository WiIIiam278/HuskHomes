package net.william278.huskhomes.random;

import com.github.plot.Plot;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NormalDistributionTests {

    @Test
    public void testNormalDistributionRandomizer() {
        Assertions.assertEquals(1000, generateLocations(1000).size());
    }

    // Plots location distribution
    public static void generateRtpLocationDistributionImage() {
        final List<Location> locations = generateLocations(2000);
        final double[] xValues = new double[locations.size()];
        final double[] zValues = new double[locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            xValues[i] = locations.get(i).x;
            zValues[i] = locations.get(i).z;
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
            plot.save("images/rtp-location-distribution", "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Location> generateLocations(int amount) {
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            locations.add(NormalDistributionEngine.generateLocation(
                    new Location(0, 0, 0, 0, 0, new World("TestWorld", UUID.randomUUID())),
                    0.75f, 2f, 500f, 3000f));
        }
        return locations;
    }

}
