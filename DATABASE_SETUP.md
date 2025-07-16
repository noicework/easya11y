# Database Storage for easya11y

The easya11y module supports storing accessibility scan results in either JCR (default) or an external database (MySQL/PostgreSQL). Database storage enables historical data retention and advanced analytics.

## Benefits of Database Storage

- **Historical Data**: Retain scan results indefinitely without impacting JCR performance
- **Advanced Queries**: Use SQL for complex filtering and reporting
- **Trend Analysis**: Track accessibility scores over time
- **Scalability**: Better performance for large numbers of scans
- **Data Export**: Easy integration with external analytics tools

## Configuration

### 1. Add Database Driver Dependency

Add the appropriate database driver to your project's `pom.xml`:

#### For PostgreSQL:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```

#### For MySQL:
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### 2. Create Database

Create a database for easya11y data:

#### PostgreSQL:
```sql
CREATE DATABASE easya11y_db;
CREATE USER easya11y_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE easya11y_db TO easya11y_user;
```

#### MySQL:
```sql
CREATE DATABASE easya11y_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'easya11y_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON easya11y_db.* TO 'easya11y_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure easya11y

Create a configuration file in your light module:
`<light-module-name>/decorations/easya11y/config.yaml`

```yaml
# Enable database storage
storageType: database

# Database connection configuration
datasource:
  username: easya11y_user
  password: your_password_here
  url: jdbc:postgresql://localhost:5432/easya11y_db
  driver: org.postgresql.Driver
  
  # For MySQL, use:
  # url: jdbc:mysql://localhost:3306/easya11y_db
  # driver: com.mysql.cj.jdbc.Driver
  
  migration:
    run: true  # Auto-run database migrations on startup
```

### 4. Restart Magnolia

After adding the configuration, restart your Magnolia instance. The module will:
1. Connect to the database
2. Run migrations to create the required tables
3. Start storing new scan results in the database

## Database Schema

The following tables are created automatically:

- **audit_scans**: Main scan results
- **audit_violations**: Individual accessibility violations
- **audit_violation_nodes**: DOM nodes affected by violations
- **audit_passes**: Successful accessibility checks
- **audit_incomplete**: Incomplete checks
- **audit_configuration**: Module configuration
- **audit_scan_history_summary**: View for historical analytics

## API Endpoints

When database storage is enabled, additional endpoints become available:

### Historical Trends
```
GET /easya11y/results/trends?days=30&pagePath=/home
```

Returns daily aggregated scan data for trend analysis.

### Advanced Filtering
```
GET /easya11y/results?minScore=80&dateFrom=1234567890&dateTo=1234567890
```

Filter results by score, date range, and other criteria.

## Monitoring

### Test Database Connection
```
GET /easya11y/configuration/test-connection
```

### Check Storage Type
```
GET /easya11y/configuration
```

The response includes `storageType` indicating whether JCR or database storage is active.

## Migration from JCR

If you have existing data in JCR and want to switch to database storage:

1. Export existing data using the CSV export endpoint
2. Configure database storage as described above
3. New scans will be stored in the database
4. Old JCR data remains accessible but won't be included in new queries

## Troubleshooting

### Connection Issues
- Verify database is running and accessible
- Check username/password in config.yaml
- Ensure database driver is in classpath
- Review Magnolia logs for connection errors

### Migration Failures
- Check database user has CREATE TABLE permissions
- Verify database charset supports UTF-8
- Review migration logs in Magnolia

### Performance
- Add indexes for frequently queried columns if needed
- Configure connection pool size in HikariCP
- Monitor database query performance

## Security Considerations

1. **Credentials**: Store database passwords securely, consider using environment variables
2. **Network**: Use SSL/TLS for database connections in production
3. **Permissions**: Grant minimum required database permissions
4. **Backups**: Implement regular database backups for audit data