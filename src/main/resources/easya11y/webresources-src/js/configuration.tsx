import React from 'react'
import { createRoot } from 'react-dom/client'
import { AppShell } from '@components/AppShell'
import { ConfigurationApp } from '@components/ConfigurationApp'
import '../css/globals.css'

const container = document.getElementById('root')
if (container) {
  const root = createRoot(container)
  root.render(
    <React.StrictMode>
      <AppShell>
        <ConfigurationApp />
      </AppShell>
    </React.StrictMode>
  )
}