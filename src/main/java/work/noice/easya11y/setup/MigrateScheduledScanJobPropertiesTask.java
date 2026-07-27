package work.noice.easya11y.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Migrates scheduler properties written by EasyA11y versions before 1.5.2.
 */
public class MigrateScheduledScanJobPropertiesTask extends AbstractRepositoryTask {

    static final String LEGACY_CATALOG_PROPERTY = "catalogName";
    static final String LEGACY_JOB_NAME_PROPERTY = "jobName";
    static final String LEGACY_ACTIVE_PROPERTY = "active";
    static final String LEGACY_COMMAND_VALUE = "easya11y-serverSideScan";

    public MigrateScheduledScanJobPropertiesTask() {
        super("Migrate scheduled scan job properties",
            "Replaces invalid and deprecated scheduler properties on the accessibility scan job");
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        Session configSession = ctx.getConfigJCRSession();
        if (!configSession.nodeExists(RegisterScheduledScanJobTask.JOB_PATH)) {
            ctx.info("No existing scheduled accessibility scan job to migrate");
            return;
        }

        Node jobNode = configSession.getNode(RegisterScheduledScanJobTask.JOB_PATH);

        if (!jobNode.hasProperty("catalog")) {
            String catalog = jobNode.hasProperty(LEGACY_CATALOG_PROPERTY)
                ? jobNode.getProperty(LEGACY_CATALOG_PROPERTY).getString()
                : RegisterServerSideScanCommandTask.CATALOG_NAME;
            jobNode.setProperty("catalog", catalog);
        }

        if (!jobNode.hasProperty("enabled") && jobNode.hasProperty(LEGACY_ACTIVE_PROPERTY)) {
            jobNode.setProperty("enabled", jobNode.getProperty(LEGACY_ACTIVE_PROPERTY).getBoolean());
        }

        if (jobNode.hasProperty("command")
            && LEGACY_COMMAND_VALUE.equals(jobNode.getProperty("command").getString())) {
            jobNode.setProperty("command", RegisterServerSideScanCommandTask.COMMAND_NAME);
        }

        removeProperty(jobNode, LEGACY_CATALOG_PROPERTY);
        removeProperty(jobNode, LEGACY_JOB_NAME_PROPERTY);
        removeProperty(jobNode, LEGACY_ACTIVE_PROPERTY);

        ctx.info("Migrated scheduled accessibility scan job properties");
    }

    private void removeProperty(Node node, String propertyName) throws RepositoryException {
        if (node.hasProperty(propertyName)) {
            Property property = node.getProperty(propertyName);
            property.remove();
        }
    }
}
