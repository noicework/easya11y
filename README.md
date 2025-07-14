# EasyA11y - Magnolia CMS Accessibility Checker Module

## Overview

EasyA11y is a Magnolia CMS module that provides comprehensive accessibility testing for your website pages. It integrates axe-core to scan pages for WCAG compliance and accessibility issues, providing detailed reports and tracking improvements over time.

## Architecture

### Backend Components

1. **REST Endpoints**
   - `PageListEndpoint` - Lists pages from the website workspace with scan status
   - `AccessibilityScanEndpoint` - Initiates scans and stores results
   - `ScanResultsListEndpoint` - Lists and filters scan results, exports reports

2. **Model Classes**
   - `AccessibilityScanResult` - Comprehensive model for scan results including violations, passes, and statistics

3. **UI Components**
   - `AccessibilityCheckerSubApp` - Magnolia UI subapp that displays the checker interface
   - `AccessibilityCheckerSubAppDescriptor` - Configuration for the subapp

### Frontend Components

1. **accessibility-checker.html** - Main UI interface for the accessibility checker
2. **accessibility-scanner.js** - Core scanning logic integrating axe-core
   - Loads pages in iframes
   - Runs axe-core analysis
   - Sends results to backend

### Data Storage

Scan results are stored in the `easya11y` workspace with the following structure:
```
/scanResults/
  /[page-path]/
    - scanId
    - pageUrl
    - pageTitle
    - scanDate
    - wcagLevel
    - score
    - violationCount
    - violations_[impact]
    - fullResults (JSON)
```

## Development

### Prerequisites
- Java 8 or higher
- Node.js 14 or higher
- Maven 3.6 or higher
- Magnolia CMS 6.2 or higher

### Building the Module

1. **Install dependencies**
   ```bash
   npm install
   ```

2. **Build frontend assets**
   ```bash
   npm run build
   ```

3. **Build the module**
   ```bash
   mvn clean package
   ```

### Development Mode

For frontend development with hot reload:
```bash
npm run dev
```

## Testing

### Run the build and lint
```bash
npm run build
mvn clean package
```

### Testing endpoints
Test scripts are available for verifying the REST endpoints:
```bash
node test-rest-endpoints.js
```

## API Reference

### Scan Initiation
```
POST /.rest/easya11y/scan/initiate
Content-Type: application/json

{
  "pagePath": "/path/to/page"
}
```

### Store Scan Results
```
POST /.rest/easya11y/scan/results
Content-Type: application/json

{
  "scanId": "uuid",
  "pagePath": "/path/to/page",
  "pageUrl": "https://...",
  "pageTitle": "Page Title",
  "wcagLevel": "AA",
  "axeResults": { ... }
}
```

### List Pages
```
GET /.rest/easya11y/pages?includeStatus=true
```

### List Scan Results
```
GET /.rest/easya11y/results?severity=critical&wcagLevel=AA
```

### Export Results
```
GET /.rest/easya11y/results/export/csv
```

## Configuration

The module uses the following configuration:

- **Workspace**: `easya11y` - Stores scan results
- **App**: Accessible via Magnolia AdminCentral app launcher
- **REST Endpoints**: Available under `/.rest/easya11y/*`

## WCAG Compliance Levels

The module tests for:
- WCAG 2.0 Level A
- WCAG 2.0 Level AA
- WCAG 2.1 Level A
- WCAG 2.1 Level AA
- Best practices

## Scoring Algorithm

The accessibility score is calculated based on:
- Critical violations: 10 points deduction
- Serious violations: 5 points deduction
- Moderate violations: 2 points deduction
- Minor violations: 1 point deduction

Score = 100 - (weighted violations / total elements * 100)

## Troubleshooting

### Pages not loading in scanner
- Check CORS settings on your Magnolia instance
- Verify the page URL is accessible from the browser
- Check browser console for errors

### Scan results not saving
- Verify the `easya11y` workspace exists
- Check Magnolia logs for permission errors
- Ensure the REST endpoints are properly configured

## Future Enhancements

- Scheduled automatic scanning
- Email notifications for critical issues
- Integration with CI/CD pipelines
- Customizable rule sets
- Historical trend analysis
- Multi-language support

## License

EasyA11y is open source software, licensed under the [Mozilla Public License 2.0](https://www.mozilla.org/en-US/MPL/2.0/).

You are free to use, modify, and distribute this software commercially or non-commercially. If you make any changes to EasyA11y’s source files, you must share those changes under the same license.

This strikes a balance between community collaboration and commercial use—perfect for agencies, freelancers, and accessibility advocates who want to contribute while maintaining flexibility in their own projects.

Brought to you by [Noice](https://noice.work), helping teams deliver accessible, performant websites faster.