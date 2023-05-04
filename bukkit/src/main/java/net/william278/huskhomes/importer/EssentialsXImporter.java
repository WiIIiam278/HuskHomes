package net.william278.huskhomes.importer;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.BukkitAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class EssentialsXImporter extends Importer {

    private final Essentials essentials;

    public EssentialsXImporter(@NotNull HuskHomes plugin) {
        super("EssentialsX", List.of(ImportData.USERS, ImportData.HOMES, ImportData.WARPS), plugin);
        this.essentials = (Essentials) ((BukkitHuskHomes) plugin).getServer().getPluginManager().getPlugin("Essentials");
    }

    private int importUsers() {
        final AtomicInteger usersImported = new AtomicInteger();
        for (UUID uuid : essentials.getUserMap().getAllUniqueUsers()) {
            final User user = User.of(
                    uuid,
                    essentials.getUser(uuid).getLastAccountName()
            );
            plugin.getDatabase().ensureUser(user);
            plugin.editUserData(user, (editor -> editor.setHomeSlots(Math.max(
                    plugin.getSettings().getFreeHomeSlots(),
                    essentials.getUser(uuid).getHomes().size()
            ))));
            usersImported.getAndIncrement();
        }
        return usersImported.get();
    }

    private int importHomes() {
        final AtomicInteger homesImported = new AtomicInteger();
        for (UUID uuid : essentials.getUserMap().getAllUniqueUsers()) {
            final com.earth2me.essentials.User essentialsUser = essentials.getUser(uuid);
            for (String homeName : essentialsUser.getHomes()) {
                BukkitAdapter.adaptLocation(essentialsUser.getHome(homeName))
                        .map(location -> Position.at(location, plugin.getServerName()))
                        .ifPresent(position -> {
                            plugin.getManager().homes().createHome(
                                    User.of(uuid, essentialsUser.getLastAccountName()),
                                    this.normalizeName(homeName),
                                    position,
                                    true, true
                            );
                            homesImported.getAndIncrement();
                        });
            }
        }
        return homesImported.get();
    }

    private int importWarps() throws Throwable {
        final AtomicInteger warpsImported = new AtomicInteger();
        final Warps warps = essentials.getWarps();
        for (String warpName : warps.getList()) {
            BukkitAdapter.adaptLocation(warps.getWarp(warpName))
                    .map(location -> Position.at(location, plugin.getServerName()))
                    .ifPresent(position -> {
                        plugin.getManager().warps().createWarp(
                                this.normalizeName(warpName),
                                position,
                                true
                        );
                        warpsImported.getAndIncrement();
                    });
        }
        return warpsImported.get();
    }

    @NotNull
    private String normalizeName(@NotNull String name) {
        if (plugin.getValidator().isValidName(name)) {
            return name;
        }

        // Remove spaces
        name = name.replaceAll(" ", "_");

        // Remove unicode characters
        if (!plugin.getSettings().doAllowUnicodeNames()) {
            name = name.replaceAll("[^A-Za-z0-9_-]", "");
        }

        // Ensure the name is not blank
        if (name.isBlank()) {
            name = "imported-" + UUID.randomUUID().toString().substring(0, 5);
        }

        // Ensure name is not too long
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }
        return name;
    }

    @Override
    protected int importData(@NotNull Importer.ImportData importData) throws Throwable {
        return switch (importData) {
            case USERS -> importUsers();
            case HOMES -> importHomes();
            case WARPS -> importWarps();
        };
    }

}
