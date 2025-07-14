import type { Page, ScanInit, ScanResult, DetailedResult, WCAGLevel, Configuration } from '@types'

class AccessibilityService {
  private apiBase: string
  private configCache: Configuration | null = null
  private configCacheTime: number = 0
  private readonly CONFIG_CACHE_TTL = 60000 // 1 minute cache

  constructor() {
    const baseUrl = window.location.origin
    const pathParts = window.location.pathname.split('/')
    const contextPath = pathParts[1] === 'magnoliaAuthor' ? '/magnoliaAuthor' : ''
    this.apiBase = `${baseUrl}${contextPath}/.rest`
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
}

export const accessibilityService = new AccessibilityService()