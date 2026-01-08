# Changelog

All notable changes to Easy Accessibility (EasyA11y) will be documented in this file.

The format is based on Keep a Changelog, and this project aims to follow Semantic Versioning.

## [Unreleased]

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
