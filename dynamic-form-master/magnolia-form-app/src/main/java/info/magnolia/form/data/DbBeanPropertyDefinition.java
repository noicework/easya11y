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
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.jdbc.data.JdbcPropertyDefinition;

import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import com.vaadin.data.ValueProvider;
import com.vaadin.server.Setter;

import jodd.bean.BeanUtil;

/**
 * Property definition operating over {@link Object}.
 *
 * @param <T> entity type
 * @param <V> value type
 */
public class DbBeanPropertyDefinition<T extends BaseModel, V> extends JdbcPropertyDefinition<T,V> implements DbBeanOperator<V> {

    @Inject
    public DbBeanPropertyDefinition(Class<V> valueType, String name, I18NAuthoringSupport<Object> localisationSupport) {
        super(valueType, name, localisationSupport);
    }

    @Inject
    public DbBeanPropertyDefinition(Class<V> valueType, String name, boolean i18n, Locale locale, I18NAuthoringSupport<Object> localisationSupport) {
        super(valueType, name, i18n, locale, localisationSupport);
    }

    @Override
    public ValueProvider<T, V> getGetter() {
        return this::read;
    }

    @Override
    public Optional<Setter<T, V>> getSetter() {
        return Optional.of(this::write);
    }

    private V read(Object bean) {
        if (this.isI18n() &&
                this.getLocale() != null &&
                !this.getLocalisationSupport().isDefaultLocale(this.getLocale(), bean) &&
                (!this.getName().contains(".") || this.getName().startsWith("content.")) &&
                BeanUtil.pojo.hasProperty(bean, "localizations")
        ) {
            return BeanUtil.silent.getProperty(bean, this.localizedName());
        }

        return BeanUtil.silent.getProperty(bean, this.getName());
    }

    private void write(Object bean, V value) {
        if (this.isI18n() &&
                this.getLocale() != null &&
                !this.getLocalisationSupport().isDefaultLocale(this.getLocale(), bean) &&
                (!this.getName().contains(".") || this.getName().startsWith("content.")) &&
                BeanUtil.pojo.hasProperty(bean, "localizations")
        ) {
            BeanUtil.declaredForcedSilent.setProperty(bean, this.localizedName(), value);
            return;
        }

        BeanUtil.declaredForcedSilent.setProperty(bean, this.getName(), value);
    }

    private String localizedName() {
        return String.format("localizations[%s].%s", this.getLocale().getLanguage(), this.getName());
    }
}
