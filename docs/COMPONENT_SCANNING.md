# Component-Level Accessibility Scanning

This document describes the component-level scanning functionality added to the easya11y module.

## Overview

The component scanning feature allows you to scan individual components in isolation, rather than entire pages. This is useful for:

- Testing specific UI components during development
- Validating component libraries
- Performing targeted accessibility checks
- Real-time scanning during component editing

## REST API Endpoints

### 1. Scan Single Component

**Endpoint:** `GET /easya11y/scan/component/{componentId}`

**Parameters:**
- `componentId` (path) - Component ID or path
- `wcagLevel` (query) - WCAG level: A, AA, or AAA (default: AA)
- `includeChildren` (query) - Include child components (default: true)
- `rules` (query) - Specific axe-core rules to check (optional)
- `resultFormat` (query) - Result format: standard or detailed (default: standard)

**Example Request:**
```bash
GET /.rest/easya11y/scan/component/my-component-id?wcagLevel=AA&includeChildren=true
```

**Response:**
```json
{
  "success": true,
  "scanId": "uuid-here",
  "componentId": "my-component-id",
  "componentPath": "/path/to/component",
  "componentUrl": "http://localhost:8080/page.html?componentPath=/path/to/component",
  "componentType": "text/image",
  "wcagLevel": "AA",
  "includeChildren": true,
  "rules": [],
  "message": "Component scan initiated. Use the URL to scan with axe-core."
}
```

### 2. Batch Component Scanning

**Endpoint:** `POST /easya11y/scan/component/batch`

**Request Body:**
```json
{
  "componentIds": ["component1", "component2", "component3"],
  "wcagLevel": "AA",
  "includeChildren": true,
  "rules": ["color-contrast", "image-alt"],
  "resultFormat": "standard"
}
```

**Response:**
```json
{
  "success": true,
  "scans": [
    {
      "scanId": "uuid-1",
      "componentId": "component1",
      "componentPath": "/path/to/component1",
      "componentUrl": "http://...",
      "componentType": "text"
    },
    {
      "scanId": "uuid-2",
      "componentId": "component2",
      "componentPath": "/path/to/component2",
      "componentUrl": "http://...",
      "componentType": "image"
    }
  ],
  "wcagLevel": "AA",
  "includeChildren": true,
  "rules": ["color-contrast", "image-alt"],
  "resultFormat": "standard",
  "notFound": ["component3"],
  "message": "Batch scan initiated for 2 components"
}
```

### 3. Submit Scan Results

**Endpoint:** `POST /easya11y/scan/component/results`

**Request Body:**
```json
{
  "scanId": "uuid-here",
  "componentId": "my-component-id",
  "componentSelector": "#component1",
  "componentUrl": "http://...",
  "axeResults": {
    "violations": [...],
    "passes": [...],
    "incomplete": [...],
    "inapplicable": [...],
    "toolOptions": {
      "version": "4.10.0"
    }
  }
}
```

**Response:**
```json
{
  "success": true,
  "scanId": "uuid-here",
  "componentId": "my-component-id",
  "score": 95.5,
  "violationCount": 2,
  "passCount": 38,
  "totalElements": 40,
  "elementsWithIssues": 2,
  "message": "Component scan results stored successfully"
}
```

### 4. Get Component Scan History

**Endpoint:** `GET /easya11y/scan/component/{componentId}/results`

**Parameters:**
- `componentId` (path) - Component ID
- `limit` (query) - Maximum number of results (default: 10)

**Response:**
```json
{
  "componentId": "my-component-id",
  "results": [
    {
      "scanId": "uuid-1",
      "scanDate": "2024-01-07T10:30:00Z",
      "score": 95.5,
      "violationCount": 2,
      "passCount": 38,
      "wcagLevel": "AA"
    },
    {
      "scanId": "uuid-2",
      "scanDate": "2024-01-06T15:20:00Z",
      "score": 92.0,
      "violationCount": 3,
      "passCount": 37,
      "wcagLevel": "AA"
    }
  ],
  "totalResults": 15
}
```

## JavaScript API

### ComponentScanner Class

```javascript
// Initialize scanner
const scanner = new ComponentScanner(apiBase);

// Scan a single component
const results = await scanner.scanComponent({
    componentId: 'my-component',
    componentSelector: '#component1',
    wcagLevel: 'AA',
    includeChildren: true,
    rules: ['color-contrast', 'image-alt']
});

// Scan multiple components
const batchResults = await scanner.scanComponents([
    { componentId: 'comp1', componentSelector: '#comp1' },
    { componentId: 'comp2', componentSelector: '#comp2' }
]);

// Initiate scan via API
const initResponse = await scanner.initiateScan('component-id', {
    wcagLevel: 'AA',
    includeChildren: true
});

// Submit results to server
const submitResponse = await scanner.submitResults(scanId, scanResults);

// Get component history
const history = await scanner.getComponentHistory('component-id', 10);
```

### ComponentScannerUI Class

```javascript
// Initialize UI helper
const scannerUI = new ComponentScannerUI(scanner);

// Highlight a component
scannerUI.highlightComponent('#component1');

// Show results in popup
scannerUI.showResults(scanResults);
```

## Integration Examples

### 1. Real-time Component Scanning

```javascript
// Scan component on user interaction
document.getElementById('scanBtn').addEventListener('click', async () => {
    const selector = '#myComponent';
    
    // Highlight component
    scannerUI.highlightComponent(selector);
    
    // Perform scan
    const results = await scanner.scanComponent({
        componentId: 'my-component',
        componentSelector: selector,
        wcagLevel: 'AA'
    });
    
    // Display results
    scannerUI.showResults(results);
});
```

### 2. Component Library Validation

```javascript
// Scan all components in a library
async function validateComponentLibrary() {
    const components = document.querySelectorAll('[data-component]');
    const scanConfigs = [];
    
    components.forEach((comp, index) => {
        scanConfigs.push({
            componentId: comp.dataset.component || `component-${index}`,
            componentSelector: `[data-component="${comp.dataset.component}"]`,
            wcagLevel: 'AA',
            includeChildren: true
        });
    });
    
    const results = await scanner.scanComponents(scanConfigs);
    
    // Process results
    const failedComponents = results.filter(r => 
        r.success && r.axeResults.violations.length > 0
    );
    
    console.log(`Found ${failedComponents.length} components with issues`);
}
```

### 3. Editor Integration

```javascript
// Integrate with Magnolia editor
class ComponentEditorIntegration {
    constructor(scanner) {
        this.scanner = scanner;
        this.setupListeners();
    }
    
    setupListeners() {
        // Listen for component selection in editor
        document.addEventListener('mgnl:component:selected', async (e) => {
            const componentPath = e.detail.path;
            const componentElement = e.detail.element;
            
            // Scan the selected component
            const results = await this.scanner.scanComponent({
                componentId: componentPath,
                componentSelector: `[data-mgnl-path="${componentPath}"]`,
                wcagLevel: 'AA'
            });
            
            // Show inline feedback
            this.showInlineFeedback(componentElement, results);
        });
    }
    
    showInlineFeedback(element, results) {
        const violations = results.axeResults.violations.length;
        
        // Add visual indicator
        element.dataset.a11yStatus = violations > 0 ? 'error' : 'pass';
        element.title = `Accessibility: ${violations} issues found`;
    }
}
```

## Configuration

### Module Configuration

The component scan endpoint is configured in:
`/easya11y/restEndpoints/componentScan.yaml`

```yaml
class: info.magnolia.rest.registry.ConfiguredEndpointDefinition
implementationClass: work.noice.easya11y.endpoints.ComponentScanEndpoint
```

### Security

The endpoint follows Magnolia's standard REST security model. Users must have appropriate permissions to:
- Read components from the website workspace
- Write scan results to the easya11y workspace

### Storage

Component scan results are stored in the JCR under:
```
/easya11y
  /componentScans
    /{componentId}
      /scan_[timestamp]
        - scanId
        - componentPath
        - componentUrl
        - scanDate
        - wcagLevel
        - score
        - violationCount
        - passCount
        - totalElements
        - elementsWithIssues
        - fullResults (JSON)
```

## Best Practices

1. **Component Isolation**: When scanning components, ensure they're properly isolated from page-level styles that might affect accessibility.

2. **Batch Scanning**: Use batch scanning for better performance when checking multiple components.

3. **Caching**: Implement client-side caching of scan results to avoid redundant scans.

4. **Progressive Enhancement**: Start with basic scans and add more specific rules as needed.

5. **Regular Scanning**: Set up automated scans for component libraries as part of CI/CD pipelines.

## Troubleshooting

### Common Issues

1. **Component Not Found**
   - Verify the component ID or path exists
   - Check permissions on the website workspace

2. **Scan Results Not Storing**
   - Verify write permissions on the easya11y workspace
   - Check JCR session limits

3. **Incomplete Results**
   - Ensure axe-core is properly loaded
   - Verify component is fully rendered before scanning

### Debug Mode

Enable debug logging for the component scanner:

```javascript
// In browser console
localStorage.setItem('easya11y.debug', 'true');

// In Java
Logger log = LoggerFactory.getLogger(ComponentScanEndpoint.class);
log.setLevel(Level.DEBUG);
```