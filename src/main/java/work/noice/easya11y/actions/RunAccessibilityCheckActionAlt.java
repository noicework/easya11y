package work.noice.easya11y.actions;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
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

/**
 * Alternative action to run accessibility check on a page.
 * This version uses direct Node injection like ViewFormSubmissionsAction.
 */
public class RunAccessibilityCheckActionAlt extends AbstractAction<ConfiguredActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(RunAccessibilityCheckActionAlt.class);
    
    private final Node node;
    private final AppContext appContext;

    @Inject
    public RunAccessibilityCheckActionAlt(ConfiguredActionDefinition definition, 
                                         Node node,
                                         AppContext appContext) {
        super(definition);
        this.node = node;
        this.appContext = appContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            // Get page information
            String pagePath = node.getPath();
            String pageTitle = node.hasProperty("title") ? 
                node.getProperty("title").getString() : node.getName();
            
            // Initiate scan
            log.info("Initiating accessibility scan for page: {}", pagePath);
            
            // Call the REST endpoint to initiate scan
            Client client = ClientBuilder.newClient();
            String baseUrl = MgnlContext.getWebContext().getRequest().getScheme() + "://" + 
                           MgnlContext.getWebContext().getRequest().getServerName() + ":" +
                           MgnlContext.getWebContext().getRequest().getServerPort() +
                           MgnlContext.getContextPath();
            
            Map<String, String> request = new HashMap<>();
            request.put("pagePath", pagePath);
            request.put("wcagLevel", "AA"); // Default to AA
            
            Response response = client.target(baseUrl + "/.rest/easya11y/scan/initiate")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request));
            
            if (response.getStatus() == 200) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = response.readEntity(Map.class);
                String pageUrl = (String) result.get("pageUrl");
                String scanId = (String) result.get("scanId");
                
                // Create the scan viewer URL
                String scanViewerUrl = baseUrl + "/.magnolia/admincentral#app:easya11y:scanViewer;/" + 
                    "scan?pageUrl=" + URLEncoder.encode(pageUrl, StandardCharsets.UTF_8.toString()) +
                    "&scanId=" + scanId +
                    "&pagePath=" + URLEncoder.encode(pagePath, StandardCharsets.UTF_8.toString()) +
                    "&pageTitle=" + URLEncoder.encode(pageTitle, StandardCharsets.UTF_8.toString());
                
                // Show success message with link
                String messageText = String.format("Accessibility scan initiated for '%s'. <a href=\"%s\" target=\"_blank\">Click here to run the scan</a>", 
                    pageTitle, scanViewerUrl);
                Message message = new Message(MessageType.INFO, "Accessibility Check", messageText);
                appContext.sendLocalMessage(message);
                
                log.info("Accessibility scan initiated successfully. Scan ID: {}", scanId);
                
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
    
    private void showError(String errorText) {
        Message message = new Message(MessageType.ERROR, "Accessibility Check Error", errorText);
        appContext.sendLocalMessage(message);
    }
}