import React from 'react'
import ReactDOM from 'react-dom/client'
import { AppShell, ErrorBoundary } from '@components/AppShell'
import { AccessibilityScanDialog } from '@components/AccessibilityScanDialog'
import '@/css/globals.css'

const root = ReactDOM.createRoot(document.getElementById('root')!)

root.render(
  <React.StrictMode>
    <ErrorBoundary>
      <AppShell variant="dialog">
        <AccessibilityScanDialog />
      </AppShell>
    </ErrorBoundary>
  </React.StrictMode>
)