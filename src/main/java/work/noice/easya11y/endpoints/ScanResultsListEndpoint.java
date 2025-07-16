package work.noice.easya11y.endpoints;

import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;
import work.noice.easya11y.models.AccessibilityScanResult;
import work.noice.easya11y.storage.StorageService;
import work.noice.easya11y.storage.StorageServiceFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint for listing and filtering accessibility scan results.
 */
@Path("/easya11y/results")
public class ScanResultsListEndpoint extends AbstractEndpoint<EndpointDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ScanResultsListEndpoint.class);
    private final StorageServiceFactory storageServiceFactory;
    
    @Inject
    public ScanResultsListEndpoint(EndpointDefinition definition, StorageServiceFactory storageServiceFactory) {
        super(definition);
        this.storageServiceFactory = storageServiceFactory;
    }

    /**
     * Get a list of scan results with filtering options.
     *
     * @param pagePath Filter by page path
     * @param severity Filter by severity (critical, serious, moderate, minor)
     * @param wcagLevel Filter by WCAG level (A, AA, AAA)
     * @param dateFrom Filter by date from (timestamp)
     * @param dateTo Filter by date to (timestamp)
     * @param limit Maximum number of results to return
     * @param offset Offset for pagination
     * @param minScore Minimum score filter
     * @return HTTP response with scan results
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listScanResults(
            @QueryParam("pagePath") String pagePath,
            @QueryParam("severity") String severity,
            @QueryParam("wcagLevel") String wcagLevel,
            @QueryParam("dateFrom") Long dateFrom,
            @QueryParam("dateTo") Long dateTo,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("minScore") Double minScore) {
        
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            
            // Build filter parameters
            Map<String, Object> filterParams = new HashMap<>();
            if (pagePath != null) filterParams.put("pagePath", pagePath);
            if (wcagLevel != null) filterParams.put("wcagLevel", wcagLevel);
            if (dateFrom != null) filterParams.put("startDate", new Date(dateFrom));
            if (dateTo != null) filterParams.put("endDate", new Date(dateTo));
            if (minScore != null) filterParams.put("minScore", minScore);
            
            // Get results from storage
            int resultLimit = limit != null ? limit : 50;
            int resultOffset = offset != null ? offset : 0;
            List<AccessibilityScanResult> scanResults = storageService.getAllScanResults(filterParams, resultOffset, resultLimit);
            
            // Convert to response format
            List<Map<String, Object>> results = new ArrayList<>();
            for (AccessibilityScanResult scanResult : scanResults) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("scanId", scanResult.getId());
                resultMap.put("pagePath", scanResult.getPagePath());
                resultMap.put("pageUrl", scanResult.getPageUrl());
                resultMap.put("pageTitle", scanResult.getPageTitle());
                resultMap.put("scanDate", scanResult.getScanDate() != null ? scanResult.getScanDate().getTime() : null);
                resultMap.put("wcagLevel", scanResult.getWcagLevel());
                resultMap.put("score", scanResult.getScore());
                resultMap.put("violationCount", scanResult.getViolations().size());
                resultMap.put("violations_critical", scanResult.getViolationsByImpact().get("critical"));
                resultMap.put("violations_serious", scanResult.getViolationsByImpact().get("serious"));
                resultMap.put("violations_moderate", scanResult.getViolationsByImpact().get("moderate"));
                resultMap.put("violations_minor", scanResult.getViolationsByImpact().get("minor"));
                results.add(resultMap);
            }
            
            // Get summary statistics
            Map<String, Object> summary = storageService.getScanStatistics();
            
            // For database storage, add historical trends
            if (!"jcr".equals(storageService.getStorageType())) {
                List<Map<String, Object>> trends = storageService.getHistoricalTrends(null, 30);
                summary.put("historicalTrends", trends);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalResults", results.size());
            response.put("results", results);
            response.put("summary", summary);
            response.put("storageType", storageService.getStorageType());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            log.error("Error listing scan results", e);
            return buildErrorResponse("Error listing scan results: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get detailed scan result for a specific page.
     *
     * @param pagePath The page path
     * @param scanId The scan ID
     * @return HTTP response with detailed scan result
     */
    @GET
    @Path("/detail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScanDetail(@QueryParam("pagePath") String pagePath,
                                 @QueryParam("scanId") String scanId) {
        if (StringUtils.isBlank(pagePath) && StringUtils.isBlank(scanId)) {
            return buildErrorResponse("Page path or scan ID is required", Response.Status.BAD_REQUEST);
        }
        
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            
            AccessibilityScanResult result = null;
            
            if (scanId != null) {
                // Get by scan ID
                Optional<AccessibilityScanResult> scanResult = storageService.getScanResultById(scanId);
                if (scanResult.isPresent()) {
                    result = scanResult.get();
                }
            } else {
                // Get latest for page
                List<AccessibilityScanResult> results = storageService.getScanResultsForPage(pagePath, 1);
                if (!results.isEmpty()) {
                    result = results.get(0);
                }
            }
            
            if (result == null) {
                return buildErrorResponse("No scan results found", Response.Status.NOT_FOUND);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            log.error("Error getting scan detail", e);
            return buildErrorResponse("Error getting scan detail: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get historical trends for accessibility scans.
     *
     * @param pagePath Optional page path filter
     * @param days Number of days of history (default 30)
     * @return HTTP response with trend data
     */
    @GET
    @Path("/trends")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistoricalTrends(
            @QueryParam("pagePath") String pagePath,
            @QueryParam("days") Integer days) {
        
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            
            // Check if database storage is enabled
            if ("jcr".equals(storageService.getStorageType())) {
                return buildErrorResponse("Historical trends are only available with database storage", 
                                        Response.Status.NOT_IMPLEMENTED);
            }
            
            int daysToRetrieve = days != null && days > 0 ? days : 30;
            List<Map<String, Object>> trends = storageService.getHistoricalTrends(pagePath, daysToRetrieve);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pagePath", pagePath);
            response.put("days", daysToRetrieve);
            response.put("trends", trends);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            log.error("Error getting historical trends", e);
            return buildErrorResponse("Error getting historical trends: " + e.getMessage(), 
                                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Export scan results as CSV.
     *
     * @return CSV file with scan results
     */
    @GET
    @Path("/export/csv")
    @Produces("text/csv")
    public Response exportCsv(@QueryParam("pagePath") String pagePath,
                             @QueryParam("dateFrom") Long dateFrom,
                             @QueryParam("dateTo") Long dateTo) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            
            // Build filter parameters
            Map<String, Object> filterParams = new HashMap<>();
            if (pagePath != null) filterParams.put("pagePath", pagePath);
            if (dateFrom != null) filterParams.put("startDate", new Date(dateFrom));
            if (dateTo != null) filterParams.put("endDate", new Date(dateTo));
            
            // Get all results (no pagination for export)
            List<AccessibilityScanResult> scanResults = storageService.getAllScanResults(filterParams, 0, 10000);
            
            // Build CSV
            StringBuilder csv = new StringBuilder();
            csv.append("Page Path,Page Title,Scan Date,Score,Violations,Critical,Serious,Moderate,Minor\n");
            
            for (AccessibilityScanResult result : scanResults) {
                csv.append(escapeCsv(result.getPagePath())).append(",");
                csv.append(escapeCsv(result.getPageTitle())).append(",");
                csv.append(result.getScanDate()).append(",");
                csv.append(result.getScore()).append(",");
                csv.append(result.getViolations().size()).append(",");
                csv.append(result.getViolationsByImpact().get("critical")).append(",");
                csv.append(result.getViolationsByImpact().get("serious")).append(",");
                csv.append(result.getViolationsByImpact().get("moderate")).append(",");
                csv.append(result.getViolationsByImpact().get("minor")).append("\n");
            }
            
            return Response.ok(csv.toString())
                    .header("Content-Disposition", "attachment; filename=\"accessibility-scan-results.csv\"")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error exporting CSV", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error exporting CSV: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Escape CSV values.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Build an error response.
     */
    private Response buildErrorResponse(String message, Response.Status status) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        
        return Response.status(status).entity(result).build();
    }
}