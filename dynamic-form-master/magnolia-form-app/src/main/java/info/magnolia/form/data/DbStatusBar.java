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
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.ItemDescriber;
import info.magnolia.ui.jdbc.contentapp.browser.JdbcStatusBar;

import javax.inject.Inject;

/**
 * Status bar for {@link BaseModel}.
 */
public class DbStatusBar extends JdbcStatusBar<BaseModel> {

    @Inject
    public DbStatusBar(ValueContext<BaseModel> valueContext, ItemDescriber<BaseModel> itemDescriber) {
        super(valueContext, itemDescriber);
    }
}
