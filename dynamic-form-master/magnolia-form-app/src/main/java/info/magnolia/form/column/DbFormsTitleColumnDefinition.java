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
package info.magnolia.form.column;

import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Section;
import info.magnolia.ui.contentapp.configuration.column.icon.IconAndValueColumnDefinition;

import org.apache.commons.lang3.StringUtils;

/**
 * Custom column that shows different icon for each of the form related entities.
 */
public class DbFormsTitleColumnDefinition extends IconAndValueColumnDefinition {
    public DbFormsTitleColumnDefinition() {
        this.setValueProvider(DbFormsTitleColumnDefinition.ValueProvider.class);
    }

    /**
     * Icon and Value provider.
     */
    public static class ValueProvider implements com.vaadin.data.ValueProvider<BaseModel, String> {

        protected String getIcon(BaseModel item) {

            if (item instanceof Form) {
                return MagnoliaIcons.WORK_ITEM.getCssClass();
            } else if (item instanceof Section) {
                return MagnoliaIcons.VIEW_LIST.getCssClass();
            } else if (item instanceof Question) {
                return MagnoliaIcons.HELP.getCssClass();
            } else if (item instanceof AnswerOption) {
                return MagnoliaIcons.TICK.getCssClass();
            }

            return MagnoliaIcons.NODE_CONTENT.getCssClass();
        }

        @Override
        public String apply(BaseModel item) {

            String value = StringUtils.EMPTY;

            if (item instanceof Form) {
               value = ((Form) item).getTitle();
            } else if (item instanceof Section) {
                value = ((Section) item).getTitle();
            } else if (item instanceof Question) {
                value = ((Question) item).getTitle();
            } if (item instanceof AnswerOption) {
                value = ((AnswerOption) item).getTitle();
            }

            return "<span class=\"v-table-icon-element " + getIcon(item) + "\" ></span>" +
                   "<span>" + value + "</span>";
        }
    }
}
