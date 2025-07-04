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

import static java.util.stream.Collectors.toMap;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.availability.AvailabilityChecker;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.FilterableListDataProvider;
import info.magnolia.ui.contentapp.browser.ListPresenter;
import info.magnolia.ui.contentapp.configuration.GridViewDefinition;
import info.magnolia.ui.contentapp.configuration.column.ColumnDefinition;
import info.magnolia.ui.observation.DatasourceObservation;

import javax.inject.Inject;

import com.vaadin.data.PropertySet;

/**
 * List Presenter.
 */
public class DbListPresenter extends ListPresenter<BaseModel> {

    private final DbBeanPropertySetFactory propertySetFactory;
    private final DbDataProvider dataProvider;

    @Inject
    public DbListPresenter(GridViewDefinition<BaseModel> definition,
                           ComponentProvider componentProvider,
                           AvailabilityChecker<BaseModel> availabilityChecker,
                           DatasourceObservation datasourceObservation,
                           DbDataProvider dataProvider,
                           DbBeanPropertySetFactory propertySetFactory,
                           Datasource<BaseModel> datasource,
                           AppContext appContext) {

        super(definition, componentProvider, availabilityChecker, datasource, datasourceObservation);
        this.dataProvider = dataProvider;
        this.propertySetFactory = propertySetFactory;
    }

    @Override
    protected FilterableListDataProvider<BaseModel> createDataProvider() {

        return FilterableListDataProvider.wrap(dataProvider);
    }

    @Override
    protected PropertySet createPropertySet() {
        return propertySetFactory.withProperties(
                definition.getColumns()
                        .stream()
                        .collect(toMap(ColumnDefinition::getName, ColumnDefinition::getType))
        );
    }
}
