package net.william278.huskhomes.position;

import net.william278.huskhomes.Cache;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.hook.MapHook;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Cross-platform manager for validating homes and warps and updating them as necessary
 */
public class SavedPositionManager {

    private final HuskHomes plugin;

    public SavedPositionManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
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
        return plugin.getDatabase().getHome(homeOwner, homeMeta.name).thenApplyAsync(optionalHome ->
                validateMeta(homeMeta).map(resultType -> new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                    if (optionalHome.isEmpty()) {
                        // Handle setting a new home
                        final Home home = new Home(position, homeMeta, homeOwner);
                        return plugin.getEventDispatcher().dispatchHomeSaveEvent(home).thenApplyAsync(event -> {
                            if (event.isCancelled()) {
                                return new SaveResult(SaveResult.ResultType.FAILED_EVENT_CANCELLED, Optional.empty());
                            }
                            final Cache cache = plugin.getCache();
                            cache.homes.putIfAbsent(home.owner.uuid, new ArrayList<>());
                            cache.homes.get(home.owner.uuid).add(home.meta.name);
                            cache.privateHomeLists.remove(home.owner.username);
                            return plugin.getDatabase().saveHome(home).thenApplyAsync(value ->
                                    new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(home))).join();
                        }).join();
                    } else if (plugin.getSettings().overwriteExistingHomesWarps) {
                        // Handle overwriting existing homes if the option is enabled
                        final Home existingHome = optionalHome.get();
                        updateHomePosition(existingHome, position).thenApplyAsync(result -> {
                            if (result) {
                                if (existingHome.isPublic) {
                                    plugin.getMapHook().ifPresent(hook -> hook.updateHome(existingHome));
                                }

                                return new SaveResult(SaveResult.ResultType.SUCCESS_OVERWRITTEN, Optional.of(existingHome));
                            }
                            return new SaveResult(SaveResult.ResultType.FAILED_EVENT_CANCELLED, Optional.empty());
                        }).join();
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
        return plugin.getDatabase().getHome(homeOwner, homeName).thenApplyAsync(optionalHome -> {
            if (optionalHome.isPresent()) {
                return plugin.getEventDispatcher().dispatchHomeDeleteEvent(optionalHome.get()).thenApplyAsync(event -> {
                    if (event.isCancelled()) {
                        return false;
                    }
                    final Home home = event.getHome();
                    return plugin.getDatabase().deleteHome(home.uuid).thenApplyAsync(ignored -> {
                        final Cache cache = plugin.getCache();
                        cache.homes.computeIfPresent(home.owner.uuid, (ownerUUID, homeNames) -> {
                            homeNames.remove(home.meta.name);
                            return homeNames;
                        });
                        cache.privateHomeLists.remove(home.owner.username);
                        cache.publicHomeLists.clear();
                        plugin.getMapHook().ifPresent(hook -> hook.removeHome(home));
                        return true;
                    }).join();
                }).join();
            }
            return false;
        });
    }

    public CompletableFuture<Integer> deleteAllHomes(@NotNull User user) {
        return CompletableFuture.supplyAsync(() -> plugin.getEventDispatcher().dispatchDeleteAllHomesEvent(user).join().isCancelled())
                .thenApply(cancelled -> plugin.getDatabase().deleteAllHomes(user)
                        .thenApply(result -> {
                            final Cache cache = plugin.getCache();
                            cache.homes.remove(user.uuid);
                            cache.privateHomeLists.remove(user.username);
                            cache.publicHomeLists.clear();
                            plugin.getMapHook().ifPresent(hook -> hook.clearHomes(user));
                            return result;
                        }).join());
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
            if (!home.meta.name.equals(newHomeMeta.name) && plugin.getDatabase().getHome(home.owner, newHomeMeta.name).join().isPresent()) {
                return new SaveResult(SaveResult.ResultType.FAILED_DUPLICATE, Optional.empty());
            }
            final String existingHomeName = home.meta.name;
            return validateMeta(newHomeMeta).map(resultType ->
                    new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                home.meta = newHomeMeta;
                return plugin.getEventDispatcher().dispatchHomeSaveEvent(home).thenApplyAsync(event -> {
                    if (event.isCancelled()) {
                        return new SaveResult(SaveResult.ResultType.FAILED_EVENT_CANCELLED, Optional.empty());
                    }
                    final Cache cache = plugin.getCache();
                    if (!existingHomeName.equals(newHomeMeta.name)) {
                        cache.homes.get(home.owner.uuid).remove(existingHomeName);
                        cache.homes.get(home.owner.uuid).add(newHomeMeta.name);
                    }
                    cache.privateHomeLists.remove(home.owner.username);
                    cache.publicHomeLists.clear();
                    if (home.isPublic) {
                        plugin.getMapHook().ifPresent(hook -> hook.updateHome(home));
                    }
                    return plugin.getDatabase().saveHome(home).thenApplyAsync(value ->
                            new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(home))).join();
                }).join();
            });
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
        home.update(newPosition);
        return plugin.getEventDispatcher().dispatchHomeSaveEvent(home).thenApplyAsync(event -> {
            if (event.isCancelled()) {
                return false;
            }
            if (home.isPublic) {
                plugin.getMapHook().ifPresent(hook -> hook.updateHome(home));
            }
            return plugin.getDatabase().saveHome(home).thenApplyAsync(ignored -> true).join();
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
        return plugin.getEventDispatcher().dispatchHomeSaveEvent(home).thenApplyAsync(event -> {
            if (event.isCancelled()) {
                return false;
            }

            // Save the home to database and propagate updates to cache and map hook
            return plugin.getDatabase().saveHome(home).thenApplyAsync(ignored -> {
                final Cache cache = plugin.getCache();
                if (home.isPublic) {
                    cache.publicHomes.putIfAbsent(home.owner.username, new ArrayList<>());
                    cache.publicHomes.get(home.owner.username).add(home.meta.name);
                    plugin.getMapHook().ifPresent(hook -> hook.updateHome(home));
                } else {
                    if (cache.publicHomes.containsKey(home.owner.username)) {
                        cache.publicHomes.get(home.owner.username).remove(home.meta.name);
                    }
                    plugin.getMapHook().ifPresent(hook -> hook.removeHome(home));
                }
                cache.privateHomeLists.remove(home.owner.username);
                cache.publicHomeLists.clear();
                return true;
            }).join();
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
        return plugin.getDatabase().getWarp(warpMeta.name).thenApplyAsync(optionalWarp ->
                validateMeta(warpMeta).map(resultType -> new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                    if (optionalWarp.isEmpty()) {
                        // Handle setting new warps
                        final Warp warp = new Warp(position, warpMeta);
                        return plugin.getEventDispatcher().dispatchWarpSaveEvent(warp).thenApplyAsync(event -> {
                            if (event.isCancelled()) {
                                return new SaveResult(SaveResult.ResultType.FAILED_EVENT_CANCELLED, Optional.empty());
                            }
                            final Cache cache = plugin.getCache();
                            cache.warps.add(warp.meta.name);
                            cache.warpLists.clear();
                            plugin.getMapHook().ifPresent(hook -> hook.updateWarp(warp));
                            return plugin.getDatabase().saveWarp(warp).thenApplyAsync(ignored ->
                                    new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(warp))).join();
                        }).join();
                    } else if (plugin.getSettings().overwriteExistingHomesWarps) {
                        // Handle overwriting existing warps if the option is enabled
                        final Warp existingWarp = optionalWarp.get();
                        updateWarpPosition(existingWarp, position).thenApplyAsync(result -> {
                            if (result) {
                                plugin.getMapHook().ifPresent(hook -> hook.updateWarp(existingWarp));
                                return new SaveResult(SaveResult.ResultType.SUCCESS_OVERWRITTEN, Optional.of(existingWarp));
                            }
                            return new SaveResult(SaveResult.ResultType.FAILED_EVENT_CANCELLED, Optional.empty());
                        }).join();
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
        return plugin.getDatabase().getWarp(warpName).thenApplyAsync(optionalWarp -> {
            if (optionalWarp.isPresent()) {
                return plugin.getEventDispatcher().dispatchWarpDeleteEvent(optionalWarp.get()).thenApplyAsync(event -> {
                    if (event.isCancelled()) {
                        return false;
                    }

                    // Delete the warp from database and propagate updates to cache and map hook
                    final Warp warp = event.getWarp();
                    return plugin.getDatabase().deleteWarp(warp.uuid).thenApplyAsync(ignored -> {
                        final Cache cache = plugin.getCache();
                        cache.warps.remove(warp.meta.name);
                        cache.warpLists.clear();
                        plugin.getMapHook().ifPresent(hook -> hook.removeWarp(warp));
                        return true;
                    }).join();
                }).join();
            }
            return false;
        });
    }

    public CompletableFuture<Integer> deleteAllWarps() {
        return CompletableFuture.supplyAsync(() -> plugin.getEventDispatcher().dispatchDeleteAllWarpsEvent().join().isCancelled())
                .thenApply(cancelled -> plugin.getDatabase().deleteAllWarps()
                        .thenApply(result -> {
                            plugin.getCache().warps.clear();
                            plugin.getCache().warpLists.clear();
                            plugin.getMapHook().ifPresent(MapHook::clearWarps);
                            return result;
                        }).join());
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
            if (!warp.meta.name.equals(newWarpMeta.name) && plugin.getDatabase().getWarp(newWarpMeta.name).join().isPresent()) {
                return new SaveResult(SaveResult.ResultType.FAILED_DUPLICATE, Optional.empty());
            }
            final String existingWarpName = warp.meta.name;
            return validateMeta(newWarpMeta).map(resultType ->
                    new SaveResult(resultType, Optional.empty())).orElseGet(() -> {
                warp.meta = newWarpMeta;
                return plugin.getEventDispatcher().dispatchWarpSaveEvent(warp).thenApplyAsync(event -> {
                    if (event.isCancelled()) {
                        return new SaveResult(SaveResult.ResultType.FAILED_EVENT_CANCELLED, Optional.empty());
                    }

                    final Cache cache = plugin.getCache();
                    if (!existingWarpName.equals(newWarpMeta.name)) {
                        cache.warps.remove(existingWarpName);
                        cache.warps.add(newWarpMeta.name);
                    }
                    cache.warpLists.clear();
                    plugin.getMapHook().ifPresent(hook -> hook.updateWarp(warp));
                    return plugin.getDatabase().saveWarp(warp).thenApplyAsync(value ->
                            new SaveResult(SaveResult.ResultType.SUCCESS, Optional.of(warp))).join();
                }).join();
            });
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
        warp.update(newPosition);
        return plugin.getEventDispatcher().dispatchWarpSaveEvent(warp).thenApplyAsync(event -> {
            if (event.isCancelled()) {
                return false;
            }
            plugin.getMapHook().ifPresent(hook -> hook.updateWarp(warp));
            return plugin.getDatabase().saveWarp(warp).thenApplyAsync(ignored -> true).join();
        });
    }

    /**
     * Validates {@link PositionMeta} to ensure it can be added to the database
     *
     * @param positionMeta the {@link PositionMeta} to validate
     * @return An optional. If the {@link PositionMeta} is invalid, this will contain a {@link SaveResult.ResultType}
     * identifying which constraint is violated by the metadata
     */
    private Optional<SaveResult.ResultType> validateMeta(@NotNull PositionMeta positionMeta) {
        if (positionMeta.name.length() > 16) {
            return Optional.of(SaveResult.ResultType.FAILED_NAME_LENGTH);
        }
        if (!plugin.getSettings().allowUnicodeNames) {
            if (!RegexUtil.NAME_PATTERN.matcher(positionMeta.name).matches()) {
                return Optional.of(SaveResult.ResultType.FAILED_NAME_CHARACTERS);
            }
        }
        if (positionMeta.name.contains(".")) {
            return Optional.of(SaveResult.ResultType.FAILED_NAME_CHARACTERS);
        }
        if (positionMeta.description.length() > 255) {
            return Optional.of(SaveResult.ResultType.FAILED_DESCRIPTION_LENGTH);
        }
        if (!plugin.getSettings().allowUnicodeDescriptions) {
            if (!RegexUtil.DESCRIPTION_PATTERN.matcher(positionMeta.description).matches()) {
                return Optional.of(SaveResult.ResultType.FAILED_DESCRIPTION_CHARACTERS);
            }
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
             * A home or warp was attempted to be set, but one already exists with the same name (for the user in the
             * case of homes), so the existing position was updated instead.
             * <p>
             *
             * @implNote This will only fire if the {@link Settings#overwriteExistingHomesWarps} config value is
             * set to {@code true}
             */
            SUCCESS_OVERWRITTEN(true),

            /**
             * The position was not set; one by this name has already been set (for the user in the case of homes)
             * <p>
             *
             * @implNote This will only fire if the {@link Settings#overwriteExistingHomesWarps} config value is
             * set to {@code false}
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
             * The position was not set or updated; the save event was cancelled by a plugin
             */
            FAILED_EVENT_CANCELLED(false);

            public final boolean successful;

            ResultType(final boolean successful) {
                this.successful = successful;
            }
        }
    }

}
