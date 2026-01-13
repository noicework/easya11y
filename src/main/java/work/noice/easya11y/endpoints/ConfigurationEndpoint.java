package work.noice.easya11y.endpoints;

import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;
import work.noice.easya11y.storage.StorageService;
import work.noice.easya11y.storage.StorageServiceFactory;
import work.noice.easya11y.config.DatabaseConfig;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
    
    private final StorageServiceFactory storageServiceFactory;
    private final DatabaseConfig databaseConfig;
    
    @Inject
    public ConfigurationEndpoint(EndpointDefinition definition, 
                               StorageServiceFactory storageServiceFactory,
                               DatabaseConfig databaseConfig) {
        super(definition);
        this.storageServiceFactory = storageServiceFactory;
        this.databaseConfig = databaseConfig;
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
            StorageService storageService = storageServiceFactory.getStorageService();
            Map<String, Object> result = new HashMap<>();
            
            // Get configuration from storage service
            Map<String, String> configuration = new HashMap<>(storageService.getAllConfiguration());
            
            // Add storage type info
            configuration.put("storageType", storageService.getStorageType());
            configuration.put("databaseEnabled", String.valueOf(databaseConfig.isDatabaseStorageEnabled()));
            
            result.put("success", true);
            result.put("configuration", configuration);
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
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
            StorageService storageService = storageServiceFactory.getStorageService();
            
            // Update properties in storage
            for (Map.Entry<String, Object> entry : configuration.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Skip read-only properties
                if ("storageType".equals(key) || "databaseEnabled".equals(key)) {
                    continue;
                }
                
                // Skip null values
                if (value == null) {
                    continue;
                }
                
                // Store property value
                storageService.storeConfiguration(key, value.toString());
            }
            
            // Also set system properties for immediate use
            updateSystemProperties(configuration);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Configuration saved successfully");
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            log.error("Error saving configuration", e);
            return buildErrorResponse("Error saving configuration: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get the system configuration (YAML-based, read-only).
     * This shows the database configuration and other system settings.
     *
     * @return HTTP response with system configuration data
     */
    @GET
    @Path("/system")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemConfiguration() {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> systemConfig = new HashMap<>();

            // Storage type
            systemConfig.put("storageType", databaseConfig.getConfig().getStorageType());
            systemConfig.put("databaseEnabled", databaseConfig.isDatabaseStorageEnabled());

            // Database configuration (if enabled)
            if (databaseConfig.isDatabaseStorageEnabled()) {
                DatabaseConfig.DataSourceConfig dsConfig = databaseConfig.getConfig().getDatasource();
                Map<String, Object> dbConfig = new HashMap<>();

                // Don't expose password
                dbConfig.put("url", dsConfig.getUrl());
                dbConfig.put("username", dsConfig.getUsername());
                dbConfig.put("driver", dsConfig.getDriver());
                dbConfig.put("passwordConfigured", dsConfig.getPassword() != null && !dsConfig.getPassword().isEmpty());

                // Migration config
                if (dsConfig.getMigration() != null) {
                    Map<String, Object> migrationConfig = new HashMap<>();
                    migrationConfig.put("path", dsConfig.getMigration().getPath());
                    migrationConfig.put("runOnStartup", dsConfig.getMigration().isRun());
                    dbConfig.put("migration", migrationConfig);
                }

                systemConfig.put("datasource", dbConfig);
            }

            result.put("success", true);
            result.put("systemConfiguration", systemConfig);

            return Response.ok(result).build();

        } catch (Exception e) {
            log.error("Error retrieving system configuration", e);
            return buildErrorResponse("Error retrieving system configuration: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Test database connection.
     *
     * @return HTTP response with connection test result
     */
    @GET
    @Path("/test-connection")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testDatabaseConnection() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            if (!databaseConfig.isDatabaseStorageEnabled()) {
                result.put("success", false);
                result.put("message", "Database storage is not configured");
                return Response.ok(result).build();
            }
            
            StorageService storageService = storageServiceFactory.getStorageService();
            boolean connectionSuccess = storageService.testConnection();
            
            result.put("success", connectionSuccess);
            result.put("storageType", storageService.getStorageType());
            result.put("message", connectionSuccess ? 
                "Database connection successful" : 
                "Database connection failed");
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            log.error("Error testing database connection", e);
            return buildErrorResponse("Error testing connection: " + e.getMessage(), 
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
        // WCAG settings
        if (configuration.containsKey("wcagVersion")) {
            System.setProperty("easya11y.wcag.version", 
                configuration.get("wcagVersion").toString());
        }
        if (configuration.containsKey("wcagLevel")) {
            System.setProperty("easya11y.wcag.level", 
                configuration.get("wcagLevel").toString());
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
