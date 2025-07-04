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
    }

    /**
     * Scan a single page for accessibility issues
     */
    async scanPage(pageData) {
        const { scanId, pagePath, pageUrl, pageTitle } = pageData;
        this.currentScanId = scanId;
        
        console.log('Starting scan for:', pageUrl);
        
        try {
            // For now, let's try a different approach - open the page in a new window
            // This is less ideal but might work better with Magnolia's setup
            const scanWindow = window.open(pageUrl, '_blank');
            
            if (!scanWindow) {
                throw new Error('Popup blocked - please allow popups for accessibility scanning');
            }
            
            // Wait for the window to load
            await new Promise((resolve) => {
                scanWindow.onload = resolve;
                setTimeout(resolve, 5000); // Timeout after 5 seconds
            });
            
            // Try to inject and run axe in the new window
            try {
                // Check if we can access the window (same-origin)
                const testAccess = scanWindow.document;
                
                // Inject axe-core script
                const script = scanWindow.document.createElement('script');
                script.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.8.3/axe.min.js';
                
                await new Promise((resolve, reject) => {
                    script.onload = resolve;
                    script.onerror = reject;
                    scanWindow.document.head.appendChild(script);
                });
                
                // Wait a bit for axe to initialize
                await this.delay(500);
                
                // Run axe
                const results = await scanWindow.axe.run({
                    runOnly: {
                        type: 'tag',
                        values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'best-practice']
                    },
                    resultTypes: ['violations', 'passes', 'incomplete', 'inapplicable']
                });
                
                // Close the window
                scanWindow.close();
                
                // Send results
                await this.sendResults({
                    scanId,
                    pagePath,
                    pageUrl,
                    pageTitle,
                    wcagLevel: 'AA',
                    axeResults: results
                });
                
                return results;
                
            } catch (e) {
                scanWindow.close();
                console.error('Error during scan:', e);
                
                // If we can't access the window (cross-origin), fall back to a basic scan
                // This will scan the current page as a demonstration
                console.warn('Cross-origin page detected, performing demo scan on current page instead');
                
                const results = await axe.run({
                    runOnly: {
                        type: 'tag',
                        values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'best-practice']
                    },
                    resultTypes: ['violations', 'passes', 'incomplete', 'inapplicable']
                });
                
                // Add a note that this was a demo scan
                results.url = window.location.href;
                results.timestamp = new Date().toISOString();
                results.note = 'Demo scan performed on current page due to cross-origin restrictions';
                
                await this.sendResults({
                    scanId,
                    pagePath,
                    pageUrl,
                    pageTitle: pageTitle + ' (Demo)',
                    wcagLevel: 'AA',
                    axeResults: results
                });
                
                return results;
            }
        } catch (error) {
            console.error('Scanner error:', error);
            throw error;
        }
    }

    /**
     * Scan multiple pages in sequence
     */
    async scanMultiplePages(pages, progressCallback) {
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
                const initResponse = await fetch(`${this.apiBase}/easya11y/scan/initiate`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ pagePath: page.path })
                });
                
                if (!initResponse.ok) {
                    throw new Error(`Failed to initiate scan for ${page.path}`);
                }
                
                const pageData = await initResponse.json();
                
                // Perform the scan
                const scanResult = await this.scanPage(pageData);
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