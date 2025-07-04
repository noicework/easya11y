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
import info.magnolia.form.service.BaseService;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.jdbc.service.DatabaseService;
import info.magnolia.ui.observation.DatasourceObservation;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.shared.ui.grid.DropLocation;

/**
 * Jdbc (database) datasource.
 */
public class DbDatasource implements Datasource<BaseModel> {

    private final DbItemResolver itemResolver;
    private final DatasourceObservation.Manual datasourceObservation;

    public DbDatasource(final DbItemResolver itemResolver,
                        final DatasourceObservation.Manual datasourceObservation) {

        this.itemResolver = itemResolver;
        this.datasourceObservation = datasourceObservation;
    }

    @Override
    public void commit(BaseModel item) {

    }

    @Override
    public void moveItems(Collection<BaseModel> items, BaseModel target, DropLocation mgnlDropLocation) {

        if (items.size() > 0) {
            final info.magnolia.form.service.DropLocation dropLocation = Optional.of(mgnlDropLocation.name())
                    .flatMap(action -> Stream.of(info.magnolia.form.service.DropLocation.values()).filter(location -> StringUtils.equalsIgnoreCase(location.name(), action)).findFirst())
                    .orElse(info.magnolia.form.service.DropLocation.ON_TOP);

            for (BaseModel item : items) {
                DatabaseService service = itemResolver.getServiceByItemType(item.getClass().getSimpleName());
                ((BaseService) service).move(item, target, dropLocation);
            }
            this.datasourceObservation.trigger();
        }
    }

    @Override
    public void remove(BaseModel item) {

    }
}
