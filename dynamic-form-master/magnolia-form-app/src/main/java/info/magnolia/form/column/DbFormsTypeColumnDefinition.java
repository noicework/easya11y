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

import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Question;
import info.magnolia.ui.contentapp.configuration.column.icon.IconAndValueColumnDefinition;

import org.apache.commons.lang3.StringUtils;

/**
 * Custom column that shows different icon for each of the form related entities.
 */
public class DbFormsTypeColumnDefinition extends IconAndValueColumnDefinition {
    public DbFormsTypeColumnDefinition() {
        this.setValueProvider(DbFormsTypeColumnDefinition.ValueProvider.class);
    }

    /**
     * Icon and Value provider.
     */
    public static class ValueProvider implements com.vaadin.data.ValueProvider<BaseModel, String> {

        protected String getIcon(BaseModel item) {
            return StringUtils.EMPTY;
        }

        @Override
        public String apply(BaseModel item) {

            String value = StringUtils.EMPTY;

            if (item instanceof Question) {
               value = ((Question) item).getQuestionType();
               value = StringUtils.capitalize(value);
            } else {
                value = StringUtils.EMPTY;
            }

            return "<span class=\"v-table-icon-element " + getIcon(item) + "\" ></span>" +
                   "<span>" + value + "</span>";
        }
    }
}
