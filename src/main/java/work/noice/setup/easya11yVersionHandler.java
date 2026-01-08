package work.noice.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
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
        
        register(DeltaBuilder.update("1.2.1", "Added database storage, historical analytics, licensing, and JIRA integration")
            .addTask(new InitializeJiraConfigurationTask())
        );
    }
    
    @Override
    protected List<info.magnolia.module.delta.Task> getExtraInstallTasks(InstallContext installContext) {
        List<info.magnolia.module.delta.Task> tasks = new ArrayList<>();
        tasks.add(new RegisterCommandTask());
        tasks.add(new RegisterScheduledScanJobTask());
        tasks.add(new InitializeJiraConfigurationTask());
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
            
            // Create parent nodes if needed
            javax.jcr.Node root = session.getRootNode();
            javax.jcr.Node modules = root.hasNode("modules") ? 
                root.getNode("modules") : root.addNode("modules");
            javax.jcr.Node moduleNode = modules.hasNode("easya11y") ? 
                modules.getNode("easya11y") : modules.addNode("easya11y");
            javax.jcr.Node commands = moduleNode.hasNode("commands") ? 
                moduleNode.getNode("commands") : moduleNode.addNode("commands");
            javax.jcr.Node defaultCatalog = commands.hasNode("default") ? 
                commands.getNode("default") : commands.addNode("default");
            
            // Create command node
            javax.jcr.Node commandNode = defaultCatalog.hasNode("serverSideScan") ?
                defaultCatalog.getNode("serverSideScan") : 
                defaultCatalog.addNode("serverSideScan");
            
            // Set command properties
            commandNode.setProperty("class", "work.noice.easya11y.commands.ServerSideScanCommand");
            commandNode.setProperty("enabled", true);
            
            ctx.info("Registered server-side scan command");
        }
    }
    
    /**
     * Task to initialize JIRA configuration in the easya11y workspace.
     */
    private static class InitializeJiraConfigurationTask extends info.magnolia.module.delta.AbstractRepositoryTask {
        public InitializeJiraConfigurationTask() {
            super("Initialize JIRA configuration", 
                  "Creates JIRA configuration entries in the easya11y workspace");
        }
        
        @Override
        protected void doExecute(InstallContext ctx) throws javax.jcr.RepositoryException, info.magnolia.module.delta.TaskExecutionException {
            // Note: The actual configuration storage is handled by the StorageService
            // This task ensures the workspace exists and the application is aware of the need for configuration
            javax.jcr.Session session = ctx.getConfigJCRSession();
            
            // Create parent nodes if needed
            javax.jcr.Node root = session.getRootNode();
            javax.jcr.Node modules = root.hasNode("modules") ? 
                root.getNode("modules") : root.addNode("modules");
            javax.jcr.Node moduleNode = modules.hasNode("easya11y") ? 
                modules.getNode("easya11y") : modules.addNode("easya11y");
            
            // Note: The actual JIRA configuration values (jiraUrl, jiraApiToken, jiraEmail)
            // should be set through the Configuration UI or REST endpoint, not here
            ctx.info("JIRA configuration initialization completed. Configure values via the Configuration UI.");
        }
    }
}
