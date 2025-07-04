/**
 * This file Copyright (c) 2010-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.form.action.availability;


import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRuleDefinition;
import info.magnolia.ui.availability.rule.AbstractAvailabilityRule;

/**
 * Is root available rule.
 */
public class IsRootActionRule extends AbstractAvailabilityRule {

    public IsRootActionRule(AvailabilityDefinition availabilityDefinition, AvailabilityRuleDefinition ruleDefinition) {
        super(availabilityDefinition, ruleDefinition);
    }

    @Override
    protected boolean isAvailableFor(Object item) {
        return (item == null);
    }
}
