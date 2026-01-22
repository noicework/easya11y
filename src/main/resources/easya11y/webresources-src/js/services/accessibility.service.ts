import type { Page, ScanInit, ScanResult, DetailedResult, WCAGLevel, Configuration } from '@types'

class AccessibilityService {
  private apiBase: string
  private configCache: Configuration | null = null
  private configCacheTime: number = 0
  private readonly CONFIG_CACHE_TTL = 60000 // 1 minute cache

  constructor() {
    this.apiBase = this.resolveApiBase()
  }

  private resolveApiBase(): string {
    const magnoliaPath = (window as any).MGNL_CONTEXT_PATH

    // If full URL provided (headless setup), use it directly
    if (magnoliaPath?.startsWith('http')) {
      return magnoliaPath.endsWith('/.rest') ? magnoliaPath : `${magnoliaPath}/.rest`
    }

    // Otherwise combine context path with current origin
    const contextPath = magnoliaPath ?? this.detectContextPath()
    return `${window.location.origin}${contextPath}/.rest`
  }

  private detectContextPath(): string {
    // Try URL parameter first (passed by Magnolia SubApp)
    const urlParams = new URLSearchParams(window.location.search)
    const paramContext = urlParams.get('contextPath')
    if (paramContext) {
      return paramContext
    }

    // Extract from /.resources/ path pattern
    const pathname = window.location.pathname
    const resourcesIndex = pathname.indexOf('/.resources/')
    if (resourcesIndex > 0) {
      return pathname.substring(0, resourcesIndex)
    }

    // No context path detected
    return ''
  }

  async getPages(includeStatus = true): Promise<Page[]> {
    const response = await fetch(`${this.apiBase}/easya11y/pages?includeStatus=${includeStatus}`)
    if (!response.ok) {
      throw new Error(`Failed to load pages: ${response.status}`)
    }
    const data = await response.json()
    return data.items || []
  }

  async initiateScan(pagePath: string, wcagLevel: WCAGLevel = 'AA'): Promise<ScanInit> {
    const response = await fetch(`${this.apiBase}/easya11y/scan/initiate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ pagePath, wcagLevel })
    })
    
    if (!response.ok) {
      throw new Error(`Failed to initiate scan: ${response.status}`)
    }
    
    return response.json()
  }

  async saveScanResults(scanData: any): Promise<void> {
    const response = await fetch(`${this.apiBase}/easya11y/scan/results`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(scanData)
    })
    
    if (!response.ok) {
      throw new Error('Failed to save scan results')
    }
  }

  async getScanResults(): Promise<ScanResult[]> {
    const response = await fetch(`${this.apiBase}/easya11y/results`)
    
    if (!response.ok) {
      throw new Error(`Failed to load scan results: ${response.status}`)
    }
    
    const data = await response.json()
    
    // Handle different response formats
    if (Array.isArray(data)) {
      return data
    } else if (data.results) {
      return data.results
    }
    
    return []
  }

  async getDetailedResult(pagePath: string): Promise<DetailedResult> {
    const response = await fetch(
      `${this.apiBase}/easya11y/results/detail?pagePath=${encodeURIComponent(pagePath)}`
    )
    
    if (!response.ok) {
      throw new Error(`Failed to load detailed results: ${response.status}`)
    }
    
    const data = await response.json()
    return data.result || data
  }

  async exportResults(format: 'csv' | 'json' = 'csv', pagePath?: string): Promise<string> {
    let exportUrl = `${this.apiBase}/easya11y/results/export/${format}`
    if (pagePath) {
      exportUrl += `?pagePath=${encodeURIComponent(pagePath)}`
    }
    return exportUrl
  }

  async getConfiguration(): Promise<Configuration> {
    // Check cache first
    const now = Date.now()
    if (this.configCache && (now - this.configCacheTime) < this.CONFIG_CACHE_TTL) {
      return this.configCache
    }

    const response = await fetch(`${this.apiBase}/easya11y/configuration`)
    
    if (!response.ok) {
      throw new Error(`Failed to load configuration: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (data.success && data.configuration) {
      this.configCache = data.configuration
      this.configCacheTime = now
      return data.configuration
    }
    
    return {} as Configuration
  }

  async saveConfiguration(configuration: Configuration): Promise<void> {
    const response = await fetch(`${this.apiBase}/easya11y/configuration`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(configuration)
    })
    
    if (!response.ok) {
      throw new Error(`Failed to save configuration: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (!data.success) {
      throw new Error(data.message || 'Failed to save configuration')
    }
    
    // Clear cache on save
    this.configCache = null
  }

  async serverSideScan(pagePath: string, wcagLevel: WCAGLevel = 'AA'): Promise<{
    success: boolean
    scanId: string
    score: number
    violationCount: number
    pageUrl: string
    message: string
  }> {
    const response = await fetch(`${this.apiBase}/easya11y/scan/server`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ pagePath, wcagLevel })
    })
    
    if (!response.ok) {
      throw new Error(`Failed to perform server-side scan: ${response.status}`)
    }
    
    return response.json()
  }

  async shouldUseServerSideScan(): Promise<boolean> {
    try {
      const config = await this.getConfiguration()
      return config.serverSideScan === true || String(config.serverSideScan) === 'true'
    } catch (error) {
      console.warn('Failed to load configuration, defaulting to client-side scan', error)
      return false
    }
  }

  async getHistoricalTrends(pagePath?: string, days: number = 30): Promise<any> {
    const url = new URL(`${this.apiBase}/easya11y/results/trends`)
    if (pagePath) {
      url.searchParams.append('pagePath', pagePath)
    }
    url.searchParams.append('days', days.toString())
    
    const response = await fetch(url.toString())
    
    if (!response.ok) {
      throw new Error(`Failed to fetch trends: ${response.status}`)
    }
    
    return response.json()
  }

  async getSystemConfiguration(): Promise<SystemConfiguration> {
    const response = await fetch(`${this.apiBase}/easya11y/configuration/system`)

    if (!response.ok) {
      throw new Error(`Failed to load system configuration: ${response.status}`)
    }

    const data = await response.json()

    if (data.success && data.systemConfiguration) {
      return data.systemConfiguration
    }

    return {} as SystemConfiguration
  }

  async testDatabaseConnection(): Promise<{ success: boolean; message: string; storageType?: string }> {
    const response = await fetch(`${this.apiBase}/easya11y/configuration/test-connection`)

    if (!response.ok) {
      throw new Error(`Failed to test database connection: ${response.status}`)
    }

    return response.json()
  }
}

export interface SystemConfiguration {
  storageType: string
  databaseEnabled: boolean
  datasource?: {
    url: string
    username: string
    driver: string
    passwordConfigured: boolean
    migration?: {
      path: string
      runOnStartup: boolean
    }
  }
}

export const accessibilityService = new AccessibilityService()