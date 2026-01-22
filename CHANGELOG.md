# Changelog

All notable changes to Easy Accessibility (EasyA11y) will be documented in this file.

The format is based on Keep a Changelog, and this project aims to follow Semantic Versioning.

## [Unreleased]

## [1.5.0] - 2026-01-22

### Highlights

- Universal context path support - works with any Magnolia deployment path out of the box
- Headless deployment support - can now connect to remote Magnolia instances
- Streamlined module - removed unused form builder components

### Changed

- **Dynamic Context Path Detection**: Removed hardcoded `/magnoliaAuthor` path. The module now automatically detects the Magnolia context path from:
  1. URL parameter passed by the Magnolia SubApp
  2. The `/.resources/` pattern in the URL
  - Works with any context path: `/author`, `/cms`, `/magnoliaPublic`, or custom paths

- **Headless Support**: Added support for headless deployments where the React app runs on a different domain
  - Set `window.MGNL_CONTEXT_PATH` to the full Magnolia URL (e.g., `https://cms.example.com/author`) before loading the app
  - The service automatically detects full URLs and uses them directly

### Removed

- **Form Builder Components**: Removed unused form builder dialogs, templates, and includes:
  - Dialogs: button, conditional, container, form-embed, hidden, options, row, text, form page
  - Templates: button, conditional, container, form-embed, hidden, options, row, text, form page
  - Includes: btnOutline, btnSize, btnText, btnType, btnVariant, class, css, link

### Upgrade Notes

- **No Breaking Changes**: This release is backwards compatible. Existing deployments will continue to work without changes.
- **Headless Deployments**: To connect to a remote Magnolia instance, set `window.MGNL_CONTEXT_PATH = 'https://your-magnolia-url/context'` before the app loads.

## [1.4.0] - 2026-01-13

### Highlights

- Full database storage support with PostgreSQL and MySQL for persistent scan history
- Historical trends and analytics for tracking accessibility improvements over time
- Improved settings organization and system configuration visibility

### Added

- **Database Storage**: Full support for PostgreSQL and MySQL databases via Flyway migrations
  - Automatic schema migrations on startup
  - Page UUID tracking for consistent historical data across page moves
  - Configurable via `config.yaml` in light module decorations
- **Historical Trends**: New "View History" feature to track accessibility scores over time
  - Score trend charts showing improvement or regression
  - Violation breakdown by severity over time
  - Filter by page path and date range
- **System Configuration Panel**: New read-only panel in settings showing:
  - Current storage type (JCR or database)
  - Database connection details
  - Test Connection button for verifying database connectivity
- **Quick Audit Modal**: Streamlined interface for running single page or bulk scans

### Changed

- **Settings Reorganization**:
  - Server-side scanning toggle moved to System Configuration card
  - System Configuration card moved to bottom of settings page
  - Scheduled Scanning only visible when server-side scanning is enabled
- **View History Button**: Now conditionally enabled based on database storage configuration
- **Improved UI Components**:
  - New Historical Modal and Quick Audit Modal components
  - Better separation of concerns in scan controls

### Removed

- **JIRA Integration**: Removed JIRA ticket creation feature
- **License Validation**: Removed license checks from all endpoints for simpler deployment

### Fixed

- Flyway PostgreSQL 15+ compatibility with proper service file merging in shaded JAR
- Database enabled flag now correctly handles string "true" values from API responses

### Upgrade Notes

- **Database Setup**: To enable historical features, configure database storage in your light module's `decorations/easya11y/config.yaml`. See `example-config.yaml` for reference.
- **Migration**: Flyway will automatically run migrations on first startup with database storage enabled.

## [1.3.0] - 2026-01-09

### Changed

- Upgraded to Magnolia 6.4 compatibility.

## [1.2.1] - 2026-01-08

### Highlights

- Magnolia baseline updated to Magnolia 6.4 (Jakarta) and Java 17.
- Configuration expanded for scheduled scans and email notifications.
- Improved scanning UX, results browsing, and exports.

### Added

- Magnolia 6.4 / Jakarta REST compatibility and Java 17 target/runtime.
- Expanded configuration surface (WCAG defaults, scheduling, notifications, server-side scanning).
- Better results browsing and export support.

### Changed

- Updated REST layer to Jakarta (`jakarta.ws.rs`) for Magnolia 6.4 compatibility.
- Updated Java target/runtime requirements to Java 17.
- Expanded configuration model and UI (WCAG version/level defaults, scheduling, server-side scanning, notifications).

### Fixed

- Improved context path handling for Magnolia deployments not mounted at `/magnolia`.
- More resilient endpoint error handling and response payload consistency.

### Upgrade Notes

- **Magnolia/Java**: Magnolia 6.4+ and Java 17 are required for this release.
- **Workspace**: Ensure the `easya11y` workspace mapping exists in `repositories.xml` (see `README.md`).

## [1.2.0] - 2025-07-16

### Added

- Improvements to scanning UI and REST endpoints, including support for dynamic Magnolia context paths.
