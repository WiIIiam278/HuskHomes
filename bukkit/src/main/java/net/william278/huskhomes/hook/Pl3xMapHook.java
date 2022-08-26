package net.william278.huskhomes.hook;

import net.pl3x.map.api.Key;
import net.pl3x.map.api.Pl3xMap;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Hook to display warps and public homes on <a href="https://github.com/BillyGalbreath/Pl3xMap">Pl3xMap v2</a> maps
 */
//todo Still a WIP, awaiting Pl3xMap v2 API!
public class Pl3xMapHook extends MapHook {

    public Pl3xMapHook(@NotNull HuskHomes implementor) {
        super(implementor, "Pl3xMap v2");
    }

    @Override
    protected CompletableFuture<Void> initializeMap() {
        return CompletableFuture.runAsync(() -> {
            try {
                Pl3xMap.api().getIconRegistry().register(new Key(PUBLIC_HOME_MARKER_IMAGE_NAME), ImageIO.read
                        (Objects.requireNonNull(plugin.getResource("markers/16x/" + PUBLIC_HOME_MARKER_IMAGE_NAME + ".png"))));
                Pl3xMap.api().getIconRegistry().register(new Key(PUBLIC_HOME_MARKER_IMAGE_NAME), ImageIO.read
                        (Objects.requireNonNull(plugin.getResource("markers/16x/" + WARP_MARKER_IMAGE_NAME + ".png"))));
            } catch (IOException e) {
                plugin.getLoggingAdapter().log(Level.SEVERE, "Failed to load marker image resources", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateHome(@NotNull Home home) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> removeHome(@NotNull Home home) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateWarp(@NotNull Warp warp) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> removeWarp(@NotNull Warp warp) {
        return CompletableFuture.completedFuture(null);
    }

}
