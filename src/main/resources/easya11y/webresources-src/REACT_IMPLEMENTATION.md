# EasyA11y React Implementation

## Overview

This implementation provides a modern React-based interface for the EasyA11y accessibility checker module using the Magnolia Design System.

## Features

- **Comprehensive Scanning**: Uses axe-core to scan pages for WCAG compliance
- **Real-time Progress**: Shows scanning progress for multiple pages
- **Advanced Filtering**: Filter results by severity, WCAG level, and search terms
- **Detailed Reports**: View detailed violation information for each page
- **Export Functionality**: Export scan results as CSV
- **Responsive Design**: Works on desktop and mobile devices

## Components

### Main Application (`accessibility-checker.jsx`)

The main React component that provides:
- Page listing and management
- Scan initiation (single page or bulk)
- Results display with filtering and sorting
- Statistics dashboard
- Export functionality

### Scanner Module (`accessibility-scanner.js`)

The core scanning module that:
- Integrates with axe-core
- Manages iframe-based page scanning
- Handles scan queue for bulk operations
- Formats and sends results to backend

## Building

1. Install dependencies:
   ```bash
   npm install
   ```

2. Build for development:
   ```bash
   npm run dev
   ```

3. Build for production:
   ```bash
   npm run build
   ```

## Usage

The React app is available at:
- Development: `accessibility-checker-react.html`
- Production: Deploy the built files from `webresources/`

## Design System

This implementation uses the Magnolia Design System React components:
- ThemeProvider for consistent theming
- Card, Button, Select, TextField for UI elements
- Table components for data display
- Alert and Modal for user feedback
- Grid and Stack for layout

## API Integration

The app integrates with the following REST endpoints:
- `GET /easya11y/pages` - List pages with scan status
- `POST /easya11y/scan/initiate` - Start a scan
- `POST /easya11y/scan/results` - Store scan results
- `GET /easya11y/results` - List scan results
- `GET /easya11y/results/export/csv` - Export results

## Accessibility Features

- Keyboard navigation support
- ARIA labels and roles
- Focus management
- Screen reader friendly
- High contrast support

## Future Enhancements

- Real-time scan result updates via WebSocket
- Scheduled scanning
- Historical trend charts
- Custom rule configuration
- Integration with CI/CD pipelines