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

import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.ResponseItem;
import info.magnolia.form.service.AnswerOptionService;
import info.magnolia.ui.contentapp.configuration.column.icon.IconAndValueColumnDefinition;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * Custom column that shows different icon for each of the form related entities.
 */
public class DbResultsAnswerColumnDefinition extends IconAndValueColumnDefinition {

    public DbResultsAnswerColumnDefinition() {
        this.setValueProvider(DbResultsAnswerColumnDefinition.ValueProvider.class);
    }

    /**
     * Icon and Value provider.
     */
    public class ValueProvider implements com.vaadin.data.ValueProvider<BaseModel, String> {

        private final AnswerOptionService answerOptionService;

        @Inject
        public ValueProvider(final AnswerOptionService answerOptionService) {
            this.answerOptionService = answerOptionService;
        }

        protected String getIcon(BaseModel item) {
            return StringUtils.EMPTY;
        }

        @Override
        public String apply(BaseModel item) {

            String value = StringUtils.EMPTY;

            if (item instanceof ResponseItem) {
                ResponseItem responseItem = ((ResponseItem) item);
                value = responseItem.getValue();

                Optional<AnswerOption> answerOption = answerOptionService.getAnswerOptionByQuestionIdAndValue(responseItem.getQuestion().getId(), value);
                if (answerOption.isPresent()) {
                    value = answerOption.get().getLabel();
                }
            }

            return "<span class=\"v-table-icon-element " + getIcon(item) + "\" ></span>" +
                   "<span>" + value + "</span>";
        }
    }
}
