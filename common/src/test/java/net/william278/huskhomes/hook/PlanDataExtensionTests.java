package net.william278.huskhomes.hook;

import com.djrapitops.plan.extension.extractor.ExtensionExtractor;
import org.junit.jupiter.api.Test;

public class PlanDataExtensionTests {

    // Throws IllegalArgumentException if there is an implementation error or warning.
    @Test
    public void testPlanDataExtension() {
        new ExtensionExtractor(new PlanHook.PlanDataExtension()).validateAnnotations();
    }

}
