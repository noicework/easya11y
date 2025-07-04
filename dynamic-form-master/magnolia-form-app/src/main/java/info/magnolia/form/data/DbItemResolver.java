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
import info.magnolia.form.utils.DbUtil;
import info.magnolia.ui.jdbc.data.JdbcItemResolver;
import info.magnolia.ui.jdbc.service.DatabaseService;
import info.magnolia.ui.jdbc.service.DatabaseServiceFactory;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item resolver for {@link BaseModel}.
 */
public class DbItemResolver extends JdbcItemResolver<BaseModel, UUID> {

    private static final Logger log = LoggerFactory.getLogger(DbItemResolver.class);
    private final DatabaseServiceFactory serviceFactory;

    @Inject
    public DbItemResolver(DbDataProvider<BaseModel> dataProvider,
                          DatabaseServiceFactory serviceFactory) {
        super(dataProvider);
        this.serviceFactory = serviceFactory;
    }

    @Override
    public String getId(BaseModel item) {
        return item.getClass().getSimpleName() + "|" + item.getId();
    }

    @Override
    public Optional<BaseModel> getItemById(String id) {

        DatabaseService service = getServiceByItemTypeId(id);

        if (service != null) {
            return service.getById(service.parseStringToId(id.split("[|]")[1]));
        }

        return Optional.empty();
    }

    public DatabaseService getServiceByItemTypeId(String id) {
        return DbUtil.getServiceByItemTypeId(id);
    }

    public DatabaseService getServiceByItemType(String type) {
        return DbUtil.getServiceByItemType(type);
    }
}
