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

package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.command.ListCommand;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.TransactionResolver;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HomesManager {

    private final HuskHomes plugin;
    private final ConcurrentLinkedQueue<Home> publicHomes;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Home>> userHomes;

    protected HomesManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.publicHomes = new ConcurrentLinkedQueue<>(plugin.getDatabase().getPublicHomes());
        this.userHomes = new ConcurrentHashMap<>();
        plugin.runAsync(() -> plugin.getOnlineUsers()
                .forEach(this::cacheUserHomes));
    }

    /**
     * Cached user homes - maps a username to a list of their homes.
     *
     * @return a map of usernames to a list of their home names.
     */
    @NotNull
    public Map<String, List<String>> getUserHomes() {
        return userHomes.entrySet().stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().stream().map(Home::getName).toList()),
                        HashMap::putAll);
    }

    /**
     * Get a list of all cached set home identifiers.
     *
     * @return a list of all cached set home identifiers
     */
    @NotNull
    public List<String> getUserHomeIdentifiers() {
        return userHomes.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(Home::getIdentifier))
                .toList();
    }

    /**
     * Cached public homes - maps a username to a list of their public homes.
     */
    @NotNull
    public Map<String, List<String>> getPublicHomes() {
        return publicHomes.stream().collect(
                HashMap::new,
                (m, e) -> m.put(e.getOwner().getName(), List.of(e.getName())),
                HashMap::putAll
        );
    }

    /**
     * Get a list of all cached public home identifiers.
     *
     * @return a list of all cached public home identifiers
     */
    @NotNull
    public List<String> getPublicHomeIdentifierNames() {
        return publicHomes.stream().map(Home::getIdentifier).toList();
    }

    /**
     * Get a list of all cached public homes.
     *
     * <p>This will return the full home identifier if there are multiple homes with the same name.
     *
     * @return a list of all cached public home identifiers
     */
    @NotNull
    public List<String> getPublicHomeNames() {
        return publicHomes.stream()
                .map(home -> {
                    if (publicHomes.stream().filter(h -> h.getName().equals(home.getName())).count() > 1) {
                        return home.getIdentifier();
                    }
                    return home.getName();
                })
                .toList();
    }

    /**
     * Cache user homes for a given user.
     *
     * @param user the user to cache homes for
     */
    public void cacheUserHomes(@NotNull User user) {
        userHomes.put(user.getName(), new ConcurrentLinkedQueue<>(plugin.getDatabase().getHomes(user)));
    }

    /**
     * Cache a home for a given user.
     *
     * @param home      the home to cache
     * @param propagate whether to propagate the cache update to other servers (if cross-server is enabled)
     */
    public void cacheHome(@NotNull Home home, boolean propagate) {
        userHomes.computeIfPresent(home.getOwner().getName(), (k, v) -> {
            v.remove(home);
            v.add(home);
            return v;
        });
        if (publicHomes.remove(home) && !home.isPublic()) {
            plugin.removeMappedHome(home);
        }
        if (home.isPublic()) {
            publicHomes.add(home);
            plugin.addMappedHome(home);
        }

        plugin.getCommands().stream()
                .filter(command -> command instanceof ListCommand)
                .map(command -> (ListCommand) command)
                .forEach(ListCommand::invalidateCaches);
        if (propagate) {
            propagateCacheUpdate(home.getUuid());
        }
    }

    public void unCacheHome(@NotNull UUID homeId, boolean propagate) {
        userHomes.values().forEach(homes -> homes.removeIf(home -> home.getUuid().equals(homeId)));
        publicHomes.removeIf(home -> {
            if (home.getUuid().equals(homeId)) {
                plugin.removeMappedHome(home);
                return true;
            }
            return false;
        });

        plugin.getCommands().stream()
                .filter(command -> command instanceof ListCommand)
                .map(command -> (ListCommand) command)
                .forEach(ListCommand::invalidateCaches);
        if (propagate) {
            this.propagateCacheUpdate(homeId);
        }
    }

    /**
     * Propagate the update of a home/warp to other servers (if cross-server is enabled).
     *
     * <p>This works by broking a message requesting that other servers fetch the updated home from the database.
     *
     * @param homeId the UUID of the home/warp to update
     */
    private void propagateCacheUpdate(@NotNull UUID homeId) {
        plugin.getBroker().ifPresent(b -> plugin.getOnlineUsers().stream().findAny()
                .ifPresent(user -> Message.builder()
                        .type(Message.MessageType.UPDATE_HOME)
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .payload(Payload.string(homeId.toString()))
                        .build().send(b, user)));
    }

    public void updatePublicHomeCache() {
        plugin.getDatabase().getPublicHomes().forEach(home -> cacheHome(home, false));
    }

    public void removeUserHomes(@NotNull User user) {
        userHomes.remove(user.getUuid().toString());
    }

    @NotNull
    public Home createHome(@NotNull User owner, @NotNull String name, @NotNull Position position,
                           boolean overwrite, boolean buyAdditionalSlots, boolean ignoreMaxHomes,
                           boolean ignoreHomeSlots)
            throws ValidationException {
        final Optional<Home> existingHome = plugin.getDatabase().getHome(owner, name);
        if (existingHome.isPresent() && !overwrite) {
            throw new ValidationException(ValidationException.Type.NAME_TAKEN);
        }

        // Validate the home name; throw an exception if invalid
        plugin.validateName(name);

        // Determine what the new home count would be & validate against user max homes
        int homes = plugin.getDatabase().getHomes(owner).size() + (existingHome.isPresent() ? 0 : 1);
        if (!ignoreMaxHomes && homes > getMaxHomes(owner)) {
            throw new ValidationException(ValidationException.Type.REACHED_MAX_HOMES);
        }

        // Validate against user home slots
        final SavedUser savedOwner = plugin.getSavedUser(owner)
                .or(() -> plugin.getDatabase().getUser(owner.getUuid()))
                .orElseThrow(() -> new IllegalStateException("User data not found for " + owner.getUuid()));
        if (plugin.isUsingEconomy() && !ignoreHomeSlots && homes > getFreeHomes(owner)
                && homes > savedOwner.getHomeSlots()) {
            if (!buyAdditionalSlots || !(owner instanceof OnlineUser online)) {
                throw new ValidationException(ValidationException.Type.NOT_ENOUGH_HOME_SLOTS);
            }

            // Perform transaction and increase user slot size
            if (!plugin.validateTransaction(online, TransactionResolver.Action.ADDITIONAL_HOME_SLOT)) {
                throw new ValidationException(ValidationException.Type.TRANSACTION_FAILED);
            }
            plugin.performTransaction(online, TransactionResolver.Action.ADDITIONAL_HOME_SLOT);
            plugin.editSavedUser(online, (SavedUser saved) -> saved.setHomeSlots(saved.getHomeSlots() + 1));
        }

        final Home home = existingHome
                .map(existing -> {
                    existing.getMeta().setName(name);
                    existing.update(position);
                    return existing;
                })
                .orElse(Home.from(position, PositionMeta.create(name, ""), owner));
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
        return home;
    }

    public void createHome(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position)
            throws ValidationException {
        this.createHome(
                owner, name, position, plugin.getSettings().getGeneral().getNames().isOverwriteExisting(),
                true, false, false
        );
    }

    public void deleteHome(@NotNull User owner, @NotNull String name) throws ValidationException {
        final Optional<Home> home = plugin.getDatabase().getHome(owner, name);
        if (home.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.deleteHome(home.get());
    }

    public void deleteHome(@NotNull Home home) {
        plugin.getDatabase().deleteHome(home.getUuid());
        this.unCacheHome(home.getUuid(), true);
    }

    public int deleteAllHomes(@NotNull User owner) {
        final int deleted = plugin.getDatabase().deleteAllHomes(owner);
        userHomes.computeIfPresent(owner.getName(), (k, v) -> {
            v.clear();
            return v;
        });
        publicHomes.removeIf(h -> h.getOwner().getUuid().equals(owner.getUuid()));
        plugin.removeAllMappedHomes(owner);
        plugin.getCommands().stream()
                .filter(command -> command instanceof ListCommand)
                .map(command -> (ListCommand) command)
                .forEach(ListCommand::invalidateCaches);
        plugin.getManager().propagateCacheUpdate();
        return deleted;
    }

    public int deleteAllHomes(@NotNull String worldName, @NotNull String serverName) {
        final int deleted = plugin.getDatabase().deleteAllHomes(worldName, serverName);
        userHomes.values().forEach(homes -> homes.removeIf(
                h -> h.getWorld().getName().equals(worldName) && h.getServer().equals(serverName)
        ));
        publicHomes.removeIf(h -> h.getWorld().getName().equals(worldName) && h.getServer().equals(serverName));
        if (plugin.getSettings().getCrossServer().isEnabled() && serverName.equals(plugin.getServerName())) {
            plugin.removeAllMappedHomes(worldName);
        }
        plugin.getCommands().stream()
                .filter(command -> command instanceof ListCommand)
                .map(command -> (ListCommand) command)
                .forEach(ListCommand::invalidateCaches);
        plugin.getManager().propagateCacheUpdate();
        return deleted;
    }

    public void setHomePosition(@NotNull User owner, @NotNull String name,
                                @NotNull Position position) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomePosition(optionalHome.get(), position);
    }

    public void setHomePosition(@NotNull Home home, @NotNull Position position) throws ValidationException {
        home.update(position);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void setHomeName(@NotNull User owner, @NotNull String name,
                            @NotNull String newName) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomeName(optionalHome.get(), newName);
    }

    public void setHomeName(@NotNull Home home, @NotNull String newName) throws ValidationException {
        if (plugin.getDatabase().getHome(home.getOwner(), newName).isPresent()) {
            throw new ValidationException(ValidationException.Type.NAME_TAKEN);
        }
        plugin.validateName(newName);
        home.getMeta().setName(newName);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void setHomeDescription(@NotNull User owner, @NotNull String name,
                                   @NotNull String description) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomeDescription(optionalHome.get(), description);
    }

    public void setHomeDescription(@NotNull Home home, @NotNull String description) {
        plugin.validateDescription(description);
        home.getMeta().setDescription(description);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void setHomePrivacy(@NotNull User owner, @NotNull String name, boolean isPublic) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomePrivacy(owner, optionalHome.get(), isPublic);
    }

    public void setHomePrivacy(@NotNull Home home, boolean isPublic) {
        this.setHomePrivacy(home.getOwner(), home, isPublic);
    }

    public void setHomePrivacy(@NotNull User owner, @NotNull Home home, boolean isPublic) {
        if (isPublic && owner instanceof OnlineUser online) {
            int publicHomes = plugin.getDatabase().getHomes(owner).stream().filter(Home::isPublic).toList().size();
            if (publicHomes >= getMaxPublicHomes(online)) {
                throw new ValidationException(ValidationException.Type.REACHED_MAX_PUBLIC_HOMES);
            }
        }

        home.setPublic(isPublic);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void setHomeMetaTags(@NotNull User owner, @NotNull String name,
                                @NotNull Map<String, String> tags) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomeMetaTags(optionalHome.get(), tags);
    }

    public void setHomeMetaTags(@NotNull Home home, @NotNull Map<String, String> tags) {
        home.getMeta().setTags(tags);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public int getMaxHomes(@Nullable User user) {
        return user instanceof OnlineUser online ? online.getMaxHomes(
                plugin.getSettings().getGeneral().getMaxHomes(),
                plugin.getSettings().getGeneral().isStackPermissionLimits()
        ) : plugin.getSettings().getGeneral().getMaxHomes();
    }

    public int getMaxPublicHomes(@Nullable User user) {
        return user instanceof OnlineUser online ? online.getMaxPublicHomes(
                plugin.getSettings().getGeneral().getMaxPublicHomes(),
                plugin.getSettings().getGeneral().isStackPermissionLimits()
        ) : plugin.getSettings().getGeneral().getMaxPublicHomes();
    }

    public int getFreeHomes(@Nullable User user) {
        return user instanceof OnlineUser online ? online.getFreeHomes(
                plugin.getSettings().getEconomy().getFreeHomeSlots(),
                plugin.getSettings().getGeneral().isStackPermissionLimits()
        ) : plugin.getSettings().getEconomy().getFreeHomeSlots();
    }

}
