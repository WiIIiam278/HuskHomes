package net.william278.huskhomes;

import net.fabricmc.api.ClientModInitializer;

public class FabricHuskHomesClient extends FabricHuskHomes implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        this.onInitialize();
    }

}
