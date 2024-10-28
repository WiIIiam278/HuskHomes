package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

public interface ManagerProvider {

    default void loadManager() {
        setManager(new Manager(getPlugin()));
    }

    @NotNull
    Manager getManager();

    void setManager(Manager manager);

    @NotNull
    HuskHomes getPlugin();

}
