package net.william278.huskhomes.cache;

import net.william278.huskhomes.data.Database;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A cache used to hold persistently used data
 */
public class Cache {

    /**
     * Cached home names - maps a {@link UUID} to a list of their homes
     */
    public final HashMap<UUID, List<String>> homes;

    /**
     * Cached home names - maps a username to a list of their public homes
     */
    public final HashMap<String, List<String>> publicHomes;

    /**
     * Cached warp names
     */
    public final List<String> warps;

    /**
     * Create a new cache
     */
    public Cache() {
        this.homes = new HashMap<>();
        this.publicHomes = new HashMap<>();
        this.warps = new ArrayList<>();
    }

    /**
     * Initialize the cache, request basic data to load into memory
     *
     * @param database the database to load data from
     */
    public void initialize(@NotNull Database database) {
        CompletableFuture.runAsync(() -> {
            database.getPublicHomes().thenAccept(publicHomeList -> publicHomeList.forEach(home ->
                    this.publicHomes.computeIfAbsent(home.owner.username, key -> new ArrayList<>()).add(home.meta.name)));
            database.getWarps().thenAccept(warpsList -> warpsList.forEach(warp ->
                    this.warps.add(warp.meta.name)));
        });
    }

}
