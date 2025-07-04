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

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.observation.DatasourceObservation;

import javax.inject.Inject;

/**
 * Refresh database entities action.
 */
public class RefreshDatabaseAction extends AbstractAction {

    private DatasourceObservation.Manual datasourceObservation;

    @Inject
    public RefreshDatabaseAction(RefreshDatabaseActionDefinition definition,
                                 DatasourceObservation.Manual datasourceObservation) {

        super(definition);
        this.datasourceObservation = datasourceObservation;
    }

    @Override
    public void execute() throws ActionExecutionException {

        datasourceObservation.trigger();
    }
}
