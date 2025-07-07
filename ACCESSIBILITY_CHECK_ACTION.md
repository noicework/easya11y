# Run Accessibility Check Action

## Overview

The "Run Accessibility Check" action has been added to the Magnolia Pages app, allowing content editors to easily initiate accessibility scans on any page directly from the Pages app interface.

## How It Works

1. **Select a Page**: In the Pages app, select any page you want to check for accessibility issues.

2. **Run the Action**: Click the "Run Accessibility Check" button in the action bar. The action will:
   - Initiate a scan for the selected page
   - Generate a unique scan ID
   - Create a URL for the accessibility scanner

3. **Execute the Scan**: A notification will appear with a link. Click the link to open the accessibility scanner in a new tab, which will:
   - Load the page being scanned
   - Run axe-core accessibility tests
   - Display real-time results
   - Save the results to the Magnolia repository

## Technical Implementation

### Components Added:

1. **Action Definition** (`/src/main/resources/easya11y/decorations/pages-app/apps/pages-app.yaml`):
   - Decorates the Pages app to add the new action
   - Configures the action for both browser and detail subapps
   - Sets availability rules (only available for page nodes)

2. **Action Class** (`/src/main/java/work/noice/easya11y/actions/RunAccessibilityCheckAction.java`):
   - Handles the action execution
   - Calls the accessibility scan REST endpoint
   - Shows notifications to the user
   - Generates the scanner URL with all necessary parameters

3. **Integration Points**:
   - Uses the existing `AccessibilityScanEndpoint` for scan initiation
   - Leverages the existing accessibility scanner interface
   - Stores results in the `easya11y` workspace

## Configuration

The action is automatically available in the Pages app after module installation. No additional configuration is required.

## WCAG Compliance

By default, the action initiates scans at the WCAG 2.1 Level AA standard. This can be modified in the `RunAccessibilityCheckAction.java` file if different compliance levels are needed.