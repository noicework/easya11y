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

import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.contentapp.ContentBrowserSubApp;
import info.magnolia.ui.contentapp.configuration.BrowserDescriptor;

import javax.inject.Inject;

/**
 * App class for the forms management. Open several application
 * simultaneously.
 *
 * @author Magnolia International Ltd.
 */
public class FormBrowserSubApp extends ContentBrowserSubApp {

    private final ValueContext<BaseModel> valueContext;

    @Inject
    public FormBrowserSubApp(SubAppContext subAppContext,
                             BrowserDescriptor browserDescriptor,
                             ValueContext<BaseModel> valueContext) {
        super(subAppContext, browserDescriptor);
        this.valueContext = valueContext;
    }

    @Override
    public RootView start(final Location location) {
        return super.start(location);
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
    }
}
