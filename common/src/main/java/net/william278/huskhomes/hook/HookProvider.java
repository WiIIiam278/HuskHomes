package net.william278.huskhomes.hook;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.importer.Importer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public interface HookProvider extends MapHookProvider {

    @NotNull
    Set<Hook> getHooks();

    default <H extends Hook> Optional<H> getHook(@NotNull Class<H> hookClass) {
        return getHooks().stream()
                .filter(hook -> hookClass.isAssignableFrom(hook.getClass()))
                .map(hookClass::cast)
                .findFirst();
    }

    @NotNull
    @Unmodifiable
    default Set<Importer> getImporters() {
        return getHooks().stream()
                .filter(hook -> hook instanceof Importer)
                .map(hook -> (Importer) hook)
                .collect(Collectors.toSet());
    }

    default Optional<Importer> getImporterByName(@NotNull String name) {
        return getImporters().stream()
                .filter(i -> i.getName().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH)))
                .findFirst();
    }

    void setHooks(@NotNull Set<Hook> hooks);

    default void loadHooks() {
        setHooks(Sets.newHashSet(getAvailableHooks()));
    }

    default void registerHooks(@NotNull PluginHook.Register... register) {
        final Set<PluginHook.Register> registers = Arrays.stream(register).collect(Collectors.toSet());
        getHooks().removeIf(hook -> {
            if (!registers.contains(hook.getRegister())) {
                return false;
            }
            try {
                hook.load();
                return false;
            } catch (Throwable e) {
                getPlugin().log(Level.SEVERE, "Failed to register the %s hook".formatted(hook.getName()), e);
            }
            return true;
        });
        getPlugin().log(Level.INFO, "Registered '%s' hooks".formatted(registers.stream()
                .map(h -> h.name().toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" & "))));
    }

    default void unloadHooks() {
        getHooks().removeIf(hook -> {
            try {
                hook.unload();
            } catch (Throwable e) {
                getPlugin().log(Level.SEVERE, "Failed to unload the %s hook".formatted(hook.getName()), e);
            }
            return true;
        });
    }

    @NotNull
    default List<Hook> getAvailableHooks() {
        final List<Hook> hooks = Lists.newArrayList();

        if (getPlugin().getSettings().getMapHook().isEnabled()) {
            if (isDependencyAvailable("Dynmap")) {
                getHooks().add(new DynmapHook(getPlugin()));
            } else if (isDependencyAvailable("BlueMap")) {
                getHooks().add(new BlueMapHook(getPlugin()));
            } else if (isDependencyAvailable("Pl3xMap")) {
                getHooks().add(new Pl3xMapHook(getPlugin()));
            }
        }
        if (isDependencyAvailable("Plan")) {
            getHooks().add(new PlanHook(getPlugin()));
        }

        return hooks;
    }

    boolean isDependencyAvailable(@NotNull String name);

    @NotNull
    HuskHomes getPlugin();

}
