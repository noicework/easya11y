package work.noice.easya11y.actions;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.dialog.DialogBuilder;
import info.magnolia.context.MgnlContext;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import info.magnolia.jcr.util.PropertyUtil;
import javax.jcr.Session;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.VerticalLayout;

/**
 * Action to run accessibility check on a page.
 */
public class RunAccessibilityCheckAction extends AbstractAction<ConfiguredActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(RunAccessibilityCheckAction.class);
    
    private final ValueContext<Node> valueContext;
    private final AppContext appContext;
    private final DialogBuilder dialogBuilder;
    
    @Inject
    public RunAccessibilityCheckAction(ConfiguredActionDefinition definition, 
                                      ValueContext<Node> valueContext,
                                      AppContext appContext,
                                      DialogBuilder dialogBuilder) {
        super(definition);
        this.valueContext = valueContext;
        this.appContext = appContext;
        this.dialogBuilder = dialogBuilder;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            // Get selected items
            Optional<Node> selectedNode = valueContext.getSingle();
            
            if (!selectedNode.isPresent()) {
                showError("No page selected. Please select a page to scan.");
                return;
            }
            
            Node pageNode = selectedNode.get();
            String pagePath = pageNode.getPath();
            String pageTitle = pageNode.hasProperty("title") ? 
                pageNode.getProperty("title").getString() : pageNode.getName();
            
            // Log workspace info for debugging
            String workspace = pageNode.getSession().getWorkspace().getName();
            log.info("Page node workspace: {}, path: {}", workspace, pagePath);
            
            // Initiate scan
            log.info("Initiating accessibility scan for page: {}", pagePath);
            
            // Call the REST endpoint to initiate scan
            Client client = ClientBuilder.newClient();
            String baseUrl = MgnlContext.getWebContext().getRequest().getScheme() + "://" + 
                           MgnlContext.getWebContext().getRequest().getServerName() + ":" +
                           MgnlContext.getWebContext().getRequest().getServerPort() +
                           MgnlContext.getContextPath();
            
            // Get WCAG configuration from saved settings
            String wcagVersion = getWcagVersion();
            String wcagLevel = getWcagLevel();
            
            Map<String, String> request = new HashMap<>();
            request.put("pagePath", pagePath);
            request.put("wcagLevel", wcagLevel);
            
            Response response = client.target(baseUrl + "/.rest/easya11y/scan/initiate")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request));
            
            if (response.getStatus() == 200) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = response.readEntity(Map.class);
                String pageUrl = (String) result.get("pageUrl");
                String scanId = (String) result.get("scanId");
                
                // Create the scan viewer URL for the dialog view
                String scanViewerUrl = baseUrl + "/.resources/easya11y/webresources/accessibility-scan-dialog.html" +
                    "?pageUrl=" + URLEncoder.encode(pageUrl, StandardCharsets.UTF_8.toString()) +
                    "&scanId=" + scanId +
                    "&pagePath=" + URLEncoder.encode(pagePath, StandardCharsets.UTF_8.toString()) +
                    "&pageTitle=" + URLEncoder.encode(pageTitle, StandardCharsets.UTF_8.toString()) +
                    "&wcagLevel=" + URLEncoder.encode(wcagLevel, StandardCharsets.UTF_8.toString()) +
                    "&wcagVersion=" + URLEncoder.encode(wcagVersion, StandardCharsets.UTF_8.toString());
                
                // Create dialog with iframe
                openScannerDialog(scanViewerUrl, pageTitle);
                
                log.info("Accessibility scan dialog opened. Scan ID: {}", scanId);
                
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> error = response.readEntity(Map.class);
                String errorMessage = (String) error.get("message");
                showError("Failed to initiate scan: " + errorMessage);
            }
            
        } catch (RepositoryException e) {
            log.error("Error executing accessibility check action", e);
            throw new ActionExecutionException("Error accessing page: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during accessibility check", e);
            throw new ActionExecutionException("Failed to run accessibility check: " + e.getMessage(), e);
        }
    }
    
    private void openScannerDialog(String scannerUrl, String pageTitle) {
        // Create the iframe component
        BrowserFrame scannerFrame = new BrowserFrame();
        scannerFrame.setSource(new ExternalResource(scannerUrl));
        scannerFrame.setWidth("100%");
        scannerFrame.setHeight("700px");
        
        // Create layout
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(false);
        layout.addComponent(scannerFrame);
        
        // For Magnolia 6.3, use the dialog builder without expecting a return value
        // The dialog will be managed by the framework
        dialogBuilder
            .withTitle("Accessibility Scan: " + pageTitle)
            .withContent(layout)
            .buildAndOpen();
    }
    
    private void showError(String errorText) {
        Message message = new Message(MessageType.ERROR, "Accessibility Check Error", errorText);
        appContext.sendLocalMessage(message);
    }
    
    /**
     * Get WCAG version from configuration or return default.
     */
    private String getWcagVersion() {
        try {
            Session session = MgnlContext.getJCRSession("easya11y");
            if (session.nodeExists("/configuration")) {
                Node configNode = session.getNode("/configuration");
                return PropertyUtil.getString(configNode, "wcagVersion", "2.2");
            }
        } catch (RepositoryException e) {
            log.warn("Could not read WCAG version from configuration", e);
        }
        return "2.2"; // Default to WCAG 2.2
    }
    
    /**
     * Get WCAG level from configuration or return default.
     */
    private String getWcagLevel() {
        try {
            Session session = MgnlContext.getJCRSession("easya11y");
            if (session.nodeExists("/configuration")) {
                Node configNode = session.getNode("/configuration");
                return PropertyUtil.getString(configNode, "wcagLevel", "AA");
            }
        } catch (RepositoryException e) {
            log.warn("Could not read WCAG level from configuration", e);
        }
        return "AA"; // Default to AA
    }
}