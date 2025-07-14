package work.noice.easya11y.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Task to register the scheduled accessibility scan job in Magnolia's scheduler configuration.
 */
public class RegisterScheduledScanJobTask extends AbstractRepositoryTask {
    
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
        PropertyUtil.setProperty(jobNode, "active", false); // Disabled by default
        PropertyUtil.setProperty(jobNode, "name", "Accessibility Scan");
        PropertyUtil.setProperty(jobNode, "description", "Automated accessibility scan for all pages");
        PropertyUtil.setProperty(jobNode, "catalogName", "default");
        PropertyUtil.setProperty(jobNode, "jobName", "accessibilityScan");
        PropertyUtil.setProperty(jobNode, "command", "easya11y-serverSideScan");
        PropertyUtil.setProperty(jobNode, "cron", "0 0 9 ? * MON"); // Every Monday at 9 AM
        PropertyUtil.setProperty(jobNode, "enabled", false);
        
        // Create params node for job parameters
        Node paramsNode;
        if (jobNode.hasNode("params")) {
            paramsNode = jobNode.getNode("params");
        } else {
            paramsNode = jobNode.addNode("params", NodeTypes.ContentNode.NAME);
        }
        
        // Set default job parameters
        PropertyUtil.setProperty(paramsNode, "pagePattern", "/");
        PropertyUtil.setProperty(paramsNode, "wcagLevel", "AA");
        PropertyUtil.setProperty(paramsNode, "maxPages", "50");
        PropertyUtil.setProperty(paramsNode, "sendEmail", "true");
        PropertyUtil.setProperty(paramsNode, "sendDigest", "true");
        
        ctx.info("Registered scheduled accessibility scan job");
    }
}