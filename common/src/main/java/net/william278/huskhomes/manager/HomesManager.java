package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

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
     * Cached user homes - maps a username to a list of their homes
     */
    @NotNull
    public Map<String, List<String>> getUserHomes() {
        return userHomes.entrySet().stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().stream().map(Home::getName).toList()),
                        HashMap::putAll);
    }

    public void cacheUserHomes(@NotNull User user) {
        userHomes.put(user.getUsername(), new ConcurrentLinkedQueue<>(plugin.getDatabase().getHomes(user)));
    }

    public void cacheHome(@NotNull Home home, boolean propagate) {
        userHomes.computeIfPresent(home.getOwner().getUsername(), (k, v) -> {
            v.remove(home);
            v.add(home);
            return v;
        });
        if (publicHomes.remove(home) && !home.isPublic()) {
            plugin.getMapHook().ifPresent(hook -> hook.removeHome(home));
        }
        if (home.isPublic()) {
            publicHomes.add(home);
            plugin.getMapHook().ifPresent(hook -> hook.updateHome(home));
        }

        if (propagate) {
            propagateCacheUpdate(home.getUuid());
        }
    }

    public void unCacheHome(@NotNull UUID homeId, boolean propagate) {
        userHomes.values().forEach(homes -> homes.removeIf(home -> home.getUuid().equals(homeId)));
        publicHomes.removeIf(home -> {
            if (home.getUuid().equals(homeId)) {
                plugin.getMapHook().ifPresent(hook -> hook.removeHome(home));
                return true;
            }
            return false;
        });

        if (propagate) {
            this.propagateCacheUpdate(homeId);
        }
    }

    private void propagateCacheUpdate(@NotNull UUID homeId) {
        if (plugin.getSettings().isCrossServer()) {
            plugin.getOnlineUsers().stream().findAny().ifPresent(user -> Message.builder()
                    .type(Message.Type.UPDATE_HOME)
                    .scope(Message.Scope.SERVER)
                    .target(Message.TARGET_ALL)
                    .payload(Payload.withString(homeId.toString()))
                    .build().send(plugin.getMessenger(), user));
        }
    }

    public void updatePublicHomeCache() {
        plugin.getDatabase().getPublicHomes().forEach(home -> cacheHome(home, false));
    }

    public void removeUserHomes(@NotNull User user) {
        userHomes.remove(user.getUuid().toString());
    }

    /**
     * Cached public homes - maps a username to a list of their public homes
     */
    @NotNull
    public Map<String, List<String>> getPublicHomes() {
        return publicHomes.stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getOwner().getUsername(), List.of(e.getName())),
                        HashMap::putAll);
    }

    public void createHome(@NotNull User owner, @NotNull String name, @NotNull Position position,
                           boolean overwrite, boolean buyAdditionalSlots) throws ValidationException {
        final Optional<Home> existingHome = plugin.getDatabase().getHome(owner, name);
        if (existingHome.isPresent() && !overwrite) {
            throw new ValidationException(ValidationException.Type.NAME_TAKEN);
        }
        if (!plugin.getValidator().isValidName(name)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID);
        }

        // Validate against user max homes
        final int homeCount = plugin.getDatabase().getHomes(owner).size();
        int maxHomes = plugin.getSettings().getMaxHomes();
        if (owner instanceof OnlineUser online) {
            maxHomes = online.getMaxHomes(maxHomes, plugin.getSettings().doStackPermissionLimits());
        }
        if (homeCount >= maxHomes) {
            throw new ValidationException(ValidationException.Type.REACHED_MAX_HOMES);
        }

        // Validate against user home slots
        final SavedUser savedOwner = plugin.getDatabase().getUserData(owner.getUuid())
                .orElseThrow(() -> new IllegalStateException("User data not found for " + owner.getUuid()));
        if (plugin.getSettings().doEconomy() && homeCount >= savedOwner.getHomeSlots()) {
            if (!buyAdditionalSlots || plugin.getEconomyHook().isEmpty() || !(owner instanceof OnlineUser online)) {
                throw new ValidationException(ValidationException.Type.NOT_ENOUGH_HOME_SLOTS);
            }

            if (!plugin.validateEconomyCheck(online, EconomyHook.Action.ADDITIONAL_HOME_SLOT)) {
                throw new ValidationException(ValidationException.Type.NOT_ENOUGH_MONEY);
            }

            plugin.performEconomyTransaction(online, EconomyHook.Action.ADDITIONAL_HOME_SLOT);
            savedOwner.setHomeSlots(savedOwner.getHomeSlots() + 1);
            plugin.getDatabase().updateUserData(savedOwner);
        }

        final Home home = existingHome
                .map(existing -> {
                    existing.update(position);
                    return existing;
                })
                .orElse(Home.from(position, PositionMeta.create(name, ""), owner));
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void createHome(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) throws ValidationException {
        createHome(owner, name, position, plugin.getSettings().doOverwriteExistingHomesWarps(), true);
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
        userHomes.computeIfPresent(owner.getUsername(), (k, v) -> {
            v.clear();
            return v;
        });
        publicHomes.removeIf(h -> h.getOwner().getUuid().equals(owner.getUuid()));
        plugin.getMapHook().ifPresent(hook -> hook.clearHomes(owner));
        plugin.getManager().propagateCacheUpdate();
        return deleted;
    }

    public void setHomePosition(@NotNull User owner, @NotNull String name, @NotNull Position position) throws ValidationException {
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

    public void setHomeName(@NotNull User owner, @NotNull String name, @NotNull String newName) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomeName(optionalHome.get(), newName);
    }

    public void setHomeName(@NotNull Home home, @NotNull String newName) throws ValidationException {
        if (!plugin.getValidator().isValidName(newName)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID);
        }

        home.getMeta().setName(newName);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void setHomeDescription(@NotNull User owner, @NotNull String name, @NotNull String description) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomeDescription(optionalHome.get(), description);
    }

    public void setHomeDescription(@NotNull Home home, @NotNull String description) {
        if (!plugin.getValidator().isValidDescription(description)) {
            throw new ValidationException(ValidationException.Type.DESCRIPTION_INVALID);
        }

        home.getMeta().setDescription(description);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void setHomePrivacy(@NotNull User owner, @NotNull String name, boolean isPublic) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setHomePrivacy(optionalHome.get(), isPublic);
    }

    public void setHomePrivacy(@NotNull Home home, boolean isPublic) {
        if (isPublic && home.getOwner() instanceof OnlineUser online) {
            final int publicHomes = plugin.getDatabase().getHomes(home.getOwner()).stream()
                    .filter(Home::isPublic)
                    .toList().size();
            final int publicSlots = online.getMaxPublicHomes(plugin.getSettings().getMaxPublicHomes(),
                    plugin.getSettings().doStackPermissionLimits());
            if (publicHomes >= publicSlots) {
                throw new ValidationException(ValidationException.Type.REACHED_MAX_PUBLIC_HOMES);
            }
        }

        home.setPublic(isPublic);
        plugin.getDatabase().saveHome(home);
        this.cacheHome(home, true);
    }

    public void setHomeMetaTags(@NotNull User owner, @NotNull String name, @NotNull Map<String, String> tags) throws ValidationException {
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

}
