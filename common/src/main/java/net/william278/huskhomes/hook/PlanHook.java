package net.william278.huskhomes.hook;

import com.djrapitops.plan.capability.CapabilityService;
import com.djrapitops.plan.extension.ExtensionService;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.HuskHomesInitializationException;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Hooks into Plan to provide the {@link PlanDataExtension} with stats on the web panel
 */
public class PlanHook extends PluginHook {

    private final Database database;
    private final Logger logger;

    public PlanHook(@NotNull HuskHomes implementor) {
        super(implementor, "Plan");
        this.database = implementor.getDatabase();
        this.logger = implementor.getLoggingAdapter();
    }

    @Override
    public boolean initialize() throws HuskHomesInitializationException {
        if (!areAllCapabilitiesAvailable()) {
            return false;
        }
        registerDataExtension();
        handlePlanReload();
        return true;
    }


    private boolean areAllCapabilitiesAvailable() {
        CapabilityService capabilities = CapabilityService.getInstance();
        return capabilities.hasCapability("DATA_EXTENSION_VALUES");
    }

    private void registerDataExtension() {
        try {
            ExtensionService.getInstance().register(new PlanDataExtension(database, plugin.getSettings().crossServer));
        } catch (IllegalStateException planIsNotEnabled) {
            logger.log(Level.SEVERE, "Plan extension hook failed to register. Plan is not enabled.", planIsNotEnabled);
            // Plan is not enabled, handle exception
        } catch (IllegalArgumentException dataExtensionImplementationIsInvalid) {
            logger.log(Level.SEVERE, "Plan extension hook failed to register. Data hook implementation is invalid.", dataExtensionImplementationIsInvalid);
            // The DataExtension implementation has an implementation error, handle exception
        }
    }

    // Re-register the extension when plan enables
    private void handlePlanReload() {
        CapabilityService.getInstance().registerEnableListener(isPlanEnabled -> {
            if (isPlanEnabled) {
                registerDataExtension();
            }
        });
    }
}