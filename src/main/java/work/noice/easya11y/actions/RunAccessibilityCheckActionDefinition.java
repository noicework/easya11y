package work.noice.easya11y.actions;

import info.magnolia.ui.api.action.Action;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

/**
 * Magnolia action definition for the accessibility check action.
 */
public class RunAccessibilityCheckActionDefinition extends ConfiguredActionDefinition {

    @Override
    public Class<? extends Action> getImplementationClass() {
        return RunAccessibilityCheckAction.class;
    }
}
