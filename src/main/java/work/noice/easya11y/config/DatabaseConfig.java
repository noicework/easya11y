package work.noice.easya11y.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import info.magnolia.module.ModuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Singleton
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE_NAME = "config.yaml";
    private static final String DECORATIONS_PATH = "decorations/easya11y";
    
    private final ModuleRegistry moduleRegistry;
    private ConfigData configData;
    
    @Inject
    public DatabaseConfig(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
        loadConfiguration();
    }
    
    public static class ConfigData {
        @JsonProperty("datasource")
        private DataSourceConfig datasource;
        
        @JsonProperty("storageType")
        private String storageType = "jcr"; // Default to JCR
        
        public DataSourceConfig getDatasource() {
            return datasource;
        }
        
        public void setDatasource(DataSourceConfig datasource) {
            this.datasource = datasource;
        }
        
        public String getStorageType() {
            return storageType;
        }
        
        public void setStorageType(String storageType) {
            this.storageType = storageType;
        }
        
        public boolean isDatabaseStorageEnabled() {
            return "database".equalsIgnoreCase(storageType) && datasource != null;
        }
    }
    
    public static class DataSourceConfig {
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("password")
        private String password;
        
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("driver")
        private String driver;
        
        @JsonProperty("migration")
        private MigrationConfig migration;
        
        // Getters and setters
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getDriver() {
            return driver;
        }
        
        public void setDriver(String driver) {
            this.driver = driver;
        }
        
        public MigrationConfig getMigration() {
            return migration;
        }
        
        public void setMigration(MigrationConfig migration) {
            this.migration = migration;
        }
    }
    
    public static class MigrationConfig {
        @JsonProperty("path")
        private String path;
        
        @JsonProperty("run")
        private boolean run = true;
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public boolean isRun() {
            return run;
        }
        
        public void setRun(boolean run) {
            this.run = run;
        }
    }
    
    private void loadConfiguration() {
        try {
            // Try to find config.yaml in light module decorations
            Optional<Path> configPath = findConfigFile();
            
            if (configPath.isPresent()) {
                log.info("Loading easya11y database configuration from: {}", configPath.get());
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                configData = mapper.readValue(configPath.get().toFile(), ConfigData.class);
                
                if (configData.isDatabaseStorageEnabled()) {
                    log.info("Database storage enabled with driver: {}", configData.getDatasource().getDriver());
                } else {
                    log.info("Using default JCR storage");
                }
            } else {
                log.info("No database configuration found, using default JCR storage");
                configData = new ConfigData(); // Use defaults
            }
        } catch (IOException e) {
            log.error("Failed to load database configuration", e);
            configData = new ConfigData(); // Use defaults on error
        }
    }
    
    private Optional<Path> findConfigFile() {
        // Look for config in light modules
        String lightModulesPath = System.getProperty("magnolia.resources.dir", "");
        log.debug("Looking for config file. magnolia.resources.dir = {}", lightModulesPath);
        
        if (!lightModulesPath.isEmpty()) {
            // Search all light modules for decorations/easya11y/config.yaml
            try {
                Path basePath = Paths.get(lightModulesPath);
                log.debug("Searching for config in: {}", basePath);
                
                Optional<Path> found = Files.walk(basePath, 4) // Increased depth to 4
                    .filter(path -> {
                        boolean matches = path.toString().endsWith(DECORATIONS_PATH + "/" + CONFIG_FILE_NAME);
                        if (matches) {
                            log.debug("Found matching config file: {}", path);
                        }
                        return matches;
                    })
                    .findFirst();
                    
                if (!found.isPresent()) {
                    log.debug("No config file found in light modules under {}", basePath);
                }
                
                return found;
            } catch (IOException e) {
                log.warn("Error searching for config.yaml in light modules", e);
            }
        } else {
            log.debug("magnolia.resources.dir is empty");
        }
        
        // Also check classpath for module-based config
        Path classPathConfig = Paths.get("easya11y", CONFIG_FILE_NAME);
        if (Files.exists(classPathConfig)) {
            log.debug("Found config in classpath: {}", classPathConfig);
            return Optional.of(classPathConfig);
        }
        
        log.debug("No config file found");
        return Optional.empty();
    }
    
    public ConfigData getConfig() {
        return configData;
    }
    
    public boolean isDatabaseStorageEnabled() {
        return configData != null && configData.isDatabaseStorageEnabled();
    }
    
    public void reloadConfiguration() {
        loadConfiguration();
    }
}