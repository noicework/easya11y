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
package info.magnolia.form.action;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

/**
 * Action definition for {@link ExportDatabaseAction}
 */
@ActionType("exportFormDbAction")
public class ExportDatabaseActionDefinition extends ConfiguredActionDefinition {
    public ExportDatabaseActionDefinition() {
        this.setImplementationClass(ExportDatabaseAction.class);
    }
}
