package work.noice.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import work.noice.easya11y.setup.MigrateScheduledScanJobPropertiesTask;
import work.noice.easya11y.setup.RegisterScheduledScanJobTask;
import work.noice.easya11y.setup.RegisterServerSideScanCommandTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for the easya11y module.
 * Registers commands and scheduled jobs for server-side scanning.
 */
public class easya11yVersionHandler extends DefaultModuleVersionHandler {
    
    public easya11yVersionHandler() {
        register(DeltaBuilder.update("1.1.0", "Added server-side scanning capabilities")
            .addTask(new RegisterServerSideScanCommandTask())
            .addTask(new RegisterScheduledScanJobTask())
        );
        
        register(DeltaBuilder.update("1.2.1", "Added database storage, historical analytics, and licensing")
        );

        register(DeltaBuilder.update("1.5.1", "Removed invalid scheduler job properties")
            .addTask(new MigrateScheduledScanJobPropertiesTask())
        );

        register(DeltaBuilder.update("1.5.2", "Corrected scheduled scan command reference")
            .addTask(new MigrateScheduledScanJobPropertiesTask())
        );
    }
    
    @Override
    protected List<info.magnolia.module.delta.Task> getExtraInstallTasks(InstallContext installContext) {
        List<info.magnolia.module.delta.Task> tasks = new ArrayList<>();
        tasks.add(new RegisterServerSideScanCommandTask());
        tasks.add(new RegisterScheduledScanJobTask());
        return tasks;
    }
}
