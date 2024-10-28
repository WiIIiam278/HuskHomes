package net.william278.huskhomes.listener;

import org.jetbrains.annotations.NotNull;

public interface ListenerProvider {

    @NotNull
    EventListener createListener();

    default void loadListeners() {
        createListener().register();
    }


}
