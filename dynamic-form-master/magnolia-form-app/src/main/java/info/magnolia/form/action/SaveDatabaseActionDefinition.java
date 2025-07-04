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

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;

/**
 * Simple implementation of {@link SaveDatabaseAction}.
 */
@ActionType("saveFormDatabaseAction")
public class SaveDatabaseActionDefinition extends CommitActionDefinition {

    public SaveDatabaseActionDefinition() {
        setImplementationClass(SaveDatabaseAction.class);
        setName("commit");

    }

    @Override
    public int getShortcut() {
        // ENTER
        return 13;
    }

}
