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

import info.magnolia.form.domain.Form;
import info.magnolia.form.service.FormService;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

/**
 * From to String converter. Handles empty string as null.
 */
public class FormToStringConverter implements Converter<Form, String> {
    private final FormService formService;

    @Inject
    public FormToStringConverter(FormService formService) {
        this.formService = formService;
    }

    @Override
    public Result<String> convertToModel(Form value, ValueContext context) {
        if(value == null) {
            return Result.ok(StringUtils.EMPTY);
        }

        return Result.ok(value.getId().toString());
    }

    @Override
    public Form convertToPresentation(String value, ValueContext context) {
        if(StringUtils.isBlank(value)) {
            return null;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(value.trim());
        } catch (java.lang.IllegalArgumentException e) {
            return null;
        }

        try {
            return formService.getById(uuid).get();
        } catch (Exception e) {
            return null;
        }
    }
}
