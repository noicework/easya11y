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
package info.magnolia.form.data;

import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.jdbc.data.JdbcPropertyDefinition;
import info.magnolia.ui.jdbc.data.JdbcPropertySet;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

/**
 * Property set operating over {@link Object}.
 * @param <T> Entity Type.
 */
public class DbBeanPropertySet<T extends BaseModel> extends JdbcPropertySet<T> {

    @Inject
    public DbBeanPropertySet(List<JdbcPropertyDefinition> beanPropertyDefinitions, Locale locale) {
        super(beanPropertyDefinitions, locale);
    }

    @Inject
    public DbBeanPropertySet(Map<String, Class<T>> properties) {
        super(properties);
    }
}
