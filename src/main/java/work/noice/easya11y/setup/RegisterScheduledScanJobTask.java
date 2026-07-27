package work.noice.easya11y.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Map;

/**
 * Task to register the scheduled accessibility scan job in Magnolia's scheduler configuration.
 */
public class RegisterScheduledScanJobTask extends AbstractRepositoryTask {

    static final String JOB_PATH = "/modules/scheduler/config/jobs/accessibilityScan";

    static final Map<String, Object> JOB_PROPERTIES = Map.of(
        "name", "Accessibility Scan",
        "description", "Automated accessibility scan for all pages",
        "catalog", "default",
        "command", "easya11y-serverSideScan",
        "cron", "0 0 9 ? * MON",
        "enabled", false
    );

    static final Map<String, Object> PARAM_PROPERTIES = Map.of(
        "pagePattern", "/",
        "wcagLevel", "AA",
        "maxPages", "50",
        "sendEmail", "true",
        "sendDigest", "true"
    );

    public RegisterScheduledScanJobTask() {
        super("Register scheduled scan job",
              "Registers the accessibility scan job for scheduled execution");
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        Session configSession = ctx.getConfigJCRSession();

        // Create jobs node if it doesn't exist
        Node schedulerConfig = configSession.getNode("/modules/scheduler/config");
        Node jobsNode;

        if (!schedulerConfig.hasNode("jobs")) {
            jobsNode = schedulerConfig.addNode("jobs", NodeTypes.ContentNode.NAME);
        } else {
            jobsNode = schedulerConfig.getNode("jobs");
        }

        // Create the scheduled scan job configuration
        Node jobNode;
        if (jobsNode.hasNode("accessibilityScan")) {
            jobNode = jobsNode.getNode("accessibilityScan");
        } else {
            jobNode = jobsNode.addNode("accessibilityScan", NodeTypes.ContentNode.NAME);
        }

        // Set job properties
        for (Map.Entry<String, Object> property : JOB_PROPERTIES.entrySet()) {
            PropertyUtil.setProperty(jobNode, property.getKey(), property.getValue());
        }

        // Create params node for job parameters
        Node paramsNode;
        if (jobNode.hasNode("params")) {
            paramsNode = jobNode.getNode("params");
        } else {
            paramsNode = jobNode.addNode("params", NodeTypes.ContentNode.NAME);
        }

        // Set default job parameters
        for (Map.Entry<String, Object> property : PARAM_PROPERTIES.entrySet()) {
            PropertyUtil.setProperty(paramsNode, property.getKey(), property.getValue());
        }

        ctx.info("Registered scheduled accessibility scan job");
    }
}
