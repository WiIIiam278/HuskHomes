package net.william278.huskhomes.position;

import net.william278.huskhomes.cache.Cache;
import net.william278.huskhomes.data.Database;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Cross-platform manager for validating homes and warps and updating them as neccessary
 */
public class SavedPositionManager {

    /**
     * The {@link Database} implementation used to add homes to the database
     */
    private final Database database;

    /**
     * The {@link Cache} the holds cached home and warp data, updated when setting home
     */
    private final Cache cache;

    public SavedPositionManager(@NotNull Database database, @NotNull Cache cache) {
        this.database = database;
        this.cache = cache;
    }

    /**
     * Tries to create a {@link Home} for a {@link User} at the specified {@link Position} from supplied
     * {@link PositionMeta} data and add it to the database
     *
     * @param homeMeta  metadata about the home
     * @param homeOwner the owner of the home
     * @param position  where the home should be set
     * @return a future supplying the {@link SetResult}; the result of setting the home
     */
    public CompletableFuture<SetResult> setHome(@NotNull PositionMeta homeMeta, @NotNull User homeOwner,
                                                @NotNull Position position) {
        return CompletableFuture.supplyAsync(() ->
                validateMeta(homeMeta).map(resultType -> new SetResult(resultType, null)).orElseGet(() ->
                        database.getHome(homeOwner, homeMeta.name).thenApply(optionalHome -> {
                            if (optionalHome.isEmpty()) {
                                final Home home = new Home(position, homeMeta, homeOwner);
                                cache.homes.putIfAbsent(home.owner.uuid, new ArrayList<>());
                                cache.homes.get(home.owner.uuid).add(home.meta.name);
                                cache.positionLists.clear();
                                return database.setHome(home).thenApply(ignored ->
                                        new SetResult(SetResult.ResultType.SUCCESS, home)).join();
                            }
                            return new SetResult(SetResult.ResultType.FAILED_DUPLICATE, null);
                        }).join()));
    }

    /**
     * Tries to create a server {@link Warp} at the specified {@link Position} from supplied
     * {@link PositionMeta} data and add it to the database
     *
     * @param warpMeta metadata about the warp
     * @param position where the warp should be set
     * @return a future supplying the {@link SetResult}; the result of setting the warp
     */
    public CompletableFuture<SetResult> setWarp(@NotNull PositionMeta warpMeta, @NotNull Position position) {
        return CompletableFuture.supplyAsync(() ->
                validateMeta(warpMeta).map(resultType -> new SetResult(resultType, null)).orElseGet(() ->
                        database.getWarp(warpMeta.name).thenApply(optionalWarp -> {
                            if (optionalWarp.isEmpty()) {
                                final Warp warp = new Warp(position, warpMeta);
                                cache.warps.add(warp.meta.name);
                                cache.positionLists.clear();
                                return database.setWarp(warp).thenApply(ignored ->
                                        new SetResult(SetResult.ResultType.SUCCESS, warp)).join();
                            }
                            return new SetResult(SetResult.ResultType.FAILED_DUPLICATE, null);
                        }).join()));
    }

    /**
     * Validates {@link PositionMeta} to ensure it can be added to the database
     *
     * @param positionMeta the {@link PositionMeta} to validate
     * @return An optional. If the {@link PositionMeta} is invalid, this will contain a {@link SetResult.ResultType}
     * identifying which constraint is violated by the metadata
     */
    private Optional<SetResult.ResultType> validateMeta(PositionMeta positionMeta) {
        //todo options for allowing unicode names / descriptions
        if (positionMeta.name.length() > 16) {
            return Optional.of(SetResult.ResultType.FAILED_NAME_LENGTH);
        }
        if (!RegexUtil.NAME_PATTERN.matcher(positionMeta.name).matches()) {
            return Optional.of(SetResult.ResultType.FAILED_NAME_CHARACTERS);
        }
        if (positionMeta.description.length() > 255) {
            return Optional.of(SetResult.ResultType.FAILED_DESCRIPTION_LENGTH);
        }
        if (!RegexUtil.DESCRIPTION_PATTERN.matcher(positionMeta.name).matches()) {
            return Optional.of(SetResult.ResultType.FAILED_DESCRIPTION_CHARACTERS);
        }
        return Optional.empty();
    }

    /**
     * Holds information about the result of setting a warp or home
     *
     * @param resultType
     * @param setPosition
     */
    public record SetResult(@NotNull ResultType resultType, @Nullable SavedPosition setPosition) {

        /**
         * Indicates the result of setting a warp or home
         */
        public enum ResultType {
            /**
             * The home or warp was successfully set
             */
            SUCCESS(true),

            /**
             * The position was not set; one by this name has already been set (for the user in the case of homes)
             */
            FAILED_DUPLICATE(false),

            /**
             * The position was not set; the name exceeds the maximum length
             */
            FAILED_NAME_LENGTH(false),

            /**
             * The position was not set; the description exceeds the maximum length
             */
            FAILED_DESCRIPTION_LENGTH(false),

            /**
             * The position was not set; the name uses illegal characters
             */
            FAILED_NAME_CHARACTERS(false),

            /**
             * The position was not set; the description uses illegal characters
             */
            FAILED_DESCRIPTION_CHARACTERS(false);

            public final boolean successful;

            ResultType(final boolean successful) {
                this.successful = successful;
            }
        }
    }

}
