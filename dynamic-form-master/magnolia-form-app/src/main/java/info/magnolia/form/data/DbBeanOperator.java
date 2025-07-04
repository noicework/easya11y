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

import info.magnolia.ui.api.i18n.I18NAuthoringSupport;

import java.util.Locale;

import jodd.bean.BeanUtil;

/**
 * Interface to work with Beans configuration.
 * @param <V>
 */
public interface DbBeanOperator<V> {

    default V read(Object bean, String propertyName, boolean i18n, Locale locale, I18NAuthoringSupport<Object> localisationSupport) {
        if (i18n) {
            if (locale != null) {
                if (!localisationSupport.isDefaultLocale(locale, bean)) {
                    if (!propertyName.contains(".")) {
                        if (BeanUtil.pojo.hasProperty(bean, "localizations")) {

                            String localizedName = "localizations[" + locale.getLanguage() + "]." + propertyName;

                            return BeanUtil.silent.getProperty(bean, localizedName);
                        }
                    }
                }
            }
        }

        return BeanUtil.silent.getProperty(bean, propertyName);
    }

    default void write(Object bean, String propertyName, V value, boolean i18n, Locale locale, I18NAuthoringSupport<Object> localisationSupport) {
        if (i18n) {
            if (locale != null) {
                if (!localisationSupport.isDefaultLocale(locale, bean)) {
                    if (!propertyName.contains(".")) {
                        if (BeanUtil.pojo.hasProperty(bean, "localizations")) {
                            String localizedName = "localizations[" + locale.getLanguage() + "]." + propertyName;

                            BeanUtil.declaredForcedSilent.setProperty(bean, localizedName, value);
                            return;
                        }
                    }
                }
            }
        }

        BeanUtil.declaredForcedSilent.setProperty(bean, propertyName, value);
    }
}
