package work.noice.easya11y.storage;

import work.noice.easya11y.models.AccessibilityScanResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for storing and retrieving accessibility scan results.
 * Implementations can use JCR or a relational database.
 */
public interface StorageService {
    
    /**
     * Store scan results
     * @param scanResult The scan result to store
     * @param pagePath The page path in the content tree
     * @return The scan ID of the stored result
     */
    String storeScanResult(AccessibilityScanResult scanResult, String pagePath);
    
    /**
     * Get scan results for a specific page
     * @param pagePath The page path
     * @param limit Maximum number of results to return
     * @return List of scan results
     */
    List<AccessibilityScanResult> getScanResultsForPage(String pagePath, int limit);
    
    /**
     * Get all scan results with optional filtering
     * @param filterParams Map of filter parameters (e.g., startDate, endDate, minScore)
     * @param offset Offset for pagination
     * @param limit Maximum number of results
     * @return List of scan results
     */
    List<AccessibilityScanResult> getAllScanResults(Map<String, Object> filterParams, int offset, int limit);
    
    /**
     * Get a specific scan result by ID
     * @param scanId The scan ID
     * @return Optional containing the scan result if found
     */
    Optional<AccessibilityScanResult> getScanResultById(String scanId);
    
    /**
     * Delete scan results older than a certain date
     * @param daysToKeep Number of days of history to keep
     * @return Number of deleted records
     */
    int deleteOldScanResults(int daysToKeep);
    
    /**
     * Get summary statistics for all scans
     * @return Map containing statistics (totalScans, avgScore, etc.)
     */
    Map<String, Object> getScanStatistics();
    
    /**
     * Get historical trend data for a specific page or all pages
     * @param pagePath Optional page path (null for all pages)
     * @param days Number of days of history to retrieve
     * @return List of daily summary data
     */
    List<Map<String, Object>> getHistoricalTrends(String pagePath, int days);
    
    /**
     * Store configuration value
     * @param key Configuration key
     * @param value Configuration value
     */
    void storeConfiguration(String key, String value);
    
    /**
     * Get configuration value
     * @param key Configuration key
     * @return Optional containing the value if found
     */
    Optional<String> getConfiguration(String key);
    
    /**
     * Get all configuration values
     * @return Map of all configuration key-value pairs
     */
    Map<String, String> getAllConfiguration();
    
    /**
     * Test the storage connection
     * @return true if connection is successful
     */
    boolean testConnection();

    /**
     * Get the storage type
     * @return Storage type identifier (e.g., "jcr", "mysql", "postgresql")
     */
    String getStorageType();

    /**
     * Get scan results for a page by its UUID.
     * @param pageUuid The page UUID
     * @param limit Maximum number of results to return
     * @return List of scan results for the page
     */
    List<AccessibilityScanResult> getScanResultsByPageUuid(String pageUuid, int limit);

    /**
     * Register or update a page's UUID-to-path mapping.
     * @param pageUuid The page UUID
     * @param currentPath The current path of the page
     */
    void registerPage(String pageUuid, String currentPath);

    /**
     * Get the current path for a page UUID.
     * @param pageUuid The page UUID
     * @return Optional containing the current path if found
     */
    Optional<String> getPagePathByUuid(String pageUuid);

    /**
     * Get the UUID for a page path.
     * @param pagePath The page path
     * @return Optional containing the UUID if found
     */
    Optional<String> getPageUuidByPath(String pagePath);

    /**
     * Handle page move: update registry and log history.
     * @param pageUuid The page UUID
     * @param oldPath The old path before the move
     * @param newPath The new path after the move
     */
    void handlePageMove(String pageUuid, String oldPath, String newPath);
}