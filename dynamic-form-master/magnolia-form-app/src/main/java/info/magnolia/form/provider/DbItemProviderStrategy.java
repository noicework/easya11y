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
package info.magnolia.form.provider;

import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.jdbc.data.JdbcDataProvider;
import info.magnolia.ui.jdbc.editor.JdbcItemProviderStrategy;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Strategy that replaces path parameter <code>{id}</code> with one passed in location and then fetches the result
 * using configured shop service.
 *
 */
public class DbItemProviderStrategy extends JdbcItemProviderStrategy<BaseModel, UUID> {

    @Inject
    public DbItemProviderStrategy(JdbcDataProvider<BaseModel, UUID> dataProvider) {
        super(dataProvider);
    }
}
