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
package info.magnolia.form.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CheckOrCreatePropertyTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is optional and lets you manage the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @see info.magnolia.module.ModuleVersionHandler
 * @see info.magnolia.module.delta.Task
 */
public class FormAppModuleVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {

        List<Task> basicInstallTasks = new ArrayList(super.getBasicInstallTasks(installContext));

        basicInstallTasks.add(new ArrayDelegateTask("Setup App Launcher",
                new CheckOrCreatePropertyTask("", "/modules/ui-admincentral/config/appLauncherLayout/hiddenApps", "forms-chooser", "forms-chooser")
        ));

        return basicInstallTasks;
    }
}
