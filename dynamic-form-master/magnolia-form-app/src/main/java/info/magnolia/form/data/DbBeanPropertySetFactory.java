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

import info.magnolia.objectfactory.Components;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.field.FieldDefinition;
import info.magnolia.ui.jdbc.data.JdbcPropertySetFactory;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.data.PropertySet;

/**
 * Factory for {@link DbBeanPropertySet}.
 * @param <T> Entity Type.
 */
public class DbBeanPropertySetFactory<T extends BaseModel> extends JdbcPropertySetFactory<T> {

    @Override
    public PropertySet<T> withProperties(Map<String, Class> properties) {
        return new DbBeanPropertySet(properties);
    }

    @Override
    public PropertySet<T> fromFieldDefinitions(Collection<FieldDefinition> fieldDefinitions, Locale locale) {
        I18NAuthoringSupport localisationSupport = Components.getComponent(I18NAuthoringSupport.class);
        return new DbBeanPropertySet(fieldDefinitions.stream()
                .map(e -> new DbBeanPropertyDefinition(
                        e.getType(),
                        e.getName(),
                        e.isI18n(),
                        locale,
                        localisationSupport)
                )
                .collect(Collectors.toList()), locale);
    }
}
