package work.noice.easya11y.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Registers the command used by the scheduled server-side accessibility scan.
 */
public class RegisterServerSideScanCommandTask extends AbstractRepositoryTask {

    static final String CATALOG_NAME = "default";
    static final String COMMAND_NAME = "serverSideScan";
    static final String COMMAND_CLASS =
        "work.noice.easya11y.commands.ServerSideScanCommand";

    public RegisterServerSideScanCommandTask() {
        super("Register server-side scan command",
            "Registers the command for server-side accessibility scanning");
    }

    @Override
    protected void doExecute(InstallContext ctx)
        throws RepositoryException, TaskExecutionException {
        Session session = ctx.getConfigJCRSession();

        Node root = session.getRootNode();
        Node modules = root.hasNode("modules")
            ? root.getNode("modules")
            : root.addNode("modules", "mgnl:content");
        Node moduleNode = modules.hasNode("easya11y")
            ? modules.getNode("easya11y")
            : modules.addNode("easya11y", "mgnl:content");
        Node commands = moduleNode.hasNode("commands")
            ? moduleNode.getNode("commands")
            : moduleNode.addNode("commands", "mgnl:content");
        Node catalog = commands.hasNode(CATALOG_NAME)
            ? commands.getNode(CATALOG_NAME)
            : commands.addNode(CATALOG_NAME, "mgnl:content");
        Node command = catalog.hasNode(COMMAND_NAME)
            ? catalog.getNode(COMMAND_NAME)
            : catalog.addNode(COMMAND_NAME, "mgnl:contentNode");

        command.setProperty("class", COMMAND_CLASS);
        command.setProperty("enabled", true);

        ctx.info("Registered server-side scan command");
    }
}
