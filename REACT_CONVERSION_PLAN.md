# React Conversion Plan for EasyA11y Accessibility Checker

## Overview
This document tracks the conversion of the EasyA11y accessibility checker from static HTML/JS to a modern React application using TypeScript, Vite, Tailwind CSS, and shadcn/ui components.

## Technology Stack
- **Build Tool**: Vite (replaced Webpack for better performance)
- **Framework**: React 18 with TypeScript
- **Styling**: Tailwind CSS + shadcn/ui components
- **State Management**: React Query for server state, React hooks for local state
- **Testing**: Vitest + React Testing Library + Playwright

## Progress Status

### ✅ Completed Tasks

#### Infrastructure Setup
- [x] Set up Vite build configuration
- [x] Configure TypeScript with proper paths
- [x] Set up Tailwind CSS with custom theme
- [x] Configure shadcn/ui base setup
- [x] Create project directory structure

#### Core Components
- [x] AppShell - Main application wrapper with QueryClient
- [x] ErrorBoundary - Error handling wrapper
- [x] StatsOverview - Statistics dashboard component
- [x] ScoreIndicator - Visual score representation
- [x] EmptyState - Placeholder for empty data
- [x] SearchBar - Global search functionality

#### Services & Hooks
- [x] AccessibilityService - API communication layer
- [x] ScannerService - Axe-core integration for scanning
- [x] usePages - React Query hook for pages data
- [x] useScanResults - React Query hooks for scan results
- [x] useScanner - Hook for scanning functionality

#### UI Components (shadcn/ui)
- [x] Button
- [x] Card
- [x] Dialog
- [x] Input
- [x] Select
- [x] Alert

#### Entry Points
- [x] accessibility-checker.tsx - Main app entry
- [x] accessibility-scan-dialog.tsx - Dialog app entry
- [x] AccessibilityChecker component - Main app component
- [x] AccessibilityScanDialog component - Dialog component

### 🚧 In Progress

#### Components
- [ ] PageSelector - Searchable dropdown with virtualization
- [ ] ScanControls - Main scanning interface
- [ ] ResultsTable - Paginated results display
- [ ] ViolationCard - Detailed violation display
- [ ] FilterPanel - Advanced filtering options
- [ ] ScanProgressDialog - Bulk scan progress
- [ ] ScanResultsModal - Detailed results modal

### 📋 Pending Tasks

#### Components
- [ ] Combobox component for PageSelector
- [ ] Progress component for scan progress
- [ ] Table component for results
- [ ] Collapsible component for filters
- [ ] Tooltip component for help text

#### Build & Integration
- [ ] Update Maven configuration to use Vite
- [ ] Test iframe integration with Magnolia CMS
- [ ] Bundle optimization for production

#### Testing
- [ ] Component unit tests
- [ ] Integration tests
- [ ] E2E tests with Playwright
- [ ] Accessibility tests with jest-axe

## Component Architecture

### 1. PageSelector Component
```typescript
interface PageSelectorProps {
  pages: Page[]
  selectedPage: Page | null
  onPageSelect: (page: Page) => void
  isLoading: boolean
}
```
**Features**:
- Combobox with search functionality
- Virtualized list for performance
- Recent pages section
- Keyboard navigation

### 2. ScanControls Component
```typescript
interface ScanControlsProps {
  pages: Page[]
  selectedPage: Page | null
  wcagLevel: WCAGLevel
  isLoading: boolean
  isScanning: boolean
  onPageSelect: (page: Page) => void
  onWcagLevelChange: (level: WCAGLevel) => void
  onScan: () => void
  onBulkScan: () => void
}
```
**Features**:
- Page selection
- WCAG level selector
- Scan button with loading state
- Bulk scan with confirmation

### 3. ResultsTable Component
```typescript
interface ResultsTableProps {
  results: ScanResult[]
  sortOrder: SortOrder
  onSortChange: (order: SortOrder) => void
  onResultClick: (result: ScanResult) => void
}
```
**Features**:
- Sortable columns
- Expandable rows
- Score visualization
- Violation badges

### 4. ViolationCard Component
```typescript
interface ViolationCardProps {
  violation: Violation
  expanded?: boolean
  onToggle?: () => void
}
```
**Features**:
- Impact level indicator
- WCAG tags display
- Affected elements list
- "Show more" functionality

### 5. FilterPanel Component
```typescript
interface FilterPanelProps {
  filters: FilterState
  onFiltersChange: (filters: FilterState) => void
}
```
**Features**:
- Severity filter
- WCAG level filter
- Clear filters option
- Applied filters display

### 6. Modal Components
```typescript
interface ScanProgressDialogProps {
  isOpen: boolean
  progress: ScanProgress | null
  onCancel: () => void
}

interface ScanResultsModalProps {
  isOpen: boolean
  result: ScanResult | null
  onClose: () => void
}
```

## Testing Strategy

### Unit Tests
- Component rendering
- User interactions
- Props validation
- Accessibility compliance

### Integration Tests
- API mocking with MSW
- State management flows
- Error handling

### E2E Tests
- Complete scan workflow
- Filter functionality
- Export functionality
- Keyboard navigation

## Migration Notes

### Key Improvements
1. **Performance**: Vite provides faster builds and HMR
2. **Type Safety**: Full TypeScript coverage
3. **Accessibility**: shadcn/ui components are WCAG compliant
4. **UX**: Better loading states, animations, and feedback
5. **Maintainability**: Component-based architecture

### Breaking Changes
- Removed jQuery dependency
- Changed from webpack to Vite
- New API structure with React Query

### Deployment Considerations
- Vite outputs to same directory structure
- HTML files updated to load React bundles
- CSS now includes Tailwind utilities
- Maintains compatibility with Magnolia iframe

## Next Steps

1. Complete remaining UI components
2. Implement comprehensive error handling
3. Add loading skeletons for better UX
4. Set up automated testing
5. Configure production build optimization
6. Document component usage
7. Create Storybook for component development

## Commands

```bash
# Development
npm run dev

# Build
npm run build

# Type checking
npm run type-check

# Linting
npm run lint

# Testing
npm run test
npm run test:e2e
```