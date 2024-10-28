package net.william278.huskhomes.hook;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface MapHookProvider {

    @NotNull
    @Unmodifiable
    default Set<MapHook> getMapHooks() {
        return getHooks().stream()
                .filter(hook -> hook instanceof MapHook)
                .map(hook -> (MapHook) hook)
                .collect(Collectors.toSet());
    }

    //todo support multiple simultaneous map hooks
    @Deprecated(since = "4.8")
    default Optional<MapHook> getMapHook() {
        return getMapHooks().stream().findFirst();
    }

    @NotNull
    Set<Hook> getHooks();

}
