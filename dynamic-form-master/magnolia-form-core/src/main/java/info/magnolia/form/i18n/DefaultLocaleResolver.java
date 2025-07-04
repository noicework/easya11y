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
package info.magnolia.form.i18n;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.Components;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of the {@link LocaleResolver}.
 *
 * @author Magnolia International Ltd.
 */
public class DefaultLocaleResolver implements LocaleResolver {

    private final String ADMIN_CENTRAL_PATH = "/.magnolia/admincentral/";

    @Override
    public String getCurrentLocaleStr() {

        // localize response only for non admincentral requests

        String requestUri = "";
        if ((MgnlContext.getInstance() != null) && (MgnlContext.getInstance() instanceof WebContext)) {
            requestUri = MgnlContext.getAggregationState().getOriginalURI();
        }

        if (StringUtils.isNotBlank(requestUri) && !requestUri.contains(ADMIN_CENTRAL_PATH)) {

            String lang = MgnlContext.getParameter("lang");
            if(StringUtils.isNotBlank(lang)) {
                return lang;
            }

            Locale currentLocale = Components.getComponent(I18nContentSupport.class).getLocale();
            if(currentLocale != null) {
                return currentLocale.toString();
            }
        }

        return null;
    }
}
