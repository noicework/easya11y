/**
 * This file Copyright (c) 2010-2018 Magnolia International
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
package info.magnolia.form.provider;

import info.magnolia.context.MgnlContext;

import io.ebean.config.CurrentUserProvider;

/**
 * Returns current user name to ebean server to be used for WhoCreated/WhoModified columns.
 */
public class MgnlCurrentUserProvider implements CurrentUserProvider {

    public static final String ANONYMOUS = "anonymous";
    public static final String ANONYMIZE = "anonymize";

    @Override
    public Object currentUser() {

        String user = "no user";

        if (MgnlContext.hasInstance()) {
            String anonymize = MgnlContext.getAttribute(ANONYMIZE);
            if (Boolean.TRUE.toString().equals(anonymize)) {
                user = ANONYMOUS;
            } else {
                user = MgnlContext.getUser().getName();
            }
        }

        return user;
    }
}
