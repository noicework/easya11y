import type { Page, ScanInit, ScanResult, DetailedResult, FilterState, WCAGLevel } from '@types/index'

class AccessibilityService {
  private apiBase: string

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
}

export const accessibilityService = new AccessibilityService()