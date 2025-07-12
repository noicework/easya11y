import { calculateScore } from '@lib/utils'
import { accessibilityService } from './accessibility.service'
import type { Page, WCAGLevel, ScanProgress } from '@types'

export class ScannerService {
  private iframeContainer: HTMLDivElement | null = null

  constructor() {
    // Clean up on page unload
    window.addEventListener('beforeunload', () => {
      this.cleanup()
    })
  }

  private initializeIframeContainer(): HTMLDivElement {
    if (!this.iframeContainer) {
      this.iframeContainer = document.createElement('div')
      this.iframeContainer.id = 'a11y-scanner-container'
      this.iframeContainer.style.cssText = `
        position: fixed;
        top: -9999px;
        left: -9999px;
        width: 1px;
        height: 1px;
        overflow: hidden;
        visibility: hidden;
        pointer-events: none;
      `
      document.body.appendChild(this.iframeContainer)
    }
    return this.iframeContainer
  }

  private cleanupIframe(iframe: HTMLIFrameElement): void {
    if (iframe && iframe.parentNode) {
      iframe.parentNode.removeChild(iframe)
    }
  }

  private cleanup(): void {
    if (this.iframeContainer && this.iframeContainer.parentNode) {
      this.iframeContainer.parentNode.removeChild(this.iframeContainer)
      this.iframeContainer = null
    }
  }

  private getWcagTags(level: WCAGLevel): string[] {
    const baseTags = ['best-practice']
    
    switch (level) {
      case 'A':
        return [...baseTags, 'wcag2a', 'wcag21a']
      case 'AA':
        return [...baseTags, 'wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']
      case 'AAA':
        return [...baseTags, 'wcag2a', 'wcag2aa', 'wcag2aaa', 'wcag21a', 'wcag21aa', 'wcag21aaa']
      default:
        return [...baseTags, 'wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']
    }
  }

  async scanPage(pageData: {
    scanId: string
    pagePath: string
    pageUrl: string
    pageTitle: string
    wcagLevel?: WCAGLevel
  }): Promise<any> {
    const { scanId, pagePath, pageUrl, pageTitle, wcagLevel = 'AA' } = pageData
    
    console.log('Starting background scan for:', pageUrl)
    
    const container = this.initializeIframeContainer()
    const iframe = document.createElement('iframe')
    iframe.id = `a11y-scan-${scanId}`
    iframe.style.cssText = `
      width: 1280px;
      height: 1024px;
      border: none;
      visibility: hidden;
    `
    iframe.setAttribute('aria-hidden', 'true')
    iframe.setAttribute('tabindex', '-1')
    
    try {
      container.appendChild(iframe)
      
      // Load the page in the iframe
      await new Promise<void>((resolve, reject) => {
        iframe.onload = () => resolve()
        iframe.onerror = () => reject(new Error('Failed to load page in iframe'))
        
        const timeout = setTimeout(() => {
          reject(new Error('Page load timeout'))
        }, 30000)
        
        iframe.addEventListener('load', () => {
          clearTimeout(timeout)
          resolve()
        }, { once: true })
        
        iframe.src = pageUrl
      })
      
      // Try to inject and run axe in the iframe
      try {
        const iframeDoc = iframe.contentDocument || iframe.contentWindow?.document
        
        if (!iframeDoc) {
          throw new Error('Cannot access iframe document')
        }
        
        // Inject axe-core script
        const script = iframeDoc.createElement('script')
        script.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.8.3/axe.min.js'
        
        await new Promise<void>((resolve, reject) => {
          script.onload = () => resolve()
          script.onerror = () => reject(new Error('Failed to load axe-core'))
          iframeDoc.head.appendChild(script)
        })
        
        // Wait for axe to initialize
        await this.delay(1000)
        
        // Run axe in the iframe context
        const wcagTags = this.getWcagTags(wcagLevel)
        const results = await (iframe.contentWindow as any).axe.run({
          runOnly: {
            type: 'tag',
            values: wcagTags
          },
          resultTypes: ['violations', 'passes', 'incomplete', 'inapplicable']
        })
        
        this.cleanupIframe(iframe)
        
        // Calculate score and prepare results
        const score = calculateScore(results)
        
        await accessibilityService.saveScanResults({
          scanId,
          pagePath,
          pageUrl,
          pageTitle,
          wcagLevel,
          score,
          axeResults: {
            ...results,
            toolOptions: {
              version: results.testEngine?.version || '4.8.3'
            }
          }
        })
        
        return { ...results, score }
        
      } catch (e) {
        console.error('Error during scan:', e)
        this.cleanupIframe(iframe)
        
        // Handle cross-origin restrictions
        const errorResult = {
          violations: [],
          passes: [],
          incomplete: [],
          inapplicable: [],
          timestamp: new Date().toISOString(),
          url: pageUrl,
          errorMessage: 'Cross-origin restrictions prevent background scanning.'
        }
        
        const errorScore = calculateScore(errorResult)
        
        await accessibilityService.saveScanResults({
          scanId,
          pagePath,
          pageUrl,
          pageTitle: pageTitle + ' (Cross-origin)',
          wcagLevel,
          score: errorScore,
          axeResults: errorResult
        })
        
        return { ...errorResult, score: errorScore }
      }
    } catch (error) {
      this.cleanupIframe(iframe)
      console.error('Scanner error:', error)
      throw error
    }
  }

  async scanMultiplePages(
    pages: Page[],
    wcagLevel: WCAGLevel = 'AA',
    progressCallback?: (progress: ScanProgress) => void
  ): Promise<any[]> {
    const results = []
    
    for (let i = 0; i < pages.length; i++) {
      const page = pages[i]
      
      if (progressCallback) {
        progressCallback({
          current: i + 1,
          total: pages.length,
          currentPage: page.title || page.path,
          percentage: Math.round(((i + 1) / pages.length) * 100)
        })
      }
      
      try {
        const pageData = await accessibilityService.initiateScan(page.path, wcagLevel)
        const scanResult = await this.scanPage({ ...pageData, wcagLevel })
        
        results.push({
          page: page.path,
          success: true,
          violationCount: scanResult.violations.length
        })
        
        await this.delay(1000)
        
      } catch (error) {
        console.error(`Error scanning ${page.path}:`, error)
        results.push({
          page: page.path,
          success: false,
          error: error instanceof Error ? error.message : 'Unknown error'
        })
      }
    }
    
    return results
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}

export const scannerService = new ScannerService()