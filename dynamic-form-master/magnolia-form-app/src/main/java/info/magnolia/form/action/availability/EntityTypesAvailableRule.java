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
import info.magnolia.ui.availability.rule.AbstractAvailabilityRule;

import javax.inject.Inject;

/**
 * Available rule based on configured entity types.
 */
public class EntityTypesAvailableRule extends AbstractAvailabilityRule {

    EntityTypesAvailableRuleDefinition ruleDefinition;

    @Inject
    public EntityTypesAvailableRule(AvailabilityDefinition availabilityDefinition, EntityTypesAvailableRuleDefinition ruleDefinition) {
        super(availabilityDefinition, ruleDefinition);
        this.ruleDefinition = ruleDefinition;
    }

    @Override
    protected boolean isAvailableFor(Object item) {
        return ruleDefinition.getEntityTypes().contains(item.getClass().getSimpleName());
    }
}
