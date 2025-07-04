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
package info.magnolia.form.field.converter;

import info.magnolia.i18nsystem.SimpleTranslator;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

/**
 * Long to String converter. Handles empty string as null.
 */
public class StringToLongConverter implements Converter<String, Long> {

    private final SimpleTranslator i18n;

    @Inject
    public StringToLongConverter(SimpleTranslator i18n) {
        this.i18n = i18n;
    }

    @Override
    public Result<Long> convertToModel(String value, ValueContext context) {

        if(StringUtils.isBlank(value)) {
            return Result.ok(null);
        }

        try {
            return Result.ok(Long.parseLong(value));
        } catch (Exception e) {
            return Result.error(i18n.translate("converter.long.error.message"));
        }
    }

    @Override
    public String convertToPresentation(Long value, ValueContext context) {

        if(value == null) {
            return StringUtils.EMPTY;
        }

        return String.valueOf(value);
    }
}
