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

import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Section;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.availability.rule.AbstractAvailabilityRule;
import info.magnolia.ui.contentapp.ContentBrowserSubApp;

import java.util.Optional;

import javax.inject.Inject;

/**
 * {@link info.magnolia.ui.api.availability.AvailabilityRule} implementation which check if item can be moved inside/before/after of selected item.
 * @param <T> Type.
 */
public class CanMoveAboveBelowRule<T> extends AbstractAvailabilityRule<T, CanMoveAboveBelowRuleDefinition> {

    private final DbItemResolver itemResolver;
    private final SubAppContext subAppContext;

    @Inject
    public CanMoveAboveBelowRule(AvailabilityDefinition availabilityDefinition, CanMoveAboveBelowRuleDefinition ruleDefinition, DbItemResolver itemResolver, SubAppContext subAppContext) {
        super(availabilityDefinition, ruleDefinition);
        this.itemResolver = itemResolver;
        this.subAppContext = subAppContext;
    }

    @Override
    protected boolean isAvailableFor(T target) {
        return Optional.of(subAppContext)
                .map(SubAppContext::getLocation)
                .map(ContentBrowserSubApp.BrowserLocation::wrap)
                .map(ContentBrowserSubApp.BrowserLocation::getNodePath)
                .map(path -> {
                    Optional<BaseModel> optionalItemToMove =  itemResolver.getItemById(path);
                    if (optionalItemToMove.isPresent()) {
                        BaseModel itemToMove = optionalItemToMove.get();
                        if ((itemToMove instanceof Form) && !(target instanceof Form)) {
                            return false;
                        }
                        if ((itemToMove instanceof Section) && !(target instanceof Section)) {
                            return false;
                        }
                        if ((itemToMove instanceof Question) && !(target instanceof Question)) {
                            return false;
                        }
                        if ((itemToMove instanceof AnswerOption) && !(target instanceof AnswerOption)) {
                            return false;
                        }
                        return true;
                    }
                    return false;
                })
                .orElse(true);
    }
}
