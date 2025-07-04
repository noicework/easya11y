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

import info.magnolia.ui.dialog.actions.OpenDialogActionDefinition;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple implementation of {@link OpenNewDialogAction}.
 */
public class OpenNewDialogActionDbDefinition extends OpenDialogActionDefinition {

    @Getter @Setter
    private String entityName;

    public OpenNewDialogActionDbDefinition() {
        setImplementationClass(OpenNewDialogAction.class);
    }
}
