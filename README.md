# Easy Accessibility (EasyA11y) - Magnolia CMS Accessibility Checker Module

## Overview

Easy Accessibility is a Magnolia CMS module that provides comprehensive accessibility testing for your website pages. It integrates axe-core to scan pages for WCAG compliance and accessibility issues, providing detailed reports and tracking improvements over time.

`easya11y` is used as the technical module/repo identifier (package names, workspace, endpoints).

## Release Notes

See `CHANGELOG.md`.

## Key Features

- Scan pages for accessibility issues (WCAG-focused) using axe-core.
- Store scan results in JCR (default).
- Browse results, filter, and export CSV reports.
- Server-side scanning support (useful for scheduled scans).

## Commercial Edition

For details about the commercial edition, please contact Noice (https://noice.net.au).

## Architecture

### Backend Components

1. **REST Endpoints**
   - `PageListEndpoint` - Lists pages from the website workspace with scan status
   - `AccessibilityScanEndpoint` - Initiates scans and stores results
   - `ScanResultsListEndpoint` - Lists and filters scan results, exports reports
   - `ConfigurationEndpoint` - Reads/writes module configuration (WCAG defaults, scheduling, notifications)

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

## Installation

### 1. Register the easya11y Workspace

Easy Accessibility requires a custom JCR workspace to store scan results and configuration. Add the following to your Magnolia `repositories.xml`:

**Add to `<RepositoryMapping>`:**
```xml
<Map name="easya11y" repositoryName="magnolia" workspaceName="easya11y" />
```

**Add to `<Repository name="magnolia">`:**
```xml
<workspace name="easya11y" />
```

Example location: `WEB-INF/config/default/repositories.xml`

### 2. Configure Storage (Optional)

By default, Easy Accessibility uses JCR storage. To explicitly configure this, create a `config.yaml` in your light module decorations:

```
your-light-module/
  decorations/
    easya11y/
      config.yaml
```

**config.yaml contents:**
```yaml
storageType: jcr
```

### 3. Restart Magnolia

After modifying `repositories.xml`, restart Magnolia to create the new workspace. The module's bootstrap files will initialize the required nodes (`/configuration` and `/scanResults`) on first startup.

## Development

### Prerequisites
- Java 17
- Node.js 18 (used by the Maven frontend build)
- Maven 3.6 or higher
- Magnolia CMS 6.4 or higher

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
node test-easya11y-endpoints.js
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

### "Error retrieving configuration: null" or 500 errors
- The `easya11y` workspace is not registered. Follow the Installation steps above to add the workspace to `repositories.xml`
- Restart Magnolia after modifying the repository configuration

### Pages not loading in scanner
- Check CORS settings on your Magnolia instance
- Verify the page URL is accessible from the browser
- Check browser console for errors

### Scan results not saving
- Verify the `easya11y` workspace exists in `repositories.xml`
- Check Magnolia logs for permission errors
- Ensure the REST endpoints are properly configured

## Future Enhancements

- Scheduled automatic scanning
- Email notifications for critical issues
- Integration with CI/CD pipelines
- Customizable rule sets
- Multi-language support

## License

Easy Accessibility is open source software, licensed under the [Mozilla Public License 2.0](https://www.mozilla.org/en-US/MPL/2.0/).

You are free to use, modify, and distribute this software commercially or non-commercially. If you make any changes to Easy Accessibility’s source files, you must share those changes under the same license.

This strikes a balance between community collaboration and commercial use—perfect for agencies, freelancers, and accessibility advocates who want to contribute while maintaining flexibility in their own projects.

## Main Contributor

Jake Tracey (`jake.tracey@noice.net.au`)

Brought to you by [Noice](https://noice.work), helping teams deliver accessible, performant websites faster.
