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
    
    // Check if we should use server-side scanning
    const useServerSide = await accessibilityService.shouldUseServerSideScan()
    
    if (useServerSide) {
      console.log('Using server-side scan for:', pageUrl)
      try {
        const serverResult = await accessibilityService.serverSideScan(pagePath, wcagLevel)
        
        // Get the detailed results that were just saved
        const detailedResult = await accessibilityService.getDetailedResult(pagePath)
        
        return {
          violations: detailedResult.fullResults?.violations || detailedResult.violations || [],
          passes: detailedResult.fullResults?.passes || detailedResult.passes || [],
          incomplete: detailedResult.fullResults?.incomplete || detailedResult.incomplete || [],
          inapplicable: detailedResult.fullResults?.inapplicable || detailedResult.inapplicable || [],
          timestamp: detailedResult.fullResults?.timestamp || new Date().toISOString(),
          url: pageUrl,
          score: detailedResult.score || serverResult.score
        }
      } catch (error) {
        console.error('Server-side scan failed:', error)
        throw error
      }
    }
    
    console.log('Starting client-side background scan for:', pageUrl)
    
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
    progressCallback?: (progress: ScanProgress) => void,
    concurrentBatchSize: number = 3
  ): Promise<any[]> {
    const results: any[] = []
    const completedCount = { value: 0 }
    
    // Process pages in batches
    for (let i = 0; i < pages.length; i += concurrentBatchSize) {
      const batch = pages.slice(i, i + concurrentBatchSize)
      
      // Scan pages in the current batch concurrently
      const batchPromises = batch.map(async (page) => {
        try {
          // Check if we should use server-side scanning
          const useServerSide = await accessibilityService.shouldUseServerSideScan()
          
          let scanResult
          if (useServerSide) {
            // For server-side scan, we don't need to initiate first
            const serverResult = await accessibilityService.serverSideScan(page.path, wcagLevel)
            const detailedResult = await accessibilityService.getDetailedResult(page.path)
            scanResult = {
              violations: detailedResult.fullResults?.violations || [],
              score: serverResult.score
            }
          } else {
            // For client-side scan, use the normal flow
            const pageData = await accessibilityService.initiateScan(page.path, wcagLevel)
            scanResult = await this.scanPage({ ...pageData, wcagLevel })
          }
          
          // Update progress for each completed page
          completedCount.value++
          if (progressCallback) {
            progressCallback({
              current: completedCount.value,
              total: pages.length,
              currentPage: page.title || page.path,
              percentage: Math.round((completedCount.value / pages.length) * 100)
            })
          }
          
          return {
            page: page.path,
            success: true,
            violationCount: scanResult.violations.length
          }
        } catch (error) {
          console.error(`Error scanning ${page.path}:`, error)
          
          // Update progress even for failed pages
          completedCount.value++
          if (progressCallback) {
            progressCallback({
              current: completedCount.value,
              total: pages.length,
              currentPage: page.title || page.path,
              percentage: Math.round((completedCount.value / pages.length) * 100)
            })
          }
          
          return {
            page: page.path,
            success: false,
            error: error instanceof Error ? error.message : 'Unknown error'
          }
        }
      })
      
      // Wait for all pages in the batch to complete
      const batchResults = await Promise.all(batchPromises)
      results.push(...batchResults)
      
      // Add a small delay between batches to avoid overwhelming the system
      if (i + concurrentBatchSize < pages.length) {
        await this.delay(500)
      }
    }
    
    return results
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}

export const scannerService = new ScannerService()