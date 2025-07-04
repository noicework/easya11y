/**
 * This file Copyright (c) 2010-2018 Magnolia International
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

import info.magnolia.ui.api.action.ConfiguredActionDefinition;

/**
 * Definition of {@link CancelDatabaseEditAction}.
 */
public class CancelDatabaseEditActionDefinition extends ConfiguredActionDefinition {

    public CancelDatabaseEditActionDefinition() {
        setImplementationClass(CancelDatabaseEditAction.class);
    }
}
