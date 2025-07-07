/**
 * Simplified Accessibility Scanner Module
 * Uses a different approach for axe-core integration
 */

import axe from 'axe-core';

class AccessibilityScanner {
    constructor(apiBase) {
        this.apiBase = apiBase;
        this.isScanning = false;
        this.scanQueue = [];
        this.currentScanId = null;
        this.iframeContainer = null;
        
        // Clean up on page unload
        window.addEventListener('beforeunload', () => {
            this.cleanup();
        });
    }

    /**
     * Initialize iframe container for background scanning
     */
    initializeIframeContainer() {
        if (!this.iframeContainer) {
            // Create a hidden container for iframes
            this.iframeContainer = document.createElement('div');
            this.iframeContainer.id = 'a11y-scanner-container';
            this.iframeContainer.style.cssText = `
                position: fixed;
                top: -9999px;
                left: -9999px;
                width: 1px;
                height: 1px;
                overflow: hidden;
                visibility: hidden;
                pointer-events: none;
            `;
            document.body.appendChild(this.iframeContainer);
        }
        return this.iframeContainer;
    }

    /**
     * Clean up iframe after scanning
     */
    cleanupIframe(iframe) {
        if (iframe && iframe.parentNode) {
            iframe.parentNode.removeChild(iframe);
        }
    }

    /**
     * Clean up all resources
     */
    cleanup() {
        if (this.iframeContainer && this.iframeContainer.parentNode) {
            this.iframeContainer.parentNode.removeChild(this.iframeContainer);
            this.iframeContainer = null;
        }
    }

    /**
     * Get WCAG tags based on the selected level
     */
    getWcagTags(level) {
        const baseTags = ['best-practice'];
        
        switch (level) {
            case 'A':
                return [...baseTags, 'wcag2a', 'wcag21a'];
            case 'AA':
                return [...baseTags, 'wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'];
            case 'AAA':
                return [...baseTags, 'wcag2a', 'wcag2aa', 'wcag2aaa', 'wcag21a', 'wcag21aa', 'wcag21aaa'];
            default:
                // Default to AA
                return [...baseTags, 'wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'];
        }
    }

    /**
     * Scan a single page for accessibility issues
     */
    async scanPage(pageData) {
        const { scanId, pagePath, pageUrl, pageTitle, wcagLevel = 'AA' } = pageData;
        
        console.log('Starting background scan for:', pageUrl);
        
        // Initialize container for hidden iframes
        const container = this.initializeIframeContainer();
        
        // Create hidden iframe with unique ID
        const iframe = document.createElement('iframe');
        iframe.id = `a11y-scan-${scanId}`;
        iframe.style.cssText = `
            width: 1280px;
            height: 1024px;
            border: none;
            visibility: hidden;
        `;
        iframe.setAttribute('aria-hidden', 'true');
        iframe.setAttribute('tabindex', '-1');
        
        try {
            container.appendChild(iframe);
            
            // Load the page in the iframe
            await new Promise((resolve, reject) => {
                iframe.onload = resolve;
                iframe.onerror = () => reject(new Error('Failed to load page in iframe'));
                
                // Set timeout for page load
                const timeout = setTimeout(() => {
                    reject(new Error('Page load timeout'));
                }, 30000); // 30 second timeout
                
                iframe.addEventListener('load', () => {
                    clearTimeout(timeout);
                    resolve();
                }, { once: true });
                
                iframe.src = pageUrl;
            });
            
            // Try to inject and run axe in the iframe
            try {
                // Check if we can access the iframe (same-origin)
                const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                
                // Inject axe-core script
                const script = iframeDoc.createElement('script');
                script.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.8.3/axe.min.js';
                
                await new Promise((resolve, reject) => {
                    script.onload = resolve;
                    script.onerror = reject;
                    iframeDoc.head.appendChild(script);
                });
                
                // Wait a bit for axe to initialize
                await this.delay(1000);
                
                // Determine which WCAG tags to use based on level
                const wcagTags = this.getWcagTags(wcagLevel);
                
                // Run axe in the iframe context
                const results = await iframe.contentWindow.axe.run({
                    runOnly: {
                        type: 'tag',
                        values: wcagTags
                    },
                    resultTypes: ['violations', 'passes', 'incomplete', 'inapplicable']
                });
                
                // Clean up iframe
                this.cleanupIframe(iframe);
                
                // Prepare results in the format the backend expects
                const formattedResults = {
                    ...results,
                    toolOptions: {
                        version: results.testEngine?.version || '4.8.3'
                    }
                };
                
                // Send results
                await this.sendResults({
                    scanId,
                    pagePath,
                    pageUrl,
                    pageTitle,
                    wcagLevel: 'AA',
                    axeResults: formattedResults
                });
                
                return results;
                
            } catch (e) {
                console.error('Error during scan:', e);
                
                // Clean up iframe
                this.cleanupIframe(iframe);
                
                // If we can't access the iframe (cross-origin), we need a different approach
                // For cross-origin pages, we might need server-side scanning
                console.warn('Cross-origin page detected. Unable to scan in background.');
                
                // For now, return a message indicating cross-origin issue
                const errorResult = {
                    violations: [],
                    passes: [],
                    incomplete: [],
                    inapplicable: [],
                    timestamp: new Date().toISOString(),
                    url: pageUrl,
                    errorMessage: 'Cross-origin restrictions prevent background scanning. Consider implementing server-side scanning for external pages.'
                };
                
                await this.sendResults({
                    scanId,
                    pagePath,
                    pageUrl,
                    pageTitle: pageTitle + ' (Cross-origin)',
                    wcagLevel: 'AA',
                    axeResults: errorResult
                });
                
                return errorResult;
            }
        } catch (error) {
            // Clean up iframe on error
            this.cleanupIframe(iframe);
            console.error('Scanner error:', error);
            throw error;
        }
    }

    /**
     * Scan multiple pages in sequence
     */
    async scanMultiplePages(pages, wcagLevel = 'AA', progressCallback) {
        this.scanQueue = [...pages];
        const results = [];
        
        for (let i = 0; i < pages.length; i++) {
            const page = pages[i];
            
            if (progressCallback) {
                progressCallback({
                    current: i + 1,
                    total: pages.length,
                    currentPage: page.title || page.path,
                    percentage: Math.round(((i + 1) / pages.length) * 100)
                });
            }
            
            try {
                // Initiate scan for this page
                const initResponse = await fetch(`${this.apiBase}/scan/initiate`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ 
                        pagePath: page.path,
                        wcagLevel 
                    })
                });
                
                if (!initResponse.ok) {
                    throw new Error(`Failed to initiate scan for ${page.path}`);
                }
                
                const pageData = await initResponse.json();
                
                // Perform the scan with WCAG level
                const scanResult = await this.scanPage({
                    ...pageData,
                    wcagLevel
                });
                results.push({
                    page: page.path,
                    success: true,
                    violationCount: scanResult.violations.length
                });
                
                // Small delay between scans
                await this.delay(1000);
                
            } catch (error) {
                console.error(`Error scanning ${page.path}:`, error);
                results.push({
                    page: page.path,
                    success: false,
                    error: error.message
                });
            }
        }
        
        return results;
    }

    /**
     * Send scan results to the backend
     */
    async sendResults(scanData) {
        const response = await fetch(`${this.apiBase}/easya11y/scan/results`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(scanData)
        });
        
        if (!response.ok) {
            throw new Error('Failed to save scan results');
        }
        
        return response.json();
    }

    /**
     * Utility delay function
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Format axe results for display
     */
    formatResults(axeResults) {
        const summary = {
            violations: {
                total: axeResults.violations.length,
                byImpact: {
                    critical: 0,
                    serious: 0,
                    moderate: 0,
                    minor: 0
                },
                byWCAG: {}
            },
            passes: axeResults.passes.length,
            incomplete: axeResults.incomplete.length,
            inapplicable: axeResults.inapplicable.length
        };

        // Count violations by impact
        axeResults.violations.forEach(violation => {
            if (summary.violations.byImpact[violation.impact]) {
                summary.violations.byImpact[violation.impact]++;
            }
            
            // Count by WCAG criteria
            violation.tags.forEach(tag => {
                if (tag.startsWith('wcag')) {
                    if (!summary.violations.byWCAG[tag]) {
                        summary.violations.byWCAG[tag] = 0;
                    }
                    summary.violations.byWCAG[tag]++;
                }
            });
        });

        return summary;
    }

    /**
     * Generate a detailed report of violations
     */
    generateViolationReport(violations) {
        return violations.map(violation => ({
            id: violation.id,
            impact: violation.impact,
            description: violation.description,
            help: violation.help,
            helpUrl: violation.helpUrl,
            wcagCriteria: violation.tags.filter(tag => tag.startsWith('wcag')),
            affectedElements: violation.nodes.map(node => ({
                selector: node.target[0] || node.target,
                html: node.html,
                failureSummary: node.failureSummary
            }))
        }));
    }
}

// Create singleton instance
let scannerInstance = null;

export function createScanner(apiBase) {
    if (!scannerInstance) {
        scannerInstance = new AccessibilityScanner(apiBase);
    }
    return scannerInstance;
}

export function getScanner() {
    if (!scannerInstance) {
        throw new Error('Scanner not initialized. Call createScanner first.');
    }
    return scannerInstance;
}

// UI Helper functions
export function createProgressModal() {
    const modal = document.createElement('div');
    modal.className = 'scan-progress-modal';
    modal.innerHTML = `
        <div class="scan-progress-content">
            <h3>Scanning Pages</h3>
            <div class="progress-info">
                <span id="scanProgressText">Initializing...</span>
            </div>
            <div class="progress-bar">
                <div id="scanProgressBar" class="progress-bar-fill"></div>
            </div>
            <div class="progress-stats">
                <span id="scanProgressStats">0 of 0 pages</span>
            </div>
            <button id="cancelScanBtn" class="btn btn-secondary">Cancel</button>
        </div>
    `;
    
    // Add styles
    const style = document.createElement('style');
    style.textContent = `
        .scan-progress-modal {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
        }
        
        .scan-progress-content {
            background: white;
            padding: 30px;
            border-radius: 8px;
            min-width: 400px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        }
        
        .scan-progress-content h3 {
            margin: 0 0 20px 0;
            color: #333;
        }
        
        .progress-info {
            margin-bottom: 10px;
            color: #666;
        }
        
        .progress-bar {
            width: 100%;
            height: 20px;
            background: #f0f0f0;
            border-radius: 10px;
            overflow: hidden;
            margin-bottom: 10px;
        }
        
        .progress-bar-fill {
            height: 100%;
            background: #068449;
            width: 0%;
            transition: width 0.3s ease;
        }
        
        .progress-stats {
            text-align: center;
            color: #666;
            font-size: 0.9em;
            margin-bottom: 20px;
        }
        
        #cancelScanBtn {
            width: 100%;
        }
    `;
    
    document.head.appendChild(style);
    document.body.appendChild(modal);
    
    return {
        modal,
        update: (progress) => {
            document.getElementById('scanProgressText').textContent = 
                `Scanning: ${progress.currentPage}`;
            document.getElementById('scanProgressBar').style.width = 
                `${progress.percentage}%`;
            document.getElementById('scanProgressStats').textContent = 
                `${progress.current} of ${progress.total} pages`;
        },
        close: () => {
            modal.remove();
            style.remove();
        }
    };
}

// Export axe for direct use if needed
export { axe };

// Default export for backwards compatibility
export default AccessibilityScanner;

// Also expose to window for HTML usage
if (typeof window !== 'undefined') {
    window.AccessibilityScanner = AccessibilityScanner;
    window.createProgressModal = createProgressModal;
}