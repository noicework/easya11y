package work.noice.easya11y.storage;

import work.noice.easya11y.config.DatabaseConfig;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating the appropriate StorageService implementation
 * based on configuration.
 */
@Singleton
public class StorageServiceFactory {
    
    private static final Logger log = LoggerFactory.getLogger(StorageServiceFactory.class);
    
    private final DatabaseConfig databaseConfig;
    private final JcrStorageService jcrStorageService;
    private final DatabaseStorageService databaseStorageService;
    
    @Inject
    public StorageServiceFactory(
            DatabaseConfig databaseConfig,
            JcrStorageService jcrStorageService,
            DatabaseStorageService databaseStorageService) {
        this.databaseConfig = databaseConfig;
        this.jcrStorageService = jcrStorageService;
        this.databaseStorageService = databaseStorageService;
    }
    
    /**
     * Get the appropriate storage service based on configuration.
     * 
     * @return StorageService implementation
     */
    public StorageService getStorageService() {
        if (databaseConfig.isDatabaseStorageEnabled()) {
            log.info("Using database storage service");
            return databaseStorageService;
        } else {
            log.info("Using JCR storage service");
            return jcrStorageService;
        }
    }
    
    /**
     * Get the storage type currently in use.
     * 
     * @return Storage type (jcr or database type)
     */
    public String getStorageType() {
        if (databaseConfig.isDatabaseStorageEnabled()) {
            return databaseStorageService.getStorageType();
        } else {
            return jcrStorageService.getStorageType();
        }
    }
    
    /**
     * Test the current storage connection.
     * 
     * @return true if connection is successful
     */
    public boolean testConnection() {
        return getStorageService().testConnection();
    }
}