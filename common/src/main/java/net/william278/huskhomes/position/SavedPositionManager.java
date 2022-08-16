package net.william278.huskhomes.position;

import net.william278.huskhomes.cache.Cache;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

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
     * @return a future supplying the {@link SaveResult}; the result of setting the home
     */
    public CompletableFuture<SaveResult> setHome(@NotNull PositionMeta homeMeta, @NotNull User homeOwner,
                                                 @NotNull Position position) {
        return database.getHome(homeOwner, homeMeta.name).thenApply(optionalHome ->
                validateMeta(homeMeta).map(resultType -> new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                    if (optionalHome.isEmpty()) {
                        final Home home = new Home(position, homeMeta, homeOwner);
                        cache.homes.putIfAbsent(home.owner.uuid, new ArrayList<>());
                        cache.homes.get(home.owner.uuid).add(home.meta.name);
                        cache.positionLists.clear();
                        return database.setHome(home).thenApply(value ->
                                new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(home))).join();
                    }
                    return new SaveResult(SaveResult.ResultType.FAILED_DUPLICATE, Optional.empty());
                }));
    }

    /**
     * Tries to delete a {@link User}'s home by given name
     *
     * @param homeOwner the owner of the home
     * @param homeName  the name of the home
     * @return a future supplying a boolean; {@code true} if the home was deleted, otherwise {@code false}
     * if the home name was invalid.
     */
    public CompletableFuture<Boolean> deleteHome(@NotNull User homeOwner, @NotNull String homeName) {
        return database.getHome(homeOwner, homeName).thenApply(optionalHome -> {
            if (optionalHome.isPresent()) {
                final Home home = optionalHome.get();
                return database.deleteHome(home.uuid).thenApply(ignored -> {
                    cache.homes.computeIfPresent(home.owner.uuid, (ownerUUID, homeNames) -> {
                        homeNames.remove(home.meta.name);
                        return homeNames;
                    });
                    cache.positionLists.clear();
                    return true;
                }).join();
            }
            return false;
        });
    }

    /**
     * Tries to update an existing home with new metadata
     *
     * @param homeOwner        the owner of the home
     * @param existingHomeName the current name of the home to update
     * @param newHomeMeta      the new metadata for the home
     * @return a future supplying the {@link SaveResult}; the result of updating the home
     */
    public CompletableFuture<SaveResult> updateHomeMeta(@NotNull User homeOwner, @NotNull String existingHomeName,
                                                        @NotNull PositionMeta newHomeMeta) {
        return database.getHome(homeOwner, existingHomeName).thenApply(optionalHome -> {
            if (optionalHome.isPresent()) {
                return updateHomeMeta(optionalHome.get(), newHomeMeta).join();
            }
            return new SaveResult(SaveResult.ResultType.FAILED_DOES_NOT_EXIST, Optional.empty());
        });
    }

    /**
     * Tries to update an existing home with new metadata
     *
     * @param home        the home to update
     * @param newHomeMeta the new metadata for the home
     * @return a future supplying the {@link SaveResult}; the result of updating the home
     */
    public CompletableFuture<SaveResult> updateHomeMeta(@NotNull Home home, @NotNull PositionMeta newHomeMeta) {
        return CompletableFuture.supplyAsync(() -> {
            if (!home.meta.name.equals(newHomeMeta.name) && database.getHome(home.owner, newHomeMeta.name).join().isPresent()) {
                return new SaveResult(SaveResult.ResultType.FAILED_DUPLICATE, Optional.empty());
            }
            final String existingHomeName = home.meta.name;
            return validateMeta(newHomeMeta).map(resultType ->
                    new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                home.meta = newHomeMeta;
                if (!existingHomeName.equals(newHomeMeta.name)) {
                    cache.homes.get(home.owner.uuid).remove(existingHomeName);
                    cache.homes.get(home.owner.uuid).add(newHomeMeta.name);
                }
                cache.positionLists.clear();
                return database.setHome(home).thenApply(value ->
                        new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(home))).join();
            });
        });
    }


    /**
     * Tries to update the position of a {@link User}'s home
     *
     * @param homeOwner   the owner of the home
     * @param homeName    the name of the home
     * @param newPosition the new position for the home
     * @return a future supplying the {@link SaveResult}; the result of updating the home
     */
    public CompletableFuture<Boolean> updateHomePosition(@NotNull User homeOwner, @NotNull String homeName,
                                                         @NotNull Position newPosition) {
        return database.getHome(homeOwner, homeName).thenApply(optionalHome -> {
            if (optionalHome.isPresent()) {
                return updateHomePosition(optionalHome.get(), newPosition).join();
            }
            return false;
        });
    }

    /**
     * Tries to update the position of a {@link User}'s home
     *
     * @param home        the home to update
     * @param newPosition the new position for the home
     * @return a future supplying the {@link SaveResult}; the result of updating the home
     */
    public CompletableFuture<Boolean> updateHomePosition(@NotNull Home home, @NotNull Position newPosition) {
        home.x = newPosition.x;
        home.y = newPosition.y;
        home.z = newPosition.z;
        home.pitch = newPosition.pitch;
        home.yaw = newPosition.yaw;
        home.world = newPosition.world;
        home.server = newPosition.server;
        return database.setHome(home).thenApply(ignored -> true);
    }

    /**
     * Tries to change the privacy of a {@link User}'s home by given name
     *
     * @param homeOwner    the owner of the home
     * @param homeName     the name of the home
     * @param isHomePublic whether the home should be public or not
     * @return a future supplying a boolean; {@code true} if the privacy was changed, otherwise {@code false}
     * if the home name was invalid.
     */
    public CompletableFuture<Boolean> updateHomePrivacy(@NotNull User homeOwner, @NotNull String homeName,
                                                        final boolean isHomePublic) {
        return database.getHome(homeOwner, homeName).thenApply(optionalHome -> {
            if (optionalHome.isPresent()) {
                return updateHomePrivacy(optionalHome.get(), isHomePublic).join();
            }
            return false;
        });
    }

    /**
     * Tries to change the privacy of a {@link User}'s home by given name
     *
     * @param home         the home to update
     * @param isHomePublic whether the home should be public or not
     * @return a future supplying a boolean; {@code true} if the privacy was changed, otherwise {@code false}
     */
    public CompletableFuture<Boolean> updateHomePrivacy(@NotNull Home home, final boolean isHomePublic) {
        home.isPublic = isHomePublic;
        return database.setHome(home).thenApply(ignored -> {
            if (isHomePublic) {
                cache.publicHomes.putIfAbsent(home.owner.username, new ArrayList<>());
                cache.publicHomes.get(home.owner.username).add(home.meta.name);
            } else if (cache.publicHomes.containsKey(home.owner.username)) {
                cache.publicHomes.get(home.owner.username).remove(home.meta.name);
            }
            cache.positionLists.clear();
            return true;
        });
    }

    /**
     * Tries to create a server {@link Warp} at the specified {@link Position} from supplied
     * {@link PositionMeta} data and add it to the database
     *
     * @param warpMeta metadata about the warp
     * @param position where the warp should be set
     * @return a future supplying the {@link SaveResult}; the result of setting the warp
     */
    public CompletableFuture<SaveResult> setWarp(@NotNull PositionMeta warpMeta, @NotNull Position position) {
        return database.getWarp(warpMeta.name).thenApply(optionalWarp ->
                validateMeta(warpMeta).map(resultType -> new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                    if (optionalWarp.isEmpty()) {
                        final Warp warp = new Warp(position, warpMeta);
                        cache.warps.add(warp.meta.name);
                        cache.positionLists.clear();
                        return database.setWarp(warp).thenApply(ignored ->
                                new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(warp))).join();
                    }
                    return new SaveResult(SaveResult.ResultType.FAILED_DUPLICATE, Optional.empty());
                }));
    }

    /**
     * Tries to delete a warp by given name
     *
     * @param warpName the name of the warp
     * @return a future supplying a boolean; {@code true} if the warp was deleted, otherwise {@code false}
     * if the warp name was invalid.
     */
    public CompletableFuture<Boolean> deleteWarp(@NotNull String warpName) {
        return database.getWarp(warpName).thenApply(optionalWarp -> {
            if (optionalWarp.isPresent()) {
                final Warp warp = optionalWarp.get();
                return database.deleteWarp(warp.uuid).thenApply(ignored -> {
                    cache.warps.remove(warp.meta.name);
                    cache.positionLists.clear();
                    return true;
                }).join();
            }
            return false;
        });
    }

    /**
     * Tries to update an existing home with new metadata
     *
     * @param existingWarpName the current name of the home to update
     * @param newWarpMeta      the new metadata for the home
     * @return a future supplying the {@link SaveResult}; the result of updating the home
     */
    public CompletableFuture<SaveResult> updateWarpMeta(@NotNull String existingWarpName,
                                                        @NotNull PositionMeta newWarpMeta) {
        return database.getWarp(existingWarpName).thenApply(optionalWarp -> {
            if (optionalWarp.isPresent()) {
                return updateWarpMeta(optionalWarp.get(), newWarpMeta).join();
            }
            return new SaveResult(SaveResult.ResultType.FAILED_DOES_NOT_EXIST, Optional.empty());
        });
    }

    /**
     * Tries to update an existing warp with new metadata
     *
     * @param warp        the warp to update
     * @param newWarpMeta the new metadata for the home
     * @return a future supplying the {@link SaveResult}; the result of updating the warp
     */
    public CompletableFuture<SaveResult> updateWarpMeta(@NotNull Warp warp, @NotNull PositionMeta newWarpMeta) {
        return CompletableFuture.supplyAsync(() -> {
            if (!warp.meta.name.equals(newWarpMeta.name) && database.getWarp(newWarpMeta.name).join().isPresent()) {
                return new SaveResult(SaveResult.ResultType.FAILED_DUPLICATE, Optional.empty());
            }
            final String existingWarpName = warp.meta.name;
            return validateMeta(newWarpMeta).map(resultType ->
                    new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                warp.meta = newWarpMeta;
                if (!existingWarpName.equals(newWarpMeta.name)) {
                    cache.warps.remove(existingWarpName);
                    cache.warps.add(newWarpMeta.name);
                }
                cache.positionLists.clear();
                return database.setWarp(warp).thenApply(value ->
                        new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(warp))).join();
            });
        });
    }


    /**
     * Tries to update the position of a warp
     *
     * @param warpName    the name of the home
     * @param newPosition the new position for the home
     * @return a future supplying the {@link SaveResult}; the result of updating the home
     */
    public CompletableFuture<Boolean> updateWarpPosition(@NotNull String warpName,
                                                         @NotNull Position newPosition) {
        return database.getWarp(warpName).thenApply(optionalWarp -> {
            if (optionalWarp.isPresent()) {
                return updateWarpPosition(optionalWarp.get(), newPosition).join();
            }
            return false;
        });
    }

    /**
     * Tries to update the position of a warp
     *
     * @param warp        the warp to update
     * @param newPosition the new position for the warp
     * @return a future supplying the {@link SaveResult}; the result of updating the warp
     */
    public CompletableFuture<Boolean> updateWarpPosition(@NotNull Warp warp, @NotNull Position newPosition) {
        warp.x = newPosition.x;
        warp.y = newPosition.y;
        warp.z = newPosition.z;
        warp.pitch = newPosition.pitch;
        warp.yaw = newPosition.yaw;
        warp.world = newPosition.world;
        warp.server = newPosition.server;
        return database.setWarp(warp).thenApply(ignored -> true);
    }

    /**
     * Validates {@link PositionMeta} to ensure it can be added to the database
     *
     * @param positionMeta the {@link PositionMeta} to validate
     * @return An optional. If the {@link PositionMeta} is invalid, this will contain a {@link SaveResult.ResultType}
     * identifying which constraint is violated by the metadata
     */
    private Optional<SaveResult.ResultType> validateMeta(@NotNull PositionMeta positionMeta) {
        //todo options for allowing unicode names / descriptions
        if (positionMeta.name.length() > 16) {
            return Optional.of(SaveResult.ResultType.FAILED_NAME_LENGTH);
        }
        if (!RegexUtil.NAME_PATTERN.matcher(positionMeta.name).matches()) {
            return Optional.of(SaveResult.ResultType.FAILED_NAME_CHARACTERS);
        }
        if (positionMeta.description.length() > 255) {
            return Optional.of(SaveResult.ResultType.FAILED_DESCRIPTION_LENGTH);
        }
        if (!RegexUtil.DESCRIPTION_PATTERN.matcher(positionMeta.name).matches()) {
            return Optional.of(SaveResult.ResultType.FAILED_DESCRIPTION_CHARACTERS);
        }
        return Optional.empty();
    }

    /**
     * A record of information about the result of setting or updating a {@link SavedPosition} in the form of a warp or home
     *
     * @param resultType    The {@link ResultType} of the result returned, indicating success
     * @param savedPosition The {@link Position} of the saved position, if the result is {@link ResultType#SUCCESS}, otherwise {@link Optional#empty()}
     */
    public record SaveResult(@NotNull ResultType resultType, @NotNull Optional<SavedPosition> savedPosition) {

        /**
         * Indicates the result of setting a warp or home
         */
        public enum ResultType {
            /**
             * The home or warp was successfully set
             */
            SUCCESS(true),

            /**
             * The home or warp was not set or updated because it does not exist
             */
            FAILED_DOES_NOT_EXIST(false),

            /**
             * The position was not set; one by this name has already been set (for the user in the case of homes)
             */
            FAILED_DUPLICATE(false),

            /**
             * The position was not set or updated; the name exceeds the maximum length
             */
            FAILED_NAME_LENGTH(false),

            /**
             * The position was not set or updated; the description exceeds the maximum length
             */
            FAILED_DESCRIPTION_LENGTH(false),

            /**
             * The position was not set or updated; the name uses illegal characters
             */
            FAILED_NAME_CHARACTERS(false),

            /**
             * The position was not set or updated; the description uses illegal characters
             */
            FAILED_DESCRIPTION_CHARACTERS(false),

            /**
             * The position was not set or updated; an exception occurred
             */
            FAILED_ERROR(false);

            public final boolean successful;

            ResultType(final boolean successful) {
                this.successful = successful;
            }
        }
    }

}
