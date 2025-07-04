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

import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.contentapp.action.CloseActionDefinition;

import javax.inject.Inject;

/**
 * Cancels dialog action.
 * @param <D> definition.
 */
public class CancelDatabaseEditAction<D extends CloseActionDefinition> extends AbstractAction<D> {

    private final CloseHandler closeHandler;

    @Inject
    public CancelDatabaseEditAction(D definition, CloseHandler closeHandler) {
        super(definition);
        this.closeHandler = closeHandler;
    }

    @Override
    public void execute() throws ActionExecutionException {
        closeHandler.close();
    }

}
