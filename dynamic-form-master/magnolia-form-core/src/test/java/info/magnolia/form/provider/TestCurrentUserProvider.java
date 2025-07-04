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

import io.ebean.config.CurrentUserProvider;

/**
 * Returns current user name to ebean server to be used for WhoCreated/WhoModified columns for test environment.
 */
public class TestCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Object currentUser() {
        return "test user";
    }
}
