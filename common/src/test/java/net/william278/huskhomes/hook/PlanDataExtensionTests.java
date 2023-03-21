package net.william278.huskhomes.hook;

import com.djrapitops.plan.extension.extractor.ExtensionExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Plan Hook Tests")
public class PlanDataExtensionTests {

    @Test
    @DisplayName("Test Plan Hook Implementation")
    public void testPlanHookImplementation() {
        new ExtensionExtractor(new PlanHook.PlanDataExtension()).validateAnnotations();
    }

}
