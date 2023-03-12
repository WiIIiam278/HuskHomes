package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.util.ValidationException;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HomesManager {
    private final HuskHomes plugin;

    protected HomesManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
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

            plugin.performEconomyTransaction(online, EconomyHook.Action.ADDITIONAL_HOME_SLOT);
            savedOwner.setHomeSlots(savedOwner.getHomeSlots() + 1);
            plugin.getDatabase().updateUserData(savedOwner);
        }

        final Home home = existingHome
                .map(existing -> {
                    existing.update(position);
                    return existing;
                })
                .orElse(new Home(position, new PositionMeta(name, ""), owner));
        plugin.getDatabase().saveHome(home);
    }

    public void createHome(@NotNull OnlineUser owner, @NotNull String name, @NotNull Position position) throws ValidationException {
        createHome(owner, name, position, plugin.getSettings().doOverwriteExistingHomesWarps(), true);
    }

    public void deleteHome(@NotNull User owner, @NotNull String name) throws ValidationException {
        final Optional<Home> home = plugin.getDatabase().getHome(owner, name);
        if (home.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }
        plugin.getDatabase().deleteHome(home.get().getUuid());
    }

    public int deleteAllHomes(@NotNull User owner) {
        return plugin.getDatabase().deleteAllHomes(owner);
    }

    public void relocateHome(@NotNull User owner, @NotNull String name, @NotNull Position position) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        final Home home = optionalHome.get();
        home.update(position);
        plugin.getDatabase().saveHome(home);
    }

    public void renameHome(@NotNull User owner, @NotNull String name, @NotNull String newName) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        if (!plugin.getValidator().isValidName(newName)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID);
        }

        final Home home = optionalHome.get();
        home.getMeta().setName(newName);
        plugin.getDatabase().saveHome(home);
    }

    public void updateHomeDescription(@NotNull User owner, @NotNull String name, @NotNull String description) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        if (!plugin.getValidator().isValidDescription(description)) {
            throw new ValidationException(ValidationException.Type.DESCRIPTION_INVALID);
        }

        final Home home = optionalHome.get();
        home.getMeta().setDescription(description);
        plugin.getDatabase().saveHome(home);
    }

    public void updateHomePrivacy(@NotNull User owner, @NotNull String name, boolean isPublic) throws ValidationException {
        final Optional<Home> optionalHome = plugin.getDatabase().getHome(owner, name);
        if (optionalHome.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        final Home home = optionalHome.get();
        if (isPublic == home.isPublic()) {
            return;
        }

        if (isPublic && owner instanceof OnlineUser online) {
            final int publicHomes = plugin.getDatabase().getHomes(owner).stream()
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
    }

}
