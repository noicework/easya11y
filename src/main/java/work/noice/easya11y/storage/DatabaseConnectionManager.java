package work.noice.easya11y.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.noice.easya11y.config.DatabaseConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database connections using HikariCP connection pool
 * and handles database migrations using Flyway.
 */
@Singleton
public class DatabaseConnectionManager {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private final DatabaseConfig databaseConfig;
    private HikariDataSource dataSource;
    
    @Inject
    public DatabaseConnectionManager(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        initialize();
    }
    
    private void initialize() {
        if (!databaseConfig.isDatabaseStorageEnabled()) {
            log.info("Database storage is not enabled");
            return;
        }
        
        try {
            DatabaseConfig.DataSourceConfig dsConfig = databaseConfig.getConfig().getDatasource();
            
            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dsConfig.getUrl());
            config.setUsername(dsConfig.getUsername());
            config.setPassword(dsConfig.getPassword());
            config.setDriverClassName(dsConfig.getDriver());
            
            // Set pool configuration
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000); // 30 seconds
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            
            // Set pool name based on database type
            String poolName = "EasyA11y-";
            if (dsConfig.getUrl().contains("mysql")) {
                poolName += "MySQL";
            } else if (dsConfig.getUrl().contains("postgresql")) {
                poolName += "PostgreSQL";
            } else {
                poolName += "DB";
            }
            config.setPoolName(poolName);
            
            // Create the data source
            this.dataSource = new HikariDataSource(config);
            
            log.info("Database connection pool created successfully");
            
            // Run migrations if enabled
            if (dsConfig.getMigration() != null && dsConfig.getMigration().isRun()) {
                runMigrations(dsConfig);
            }
            
        } catch (Exception e) {
            log.error("Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }
    
    private void runMigrations(DatabaseConfig.DataSourceConfig dsConfig) {
        try {
            String migrationPath = dsConfig.getMigration().getPath();
            if (migrationPath == null || migrationPath.isEmpty()) {
                // Determine migration path based on database type
                if (dsConfig.getUrl().contains("mysql")) {
                    migrationPath = "db/migration/mysql";
                } else if (dsConfig.getUrl().contains("postgresql")) {
                    migrationPath = "db/migration/postgresql";
                } else {
                    log.warn("Unknown database type, skipping migrations");
                    return;
                }
            }
            
            log.info("Running database migrations from: {}", migrationPath);
            
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:" + migrationPath)
                .baselineOnMigrate(true)
                .load();
            
            int migrationCount = flyway.migrate().migrationsExecuted;
            log.info("Database migrations completed. {} migrations executed", migrationCount);
            
        } catch (Exception e) {
            log.error("Failed to run database migrations", e);
            throw new RuntimeException("Failed to run database migrations", e);
        }
    }
    
    /**
     * Get a database connection from the pool.
     * 
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database is not configured");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Get the DataSource for use with other frameworks.
     * 
     * @return The configured DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Test the database connection.
     * 
     * @return true if connection is successful
     */
    public boolean testConnection() {
        if (dataSource == null) {
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            log.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Get the database type based on the JDBC URL.
     * 
     * @return Database type (mysql, postgresql, unknown)
     */
    public String getDatabaseType() {
        if (!databaseConfig.isDatabaseStorageEnabled()) {
            return "none";
        }
        
        String url = databaseConfig.getConfig().getDatasource().getUrl();
        if (url.contains("mysql")) {
            return "mysql";
        } else if (url.contains("postgresql")) {
            return "postgresql";
        }
        return "unknown";
    }
    
    /**
     * Shutdown the connection pool.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            log.info("Shutting down database connection pool");
            dataSource.close();
        }
    }
}