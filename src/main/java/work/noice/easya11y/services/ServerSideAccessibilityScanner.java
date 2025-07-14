package work.noice.easya11y.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for running axe-core accessibility scans server-side using Selenium WebDriver.
 * Uses basic authentication in URLs to bypass the login form.
 */
@Singleton
public class ServerSideAccessibilityScanner {
    
    private static final Logger log = LoggerFactory.getLogger(ServerSideAccessibilityScanner.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int PAGE_LOAD_TIMEOUT = 30;
    private static final int SCRIPT_TIMEOUT = 30;
    
    // Default admin credentials - should be externalized in production
    private static final String DEFAULT_USERNAME = "superuser";
    private static final String DEFAULT_PASSWORD = "superuser";
    
    private String axeCoreScript;
    
    public ServerSideAccessibilityScanner() {
        // Load axe-core script from resources
        loadAxeCoreScript();
        // Setup WebDriverManager
        WebDriverManager.chromedriver().setup();
    }
    
    /**
     * Run accessibility scan on a given URL.
     * Handles authentication by setting up Chrome to use basic auth headers.
     *
     * @param url The URL to scan
     * @param wcagLevel The WCAG level to test (A, AA, AAA)
     * @return Scan results as JsonNode
     */
    public JsonNode scanUrl(String url, String wcagLevel) throws Exception {
        ChromeDriver driver = null;
        try {
            driver = (ChromeDriver) createWebDriver();
            
            // Enable Network domain for Chrome DevTools
            driver.executeCdpCommand("Network.enable", new HashMap<>());
            
            // Set up Basic Authentication header
            String credentials = java.util.Base64.getEncoder()
                .encodeToString((DEFAULT_USERNAME + ":" + DEFAULT_PASSWORD).getBytes());
            
            Map<String, Object> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + credentials);
            
            Map<String, Object> params = new HashMap<>();
            params.put("headers", headers);
            
            // Set extra HTTP headers that will be sent with every request
            driver.executeCdpCommand("Network.setExtraHTTPHeaders", params);
            
            log.info("Original URL: {}", url);
            log.info("Navigating with Basic Auth header set");
            driver.get(url);
            
            // Wait for page to load
            Thread.sleep(2000);
            
            // Log the current page details to debug authentication
            String currentUrl = driver.getCurrentUrl();
            String pageTitle = driver.getTitle();
            log.info("Current URL after navigation: {}", currentUrl);
            log.info("Page title: {}", pageTitle);
            
            // Check if we're still on login page
            String pageSource = driver.getPageSource();
            if (pageTitle.equals("Magnolia 6") || pageTitle.toLowerCase().contains("login") || 
                currentUrl.contains("login") || pageSource.contains("defaultMagnoliaLoginForm")) {
                log.error("Still on login page after basic auth attempt");
                log.error("Page title indicates login page: {}", pageTitle);
                
                // Check for specific login page markers
                if (pageSource.contains("magnolia-logo-dark.svg") && pageSource.contains("defaultMagnoliaLoginForm")) {
                    log.error("Definitely on Magnolia login page - authentication failed!");
                    throw new RuntimeException("Authentication failed - still on login page");
                }
            }
            
            // Inject axe-core
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(axeCoreScript);
            
            // Wait for axe to be available (if using CDN)
            if (axeCoreScript.contains("cdnjs.cloudflare.com")) {
                js.executeAsyncScript(
                    "var callback = arguments[arguments.length - 1];" +
                    "var checkAxe = function() {" +
                    "  if (typeof axe !== 'undefined') {" +
                    "    callback(true);" +
                    "  } else {" +
                    "    setTimeout(checkAxe, 100);" +
                    "  }" +
                    "};" +
                    "checkAxe();"
                );
            }
            
            // Configure and run axe
            String axeConfig = buildAxeConfig(wcagLevel);
            log.info("Running axe-core with WCAG level: {} and config: {}", wcagLevel, axeConfig);
            
            Object result = js.executeAsyncScript(
                "var callback = arguments[arguments.length - 1];" +
                "console.log('Configuring axe with:', " + axeConfig + ");" +
                "axe.configure(" + axeConfig + ");" +
                "axe.run().then(function(results) {" +
                "  console.log('Axe scan completed. Violations:', results.violations.length);" +
                "  console.log('Passes:', results.passes.length);" +
                "  callback(JSON.stringify(results));" +
                "}).catch(function(err) {" +
                "  console.error('Axe error:', err);" +
                "  callback(JSON.stringify({error: err.message}));" +
                "});"
            );
            
            // Parse results
            String jsonResult = (String) result;
            JsonNode axeResults = objectMapper.readTree(jsonResult);
            
            if (axeResults.has("error")) {
                throw new RuntimeException("Axe-core error: " + axeResults.get("error").asText());
            }
            
            // Log results summary
            int violationCount = axeResults.has("violations") ? axeResults.get("violations").size() : 0;
            int passCount = axeResults.has("passes") ? axeResults.get("passes").size() : 0;
            int incompleteCount = axeResults.has("incomplete") ? axeResults.get("incomplete").size() : 0;
            
            log.info("Axe scan results - Violations: {}, Passes: {}, Incomplete: {}", 
                     violationCount, passCount, incompleteCount);
            
            // Log violation details
            if (axeResults.has("violations") && violationCount > 0) {
                JsonNode violations = axeResults.get("violations");
                for (JsonNode violation : violations) {
                    String id = violation.get("id").asText();
                    String impact = violation.get("impact").asText();
                    int nodeCount = violation.get("nodes").size();
                    log.info("  Violation: {} (impact: {}, nodes: {})", id, impact, nodeCount);
                }
            }
            
            return axeResults;
            
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    
    /**
     * Scan multiple URLs in batch.
     *
     * @param urls List of URLs to scan
     * @param wcagLevel The WCAG level to test
     * @return Map of URL to scan results
     */
    public Map<String, JsonNode> scanUrls(Map<String, String> urls, String wcagLevel) {
        Map<String, JsonNode> results = new HashMap<>();
        
        for (Map.Entry<String, String> entry : urls.entrySet()) {
            String pagePath = entry.getKey();
            String url = entry.getValue();
            
            try {
                JsonNode scanResult = scanUrl(url, wcagLevel);
                results.put(pagePath, scanResult);
            } catch (Exception e) {
                log.error("Error scanning URL: " + url, e);
                // Create error result
                results.put(pagePath, createErrorResult(e.getMessage()));
            }
        }
        
        return results;
    }
    
    private ChromeDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        // Disable Chrome's basic auth dialog which blocks headless mode
        options.addArguments("--disable-blink-features=BlockCredentialedSubresources");
        
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(PAGE_LOAD_TIMEOUT));
        driver.manage().timeouts().scriptTimeout(java.time.Duration.ofSeconds(SCRIPT_TIMEOUT));
        
        return driver;
    }
    
    private void loadAxeCoreScript() {
        try {
            // Load axe-core from node_modules or resources
            InputStream is = getClass().getResourceAsStream("/easya11y/webresources/vendor/axe.min.js");
            if (is == null) {
                // Try alternate location
                is = getClass().getResourceAsStream("/axe.min.js");
            }
            
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    axeCoreScript = reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                log.warn("Could not load axe-core script from resources, will use CDN");
                axeCoreScript = "var script = document.createElement('script');" +
                               "script.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.8.2/axe.min.js';" +
                               "script.onload = function() { console.log('axe-core loaded'); };" +
                               "document.head.appendChild(script);";
            }
        } catch (IOException e) {
            log.error("Error loading axe-core script", e);
        }
    }
    
    private String buildAxeConfig(String wcagLevel) {
        // Build axe configuration based on WCAG level
        String tags = "";
        switch (wcagLevel.toUpperCase()) {
            case "A":
                tags = "\"wcag2a\", \"wcag21a\", \"wcag22a\", \"best-practice\"";
                break;
            case "AA":
                tags = "\"wcag2a\", \"wcag2aa\", \"wcag21a\", \"wcag21aa\", \"wcag22a\", \"wcag22aa\", \"best-practice\"";
                break;
            case "AAA":
                tags = "\"wcag2a\", \"wcag2aa\", \"wcag2aaa\", \"wcag21a\", \"wcag21aa\", \"wcag21aaa\", \"wcag22a\", \"wcag22aa\", \"wcag22aaa\", \"best-practice\"";
                break;
            default:
                tags = "\"wcag2a\", \"wcag2aa\", \"wcag21a\", \"wcag21aa\", \"wcag22a\", \"wcag22aa\", \"best-practice\"";
        }
        
        log.debug("Building axe config for WCAG level {} with tags: {}", wcagLevel, tags);
        
        return "{" +
               "  rules: []," +
               "  runOnly: {" +
               "    type: \"tag\"," +
               "    values: [" + tags + "]" +
               "  }," +
               "  resultTypes: [\"violations\", \"passes\", \"incomplete\", \"inapplicable\"]" +
               "}";
    }
    
    private JsonNode createErrorResult(String errorMessage) {
        try {
            return objectMapper.readTree("{\"error\": \"" + errorMessage + "\"}");
        } catch (Exception e) {
            return null;
        }
    }
    
}