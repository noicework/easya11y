package work.noice.easya11y.endpoints;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint for managing easya11y global configuration.
 * Configuration is stored in the easya11y workspace under /configuration node.
 */
@Path("/easya11y/configuration")
public class ConfigurationEndpoint extends AbstractEndpoint<EndpointDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationEndpoint.class);
    private static final String easya11y_WORKSPACE = "easya11y";
    private static final String CONFIG_NODE_PATH = "/configuration";
    
    @Inject
    public ConfigurationEndpoint(EndpointDefinition definition) {
        super(definition);
    }

    /**
     * Get the current configuration settings.
     *
     * @return HTTP response with configuration data
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfiguration() {
        try {
            Session session = MgnlContext.getJCRSession(easya11y_WORKSPACE);
            Map<String, Object> result = new HashMap<>();
            Map<String, String> configuration = new HashMap<>();
            
            // Check if configuration node exists
            if (session.nodeExists(CONFIG_NODE_PATH)) {
                Node configNode = session.getNode(CONFIG_NODE_PATH);
                
                // Read all properties from the configuration node
                PropertyIterator properties = configNode.getProperties();
                while (properties.hasNext()) {
                    Property property = properties.nextProperty();
                    String propertyName = property.getName();
                    
                    // Skip JCR system properties
                    if (!propertyName.startsWith("jcr:") && !propertyName.startsWith("mgnl:")) {
                        configuration.put(propertyName, property.getString());
                    }
                }
            }
            
            result.put("success", true);
            result.put("configuration", configuration);
            
            return Response.ok(result).build();
            
        } catch (RepositoryException e) {
            log.error("Error retrieving configuration", e);
            return buildErrorResponse("Error retrieving configuration: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Save configuration settings.
     *
     * @param configuration Map of configuration key-value pairs
     * @return HTTP response indicating success or failure
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveConfiguration(Map<String, Object> configuration) {
        try {
            Session session = MgnlContext.getJCRSession(easya11y_WORKSPACE);
            Node configNode;
            
            // Create or get configuration node
            if (!session.nodeExists(CONFIG_NODE_PATH)) {
                Node rootNode = session.getRootNode();
                configNode = rootNode.addNode("configuration", NodeTypes.ContentNode.NAME);
            } else {
                configNode = session.getNode(CONFIG_NODE_PATH);
            }
            
            // Update properties
            for (Map.Entry<String, Object> entry : configuration.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Skip null values
                if (value == null) {
                    // Remove property if it exists
                    if (configNode.hasProperty(key)) {
                        configNode.getProperty(key).remove();
                    }
                    continue;
                }
                
                // Set property value
                if (value instanceof Boolean) {
                    PropertyUtil.setProperty(configNode, key, (Boolean) value);
                } else {
                    PropertyUtil.setProperty(configNode, key, value.toString());
                }
            }
            
            // Also set system properties for immediate use
            updateSystemProperties(configuration);
            
            // Save changes
            session.save();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Configuration saved successfully");
            
            return Response.ok(result).build();
            
        } catch (RepositoryException e) {
            log.error("Error saving configuration", e);
            return buildErrorResponse("Error saving configuration: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update system properties for immediate use without restart.
     * This allows the configuration to take effect immediately.
     *
     * @param configuration The configuration map
     */
    private void updateSystemProperties(Map<String, Object> configuration) {
        // reCAPTCHA settings
        if (configuration.containsKey("recaptchaSecretKey")) {
            System.setProperty("easya11y.recaptcha.secretKey", 
                configuration.get("recaptchaSecretKey").toString());
        }
        if (configuration.containsKey("recaptchaThreshold")) {
            System.setProperty("easya11y.recaptcha.threshold", 
                configuration.get("recaptchaThreshold").toString());
        }
    }
    
    /**
     * Build an error response.
     *
     * @param message The error message
     * @param status The HTTP status
     * @return HTTP response
     */
    private Response buildErrorResponse(String message, Response.Status status) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        
        return Response.status(status).entity(result).build();
    }
}