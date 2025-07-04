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
import info.magnolia.ui.contentapp.browser.preview.PreviewProvider;

import java.util.Optional;

import javax.inject.Inject;

import com.vaadin.server.Resource;

/**
 * This implementation of {@link PreviewProvider} provides no preview.
 */
public class NoPreviewProvider implements PreviewProvider<BaseModel> {

    @Inject
    public NoPreviewProvider(DbDatasourceDefinition definition) {
    }

    @Override
    public Optional<Resource> getResource(BaseModel item) {
        return Optional.empty();
    }
}
