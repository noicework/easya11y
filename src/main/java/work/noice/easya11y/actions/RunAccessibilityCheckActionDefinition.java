package work.noice.easya11y.actions;

import info.magnolia.ui.api.action.ConfiguredActionDefinition;

/**
 * Magnolia action definition for the accessibility check action.
 */
public class RunAccessibilityCheckActionDefinition extends ConfiguredActionDefinition {

    @SuppressWarnings("deprecation")
    public RunAccessibilityCheckActionDefinition() {
        setImplementationClass(RunAccessibilityCheckAction.class);
    }
}
