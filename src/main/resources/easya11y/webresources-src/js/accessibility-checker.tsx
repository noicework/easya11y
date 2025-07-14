import React from 'react'
import ReactDOM from 'react-dom/client'
import { AppShell, ErrorBoundary } from '@components/AppShell'
import { AccessibilityChecker } from '@components/AccessibilityChecker'
import '@/css/globals.css'

const root = ReactDOM.createRoot(document.getElementById('root')!)

root.render(
  <React.StrictMode>
    <ErrorBoundary>
      <AppShell variant="checker">
        <AccessibilityChecker />
      </AppShell>
    </ErrorBoundary>
  </React.StrictMode>
)