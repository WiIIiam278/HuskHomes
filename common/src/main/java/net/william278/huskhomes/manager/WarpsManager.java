package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.command.ListCommand;
import net.william278.huskhomes.hook.MapHook;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WarpsManager {
    private final HuskHomes plugin;
    private final ConcurrentLinkedQueue<Warp> warps;

    protected WarpsManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.warps = new ConcurrentLinkedQueue<>(plugin.getDatabase().getWarps());
    }

    public void cacheWarp(@NotNull Warp warp, boolean propagate) {
        warps.remove(warp);
        warps.add(warp);
        plugin.getMapHook().ifPresent(hook -> hook.updateWarp(warp));

        plugin.getCommands().stream()
                .filter(command -> command instanceof ListCommand)
                .map(command -> (ListCommand) command)
                .forEach(ListCommand::invalidateCaches);
        if (propagate) {
            this.propagateCacheUpdate(warp.getUuid());
        }
    }

    public void unCacheWarp(@NotNull UUID warpId, boolean propagate) {
        warps.removeIf(warp -> {
            if (warp.getUuid().equals(warpId)) {
                plugin.getMapHook().ifPresent(hook -> hook.removeWarp(warp));
                return true;
            }
            return false;
        });

        plugin.getCommands().stream()
                .filter(command -> command instanceof ListCommand)
                .map(command -> (ListCommand) command)
                .forEach(ListCommand::invalidateCaches);
        if (propagate) {
            this.propagateCacheUpdate(warpId);
        }
    }

    private void propagateCacheUpdate(@NotNull UUID warpId) {
        if (plugin.getSettings().doCrossServer()) {
            plugin.getOnlineUsers().stream().findAny().ifPresent(user -> Message.builder()
                    .type(Message.Type.UPDATE_WARP)
                    .scope(Message.Scope.SERVER)
                    .target(Message.TARGET_ALL)
                    .payload(Payload.withString(warpId.toString()))
                    .build().send(plugin.getMessenger(), user));
        }
    }

    public void updateWarpCache() {
        plugin.getDatabase().getWarps().forEach(warp -> cacheWarp(warp, false));
    }

    /**
     * Cached warp names
     */
    @NotNull
    public List<String> getWarps() {
        return warps.stream().map(Warp::getName).toList();
    }

    @NotNull
    public List<String> getUsableWarps(@NotNull CommandUser user) {
        if (!plugin.getSettings().doPermissionRestrictWarps()) {
            return getWarps();
        }
        return warps.stream()
                .filter(warp -> user.hasPermission(warp.getPermission()) || user.hasPermission(Warp.getWildcardPermission()))
                .map(Warp::getName)
                .toList();
    }

    public void createWarp(@NotNull String name, @NotNull Position position, boolean overwrite) throws ValidationException {
        final Optional<Warp> existingWarp = plugin.getDatabase().getWarp(name);
        if (existingWarp.isPresent() && !overwrite) {
            throw new ValidationException(ValidationException.Type.NAME_TAKEN);
        }

        if (!plugin.getValidator().isValidName(name)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID);
        }

        final Warp warp = existingWarp
                .map(existing -> {
                    existing.setX(position.getX());
                    existing.setY(position.getY());
                    existing.setZ(position.getZ());
                    existing.setWorld(position.getWorld());
                    existing.setServer(position.getServer());
                    existing.setYaw(position.getYaw());
                    existing.setPitch(position.getPitch());
                    return existing;
                })
                .orElse(Warp.from(position, PositionMeta.create(name, "")));
        plugin.getDatabase().saveWarp(warp);
        this.cacheWarp(warp, true);
    }

    public void createWarp(@NotNull String name, @NotNull Position position) throws ValidationException {
        this.createWarp(name, position, plugin.getSettings().doOverwriteExistingHomesWarps());
    }

    public void deleteWarp(@NotNull String name) throws ValidationException {
        final Optional<Warp> warp = plugin.getDatabase().getWarp(name);
        if (warp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.deleteWarp(warp.get());
    }

    public void deleteWarp(@NotNull Warp warp) {
        plugin.getDatabase().deleteWarp(warp.getUuid());
        this.unCacheWarp(warp.getUuid(), true);
    }

    public int deleteAllWarps() {
        final int deleted = plugin.getDatabase().deleteAllWarps();
        warps.clear();
        plugin.getMapHook().ifPresent(MapHook::clearWarps);
        plugin.getManager().propagateCacheUpdate();
        return deleted;
    }

    public void setWarpPosition(@NotNull String name, @NotNull Position position) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setWarpPosition(optionalWarp.get(), position);
    }

    public void setWarpPosition(@NotNull Warp warp, @NotNull Position position) {
        warp.update(position);
        plugin.getDatabase().saveWarp(warp);
        this.cacheWarp(warp, true);
    }

    public void setWarpName(@NotNull String name, @NotNull String newName) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setWarpName(optionalWarp.get(), newName);
    }

    public void setWarpName(@NotNull Warp warp, @NotNull String newName) throws ValidationException {
        if (!plugin.getValidator().isValidName(newName)) {
            throw new ValidationException(ValidationException.Type.NAME_INVALID);
        }

        warp.getMeta().setName(newName);
        plugin.getDatabase().saveWarp(warp);
        this.cacheWarp(warp, true);
    }

    public void setWarpDescription(@NotNull String name, @NotNull String description) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setWarpDescription(optionalWarp.get(), description);
    }

    public void setWarpDescription(@NotNull Warp warp, @NotNull String description) {
        if (!plugin.getValidator().isValidDescription(description)) {
            throw new ValidationException(ValidationException.Type.DESCRIPTION_INVALID);
        }

        warp.getMeta().setDescription(description);
        plugin.getDatabase().saveWarp(warp);
        this.cacheWarp(warp, true);
    }

    public void setWarpMetaTags(@NotNull String name, @NotNull Map<String, String> tags) throws ValidationException {
        final Optional<Warp> optionalWarp = plugin.getDatabase().getWarp(name);
        if (optionalWarp.isEmpty()) {
            throw new ValidationException(ValidationException.Type.NOT_FOUND);
        }

        this.setWarpMetaTags(optionalWarp.get(), tags);
    }

    public void setWarpMetaTags(@NotNull Warp warp, @NotNull Map<String, String> tags) {
        warp.getMeta().setTags(tags);
        plugin.getDatabase().saveWarp(warp);
        this.cacheWarp(warp, true);
    }

}
