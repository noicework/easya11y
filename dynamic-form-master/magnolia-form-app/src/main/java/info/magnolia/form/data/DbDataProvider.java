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
import info.magnolia.ui.jdbc.data.JdbcDataProvider;
import info.magnolia.ui.jdbc.service.DatabaseService;
import info.magnolia.ui.jdbc.service.DatabaseServiceFactory;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Data provider working with database services.
 * @param <T> Entity Type.
 */
public class DbDataProvider<T extends BaseModel> extends JdbcDataProvider<T, UUID> {

    @Inject
    public DbDataProvider(DbDatasourceDefinition sourceDefinition, DatabaseServiceFactory serviceFactory) {
        super(sourceDefinition, serviceFactory);
    }

    @Override
    public DatabaseService<T, UUID> getService() {
        throw new UnsupportedOperationException("Use DbItemResolver class to get the service!");
    }
}
