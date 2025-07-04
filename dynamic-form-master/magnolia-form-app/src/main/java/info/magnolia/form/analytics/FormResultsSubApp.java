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
package info.magnolia.form.analytics;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.contentapp.ContentBrowserSubApp;
import info.magnolia.ui.framework.app.BaseSubApp;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * Form results chart view sub app.
 */
public class FormResultsSubApp extends BaseSubApp<FormResultsAnalyticsView> {

    private final SimpleTranslator i18n;
    private Optional<BaseModel> rootItem;
    private final DbItemResolver itemResolver;

    @Inject
    public FormResultsSubApp(SubAppContext subAppContext,
                             FormResultsAnalyticsView view,
                             SimpleTranslator i18n,
                             DbItemResolver itemResolver) {
        super(subAppContext, view);
        this.i18n = i18n;
        this.itemResolver = itemResolver;
    }

    @Override
    public FormResultsAnalyticsView start(final Location location) {
        setRootItem(location);
        FormResultsAnalyticsView view =  super.start(location);
        this.rootItem.ifPresent(view::initCharts);
        return view;
    }

    @Override
    public void locationChanged(Location location) {
        setRootItem(location);
        super.locationChanged(location);
        this.rootItem.ifPresent(i -> getView().initCharts(i));
    }

    private void setRootItem(final Location location) {
        ContentBrowserSubApp.BrowserLocation browserLocation = ContentBrowserSubApp.BrowserLocation.wrap(location);
        if (StringUtils.isNotBlank(browserLocation.getNodePath())) {
            rootItem =  itemResolver.getItemById(browserLocation.getNodePath());
        }
    }

    public Optional<BaseModel> getRootItem() {
        return rootItem;
    }

    @Override
    public String getCaption() {
        if (rootItem.isPresent()) {
            BaseModel item = rootItem.get();
            if (item instanceof Form) {
                return  ((Form) item).getTitle();
            }
        }
        return "Form results";
    }
}

