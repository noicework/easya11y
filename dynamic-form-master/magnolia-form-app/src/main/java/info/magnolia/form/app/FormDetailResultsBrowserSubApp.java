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
package info.magnolia.form.app;

import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.contentapp.ContentBrowserSubApp;
import info.magnolia.ui.contentapp.configuration.BrowserDescriptor;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * App class for the listing pool results.
 *
 * @author Magnolia International Ltd.
 */
public class FormDetailResultsBrowserSubApp extends ContentBrowserSubApp {

    private Optional<BaseModel> rootItem;
    private final DbItemResolver itemResolver;

    @Inject
    public FormDetailResultsBrowserSubApp(SubAppContext subAppContext,
                                          BrowserDescriptor browserDescriptor,
                                          DbItemResolver itemResolver) {
        super(subAppContext, browserDescriptor);
        this.itemResolver = itemResolver;
    }

    @Override
    public RootView start(final Location location) {
        setRootItem(location);
        return super.start(location);
    }

    @Override
    public void locationChanged(Location location) {
        setRootItem(location);
        super.locationChanged(location);
    }

    private void setRootItem(final Location location) {
        BrowserLocation browserLocation = BrowserLocation.wrap(location);
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
        return "Form responses";
    }
}
