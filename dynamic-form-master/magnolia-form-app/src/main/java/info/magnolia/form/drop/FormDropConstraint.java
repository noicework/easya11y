/**
 * This file Copyright (c) 2021 Magnolia International
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
package info.magnolia.form.drop;

import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Section;
import info.magnolia.ui.contentapp.browser.drop.AbstractDropConstraint;

import javax.inject.Inject;

import com.vaadin.shared.ui.grid.DropLocation;

/**
 * Defines where item can be dropped.
 */
public class FormDropConstraint  extends AbstractDropConstraint<FormDropConstraintDefinition, BaseModel> {
    @Inject
    public FormDropConstraint(FormDropConstraintDefinition definition) {
        super(definition);
    }

    public boolean isAllowedAt(BaseModel source, BaseModel targetItem, DropLocation location) {
            switch(location) {
            case ON_TOP:
                return this.allowedAsChild(source, targetItem);
            case BELOW:
            case ABOVE:
                return this.allowedAsSibling(source, targetItem);
            default:
                return false;
            }

    }

    protected boolean allowedAsChild(BaseModel source, BaseModel target) {
        if ((source instanceof Form)) {
            return false;
        }
        if ((source instanceof Section) && !(target instanceof Form)) {
            return false;
        }
        if ((source instanceof Question) && !((target instanceof Section) || (target instanceof Form))) {
            return false;
        }
        if ((source instanceof AnswerOption) && !(target instanceof Question)) {
            return false;
        }
        return true;
    }

    protected boolean allowedAsSibling(BaseModel source, BaseModel target) {

        if ((source instanceof Form) && !(target instanceof Form)) {
            return false;
        }
        if ((source instanceof Section) && !(target instanceof Section)) {
            return false;
        }
        if ((source instanceof Question) && !(target instanceof Question)) {
            return false;
        }
        if ((source instanceof AnswerOption) && !(target instanceof AnswerOption)) {
            return false;
        }

        return true;
    }
}
