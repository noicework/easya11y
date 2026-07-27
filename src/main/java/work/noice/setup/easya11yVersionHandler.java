package work.noice.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import work.noice.easya11y.setup.MigrateScheduledScanJobPropertiesTask;
import work.noice.easya11y.setup.RegisterScheduledScanJobTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for the easya11y module.
 * Registers commands and scheduled jobs for server-side scanning.
 */
public class easya11yVersionHandler extends DefaultModuleVersionHandler {
    
    public easya11yVersionHandler() {
        register(DeltaBuilder.update("1.1.0", "Added server-side scanning capabilities")
            .addTask(new RegisterCommandTask())
            .addTask(new RegisterScheduledScanJobTask())
        );
        
        register(DeltaBuilder.update("1.2.1", "Added database storage, historical analytics, and licensing")
        );

        register(DeltaBuilder.update("1.5.1", "Removed invalid scheduler job properties")
            .addTask(new MigrateScheduledScanJobPropertiesTask())
        );
    }
    
    @Override
    protected List<info.magnolia.module.delta.Task> getExtraInstallTasks(InstallContext installContext) {
        List<info.magnolia.module.delta.Task> tasks = new ArrayList<>();
        tasks.add(new RegisterCommandTask());
        tasks.add(new RegisterScheduledScanJobTask());
        return tasks;
    }
    
    /**
     * Task to register the server-side scan command.
     */
    private static class RegisterCommandTask extends info.magnolia.module.delta.AbstractRepositoryTask {
        public RegisterCommandTask() {
            super("Register server-side scan command", 
                  "Registers the command for server-side accessibility scanning");
        }
        
        @Override
        protected void doExecute(InstallContext ctx) throws javax.jcr.RepositoryException, info.magnolia.module.delta.TaskExecutionException {
            javax.jcr.Session session = ctx.getConfigJCRSession();

            // Create parent nodes if needed using proper node types
            javax.jcr.Node root = session.getRootNode();
            javax.jcr.Node modules = root.hasNode("modules") ?
                root.getNode("modules") : root.addNode("modules", "mgnl:content");
            javax.jcr.Node moduleNode = modules.hasNode("easya11y") ?
                modules.getNode("easya11y") : modules.addNode("easya11y", "mgnl:content");
            javax.jcr.Node commands = moduleNode.hasNode("commands") ?
                moduleNode.getNode("commands") : moduleNode.addNode("commands", "mgnl:content");
            javax.jcr.Node defaultCatalog = commands.hasNode("default") ?
                commands.getNode("default") : commands.addNode("default", "mgnl:content");

            // Create command node
            javax.jcr.Node commandNode = defaultCatalog.hasNode("serverSideScan") ?
                defaultCatalog.getNode("serverSideScan") :
                defaultCatalog.addNode("serverSideScan", "mgnl:contentNode");

            // Set command properties
            commandNode.setProperty("class", "work.noice.easya11y.commands.ServerSideScanCommand");
            commandNode.setProperty("enabled", true);

            ctx.info("Registered server-side scan command");
        }
    }
    
}
