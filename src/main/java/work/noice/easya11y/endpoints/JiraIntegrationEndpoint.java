package work.noice.easya11y.endpoints;

import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.noice.easya11y.storage.StorageService;
import work.noice.easya11y.storage.StorageServiceFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Path("/easya11y/jira")
public class JiraIntegrationEndpoint extends AbstractEndpoint<EndpointDefinition> {
    
    private static final Logger log = LoggerFactory.getLogger(JiraIntegrationEndpoint.class);
    private final StorageServiceFactory storageServiceFactory;
    
    @Inject
    public JiraIntegrationEndpoint(EndpointDefinition endpointDefinition, StorageServiceFactory storageServiceFactory) {
        super(endpointDefinition);
        this.storageServiceFactory = storageServiceFactory;
    }
    
    @POST
    @Path("/create-issue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createJiraIssue(Map<String, Object> requestData) {
        try {
            // Get configuration from StorageService
            StorageService storageService = storageServiceFactory.getStorageService();
            
            String jiraUrl = storageService.getConfiguration("jiraUrl").orElse("");
            String jiraApiToken = storageService.getConfiguration("jiraApiToken").orElse("");
            String jiraEmail = storageService.getConfiguration("jiraEmail").orElse("");
            
            log.debug("JIRA Configuration - URL: {}, Email: {}, Token length: {}", 
                jiraUrl, jiraEmail, jiraApiToken.length());
            
            if (StringUtils.isBlank(jiraUrl) || StringUtils.isBlank(jiraApiToken) || StringUtils.isBlank(jiraEmail)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse("JIRA integration not configured. Please configure jiraUrl, jiraApiToken, and jiraEmail."))
                    .build();
            }
            
            // Extract issue data
            String projectKey = (String) requestData.get("projectKey");
            String summary = (String) requestData.get("summary");
            String description = (String) requestData.get("description");
            String issueType = (String) requestData.getOrDefault("issueType", "Bug");
            String priority = (String) requestData.getOrDefault("priority", "Medium");
            List<String> labels = (List<String>) requestData.getOrDefault("labels", new ArrayList<>());
            Map<String, Object> scanResult = (Map<String, Object>) requestData.get("scanResult");
            
            // Build JIRA issue payload
            Map<String, Object> issue = new HashMap<>();
            Map<String, Object> fields = new HashMap<>();
            
            fields.put("project", Collections.singletonMap("key", projectKey));
            fields.put("summary", summary);
            fields.put("description", description);
            fields.put("issuetype", Collections.singletonMap("name", issueType));
            fields.put("priority", Collections.singletonMap("name", priority));
            fields.put("labels", labels);
            
            // Add custom fields if needed
            if (scanResult != null) {
                // You can add custom fields here based on your JIRA configuration
                // fields.put("customfield_10001", scanResult.get("score"));
            }
            
            issue.put("fields", fields);
            
            // Create issue via JIRA API
            String issueKey = createIssueInJira(jiraUrl, jiraEmail, jiraApiToken, issue);
            
            if (issueKey != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("key", issueKey);
                response.put("url", jiraUrl + "/browse/" + issueKey);
                
                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to create JIRA issue"))
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Error creating JIRA issue", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createErrorResponse(e.getMessage()))
                .build();
        }
    }
    
    private String createIssueInJira(String jiraUrl, String email, String apiToken, Map<String, Object> issueData) throws IOException {
        // Remove trailing slash if present
        if (jiraUrl.endsWith("/")) {
            jiraUrl = jiraUrl.substring(0, jiraUrl.length() - 1);
        }
        String apiUrl = jiraUrl + "/rest/api/2/issue";
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(apiUrl);
            
            // Set headers
            String authString = email + ":" + apiToken;
            String auth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
            log.debug("Auth string length: {}, Base64 auth length: {}", authString.length(), auth.length());
            post.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
            post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            
            // Set body
            String jsonBody = convertToJson(issueData);
            log.debug("Sending to JIRA API: {}", apiUrl);
            log.debug("Request body: {}", jsonBody);
            post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                if (statusCode == 201) {
                    // Parse response to get issue key
                    Map<String, Object> responseMap = parseJson(responseBody);
                    return (String) responseMap.get("key");
                } else {
                    log.error("JIRA API error: {} - {}", statusCode, responseBody);
                    return null;
                }
            }
        }
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
    
    // Simple JSON conversion (in production, use Jackson or Gson)
    private String convertToJson(Map<String, Object> data) {
        // This is a simplified version. In production, use a proper JSON library
        StringBuilder json = new StringBuilder("{");
        Iterator<Map.Entry<String, Object>> iter = data.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            json.append("\"").append(entry.getKey()).append("\":");
            
            if (entry.getValue() instanceof String) {
                json.append("\"").append(escapeJson((String) entry.getValue())).append("\"");
            } else if (entry.getValue() instanceof Map) {
                json.append(convertToJson((Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof List) {
                json.append(convertListToJson((List<?>) entry.getValue()));
            } else {
                json.append(entry.getValue());
            }
            
            if (iter.hasNext()) {
                json.append(",");
            }
        }
        
        json.append("}");
        return json.toString();
    }
    
    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        Iterator<?> iter = list.iterator();
        
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof String) {
                json.append("\"").append(escapeJson((String) item)).append("\"");
            } else {
                json.append(item);
            }
            
            if (iter.hasNext()) {
                json.append(",");
            }
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    
    private Map<String, Object> parseJson(String json) {
        // Simplified JSON parsing - in production use Jackson or Gson
        Map<String, Object> result = new HashMap<>();
        
        // Extract key from response like: {"id":"10000","key":"PROJ-123","self":"..."}
        int keyStart = json.indexOf("\"key\":\"") + 7;
        if (keyStart > 6) {
            int keyEnd = json.indexOf("\"", keyStart);
            if (keyEnd > keyStart) {
                result.put("key", json.substring(keyStart, keyEnd));
            }
        }
        
        return result;
    }
}