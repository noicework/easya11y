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
                
                // Calculate score before sending
                const score = utils.calculateScore(formattedResults);
                
                // Send results
                await this.sendResults({
                    scanId,
                    pagePath,
                    pageUrl,
                    pageTitle,
                    wcagLevel: wcagLevel,
                    score: score,
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
                
                // Calculate score for error result (should be 100 since no violations)
                const errorScore = utils.calculateScore(errorResult);
                
                await this.sendResults({
                    scanId,
                    pagePath,
                    pageUrl,
                    pageTitle: pageTitle + ' (Cross-origin)',
                    wcagLevel: wcagLevel,
                    score: errorScore,
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
                const initResponse = await fetch(`${this.apiBase}/easya11y/scan/initiate`, {
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

// Common utility functions
export const utils = {
    // Escape HTML to prevent XSS
    escapeHtml: (text) => {
        if (text === null || text === undefined) return '';
        
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        
        return String(text).replace(/[&<>"']/g, m => map[m]);
    },
    
    // Debounce function for input handling
    debounce: (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },
    
    // Format date for display
    formatDate: (timestamp) => {
        if (!timestamp) return '-';
        
        const date = new Date(timestamp);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);
        
        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins} minute${diffMins !== 1 ? 's' : ''} ago`;
        if (diffHours < 24) return `${diffHours} hour${diffHours !== 1 ? 's' : ''} ago`;
        if (diffDays < 7) return `${diffDays} day${diffDays !== 1 ? 's' : ''} ago`;
        
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },
    
    // Get color based on score
    getScoreColor: (score) => {
        if (score >= 90) return '#28a745';
        if (score >= 70) return '#ffc107';
        return '#dc3545';
    },
    
    // Get color based on impact level
    getImpactColor: (impact) => {
        switch(impact) {
            case 'critical': return '#721c24';
            case 'serious': return '#e53e3e';
            case 'moderate': return '#ed8936';
            case 'minor': return '#ecc94b';
            default: return '#6c757d';
        }
    },
    
    // Calculate accessibility score based on violations
    calculateScore: (scanResult) => {
        if (!scanResult || !scanResult.violations) return 100;
        
        const violations = scanResult.violations;
        const totalElements = (scanResult.passes ? scanResult.passes.length : 0) + violations.length;
        
        if (totalElements === 0) return 100;
        
        // Weight violations by impact
        let weightedViolations = 0;
        violations.forEach(violation => {
            const weight = violation.impact === 'critical' ? 10 : 
                         violation.impact === 'serious' ? 5 : 
                         violation.impact === 'moderate' ? 2 : 1;
            weightedViolations += weight * violation.nodes.length;
        });
        
        const score = Math.max(0, Math.round(100 - (weightedViolations / totalElements) * 100));
        return score;
    },
    
    // Get score class for styling
    getScoreClass: (score) => {
        if (score >= 90) return 'score-good';
        if (score >= 70) return 'score-warning';
        return 'score-bad';
    }
};

// UI Component functions
export const uiComponents = {
    // Create a violation card element
    createViolationCard: (violation, asString = true) => {
        const violationId = 'violation-' + Math.random().toString(36).substr(2, 9);
        
        // Show first 3 elements
        const visibleElements = violation.nodes.slice(0, 3).map(node => 
            `<div class="element-item">${utils.escapeHtml(node.target || node.html || node.selector || '')}</div>`
        ).join('');
        
        // Hidden elements
        const hiddenElements = violation.nodes.length > 3 ? 
            violation.nodes.slice(3).map(node => 
                `<div class="element-item elements-collapsed" data-violation="${violationId}" style="display: none;">${utils.escapeHtml(node.target || node.html || node.selector || '')}</div>`
            ).join('') : '';
        
        // Show more link
        const showMoreLink = violation.nodes.length > 3 ? 
            `<span class="show-more-elements" data-violation="${violationId}" data-expanded="false" data-count="${violation.nodes.length - 3}">
                ... and ${violation.nodes.length - 3} more elements
            </span>` : '';
        
        const html = `
            <div class="violation-card">
                <div class="violation-header">
                    <div>
                        <div class="violation-title">${utils.escapeHtml(violation.help || violation.description)}</div>
                        <div class="violation-description">${utils.escapeHtml(violation.description)}</div>
                        <div class="violation-tags">
                            <span class="violation-count ${violation.impact}">${violation.impact}</span>
                            ${violation.tags ? violation.tags.map(tag => `<span class="wcag-tag">${tag}</span>`).join('') : ''}
                        </div>
                    </div>
                </div>
                ${violation.helpUrl ? `<a href="${violation.helpUrl}" target="_blank" class="help-link">Learn more →</a>` : ''}
                <div class="affected-elements">
                    <div class="affected-elements-title">Affected elements (${violation.nodes.length})</div>
                    ${visibleElements}
                    ${hiddenElements}
                    ${showMoreLink}
                </div>
            </div>
        `;
        
        if (!asString) {
            const div = document.createElement('div');
            div.innerHTML = html;
            return div.firstChild;
        }
        
        return html;
    },
    
    // Show a message to the user
    showMessage: (message, type = 'info', container = null) => {
        const messageContainer = container || document.getElementById('messageContainer');
        if (messageContainer) {
            messageContainer.innerHTML = `<div class="${type}">${message}</div>`;
        }
    },
    
    // Clear message
    clearMessage: (container = null) => {
        const messageContainer = container || document.getElementById('messageContainer');
        if (messageContainer) {
            messageContainer.innerHTML = '';
        }
    },
    
    // Setup show more/less functionality for violation cards
    setupViolationCardHandlers: (container) => {
        container.querySelectorAll('.show-more-elements').forEach(link => {
            link.addEventListener('click', function() {
                const violationId = this.getAttribute('data-violation');
                const hiddenElements = container.querySelectorAll(`.elements-collapsed[data-violation="${violationId}"]`);
                const isExpanded = this.getAttribute('data-expanded') === 'true';
                
                if (isExpanded) {
                    hiddenElements.forEach(el => el.style.display = 'none');
                    this.textContent = `... and ${this.getAttribute('data-count')} more elements`;
                    this.setAttribute('data-expanded', 'false');
                } else {
                    hiddenElements.forEach(el => el.style.display = 'block');
                    this.textContent = 'Show less';
                    this.setAttribute('data-expanded', 'true');
                }
            });
        });
    }
};

// Page initializers
export const pageInitializers = {
    // Initialize the accessibility checker page
    initAccessibilityChecker: () => {
        // Configuration
        const baseUrl = window.location.origin;
        const pathParts = window.location.pathname.split('/');
        const contextPath = pathParts[1] === 'magnoliaAuthor' ? '/magnoliaAuthor' : '';
        const apiBase = `${baseUrl}${contextPath}/.rest`;
        
        // State
        let allPages = [];
        let scanResults = [];
        let filteredResults = [];
        let currentPagePath = '';
        let currentPage = 1;
        const itemsPerPage = 20;
        let sortOrder = 'date-desc';
        let searchTerm = '';
        let severityFilter = '';
        let wcagFilter = '';
        let isScanning = false;
        let scanner = null;
        
        // DOM elements
        const pageSearchInput = document.getElementById('pageSearchInput');
        const pageDropdown = document.getElementById('pageDropdown');
        const sortSelect = document.getElementById('sortSelect');
        const searchInput = document.getElementById('searchInput');
        const scanBtn = document.getElementById('scanBtn');
        const scanAllBtn = document.getElementById('scanAllBtn');
        const exportBtn = document.getElementById('exportBtn');
        const filterBtn = document.getElementById('filterBtn');
        const filters = document.getElementById('filters');
        const severityFilterSelect = document.getElementById('severityFilter');
        const wcagFilterSelect = document.getElementById('wcagFilter');
        const wcagLevelSelect = document.getElementById('wcagLevelSelect');
        const applyFiltersBtn = document.getElementById('applyFilters');
        const submissionsContainer = document.getElementById('submissionsContainer');
        const messageContainer = document.getElementById('messageContainer');
        const statsContainer = document.getElementById('statsContainer');
        const headerStats = document.getElementById('headerStats');
        const pagination = document.getElementById('pagination');
        
        // Initialize scanner
        if (window.AccessibilityScanner) {
            scanner = new window.AccessibilityScanner(apiBase);
            console.log('Accessibility scanner initialized');
        } else {
            console.error('AccessibilityScanner not found. Make sure the script is loaded.');
        }
        
        // Load pages function
        async function loadPages() {
            try {
                const response = await fetch(`${apiBase}/easya11y/pages?includeStatus=true`);
                
                if (!response.ok) {
                    throw new Error(`Failed to load pages: ${response.status}`);
                }
                
                const data = await response.json();
                allPages = data.items || [];
                updateStats();
            } catch (error) {
                uiComponents.showMessage(`Error loading pages: ${error.message}`, 'error');
                console.error('Error loading pages:', error);
            }
        }
        
        // Populate searchable dropdown
        function populatePageDropdown(pages = allPages) {
            pageDropdown.innerHTML = '';
            
            if (pages.length === 0) {
                pageDropdown.innerHTML = '<div class=\"no-results\">No pages found</div>';
                return;
            }
            
            pages.forEach((page, index) => {
                const item = document.createElement('div');
                item.className = 'page-dropdown-item';
                item.dataset.path = page.path;
                item.dataset.index = index;
                
                item.innerHTML = `
                    <div class=\"page-title\">${utils.escapeHtml(page.title || page.name || page.path)}</div>
                    <div class=\"page-path\">${utils.escapeHtml(page.path)}</div>
                `;
                
                item.addEventListener('click', () => {
                    selectPage(page);
                });
                
                pageDropdown.appendChild(item);
            });
        }
        
        // Show page dropdown
        function showPageDropdown() {
            populatePageDropdown();
            pageDropdown.style.display = 'block';
        }
        
        // Hide page dropdown
        function hidePageDropdown() {
            pageDropdown.style.display = 'none';
            pageDropdown.querySelectorAll('.highlighted').forEach(el => {
                el.classList.remove('highlighted');
            });
        }
        
        // Filter page dropdown
        function filterPageDropdown(searchTerm) {
            const filtered = allPages.filter(page => {
                const searchText = `${page.title || ''} ${page.name || ''} ${page.path}`.toLowerCase();
                return searchText.includes(searchTerm.toLowerCase());
            });
            
            populatePageDropdown(filtered);
        }
        
        // Select a page
        function selectPage(page) {
            currentPagePath = page.path;
            pageSearchInput.value = page.title || page.name || page.path;
            pageSearchInput.dataset.selectedPath = page.path;
            hidePageDropdown();
            currentPage = 1;
            updateScanButtonState();
        }
        
        // Handle keyboard navigation
        function handleDropdownKeyNavigation(e) {
            const items = pageDropdown.querySelectorAll('.page-dropdown-item');
            const highlighted = pageDropdown.querySelector('.highlighted');
            let currentIndex = highlighted ? parseInt(highlighted.dataset.index) : -1;
            
            switch(e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    if (currentIndex < items.length - 1) {
                        currentIndex++;
                        highlightItem(items[currentIndex]);
                    }
                    break;
                case 'ArrowUp':
                    e.preventDefault();
                    if (currentIndex > 0) {
                        currentIndex--;
                        highlightItem(items[currentIndex]);
                    }
                    break;
                case 'Enter':
                    e.preventDefault();
                    if (highlighted) {
                        const page = allPages.find(p => p.path === highlighted.dataset.path);
                        if (page) selectPage(page);
                    }
                    break;
                case 'Escape':
                    e.preventDefault();
                    hidePageDropdown();
                    break;
            }
        }
        
        // Highlight dropdown item
        function highlightItem(item) {
            pageDropdown.querySelectorAll('.highlighted').forEach(el => {
                el.classList.remove('highlighted');
            });
            item.classList.add('highlighted');
            item.scrollIntoView({ block: 'nearest' });
        }
        
        // Load scan results
        async function loadScanResults() {
            try {
                uiComponents.showMessage('Loading scan results...', 'info');
                
                const response = await fetch(`${apiBase}/easya11y/results`);
                
                if (!response.ok) {
                    throw new Error(`Failed to load scan results: ${response.status}`);
                }
                
                const data = await response.json();
                
                // Handle different response formats
                if (Array.isArray(data)) {
                    scanResults = data;
                } else if (data.results) {
                    scanResults = data.results;
                } else {
                    scanResults = [];
                }
                
                filterAndDisplayResults();
                updateStats();
                uiComponents.clearMessage();
            } catch (error) {
                uiComponents.showMessage(`Error loading scan results: ${error.message}`, 'error');
                console.error('Error loading scan results:', error);
            }
        }
        
        // Scan a single page
        async function scanPage(pagePath) {
            if (!scanner) {
                uiComponents.showMessage('Scanner not initialized', 'error');
                return;
            }
            
            if (isScanning) {
                uiComponents.showMessage('A scan is already in progress', 'info');
                return;
            }
            
            try {
                isScanning = true;
                updateScanButtonState();
                uiComponents.showMessage('Initiating accessibility scan...', 'info');
                
                // Get selected WCAG level
                const wcagLevel = wcagLevelSelect.value || 'AA';
                
                // Initiate scan through backend
                const initResponse = await fetch(`${apiBase}/easya11y/scan/initiate`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ 
                        pagePath,
                        wcagLevel 
                    })
                });
                
                if (!initResponse.ok) {
                    throw new Error(`Failed to initiate scan: ${initResponse.status}`);
                }
                
                const pageData = await initResponse.json();
                uiComponents.showMessage(`Scanning ${pageData.pageTitle}...`, 'info');
                
                // Perform the actual scan with WCAG level
                const scanResult = await scanner.scanPage({
                    ...pageData,
                    wcagLevel: wcagLevel
                });
                
                uiComponents.showMessage(`Scan completed for ${pageData.pageTitle}`, 'success');
                
                // Wait a moment for the backend to process the result
                await new Promise(resolve => setTimeout(resolve, 1000));
                
                // Clear the page filter to show all results
                currentPagePath = '';
                pageSearchInput.value = '';
                pageSearchInput.dataset.selectedPath = '';
                
                // Reload the scan results to ensure list is updated
                await loadScanResults();
                
                // Try to show modal with scan results directly
                if (scanResult && scanResult.violations) {
                    // Build a result object from the axe scan results
                    const modalData = {
                        pageTitle: pageData.pageTitle,
                        pagePath: pagePath,
                        score: utils.calculateScore(scanResult),
                        violationCount: scanResult.violations.length,
                        violations: scanResult.violations,
                        violations_critical: scanResult.violations.filter(v => v.impact === 'critical').length,
                        violations_serious: scanResult.violations.filter(v => v.impact === 'serious').length,
                        violations_moderate: scanResult.violations.filter(v => v.impact === 'moderate').length,
                        violations_minor: scanResult.violations.filter(v => v.impact === 'minor').length,
                        passes: scanResult.passes
                    };
                    showScanResultsModal(modalData);
                    
                    // Don't reload immediately - wait for modal to close
                    return;
                }
                
            } catch (error) {
                uiComponents.showMessage(`Error scanning page: ${error.message}`, 'error');
                console.error('Error scanning page:', error);
            } finally {
                isScanning = false;
                updateScanButtonState();
            }
        }
        
        // Scan all pages
        async function scanAllPages() {
            if (!scanner) {
                uiComponents.showMessage('Scanner not initialized', 'error');
                return;
            }
            
            if (isScanning) {
                uiComponents.showMessage('A scan is already in progress', 'info');
                return;
            }
            
            try {
                isScanning = true;
                updateScanButtonState();
                
                // Get selected WCAG level
                const wcagLevel = wcagLevelSelect.value || 'AA';
                
                // Create progress modal
                const progressModal = createProgressModal();
                let cancelRequested = false;
                
                document.getElementById('cancelScanBtn').addEventListener('click', () => {
                    cancelRequested = true;
                });
                
                // Scan all pages with WCAG level
                const results = await scanner.scanMultiplePages(
                    allPages,
                    wcagLevel,
                    (progress) => {
                        progressModal.update(progress);
                        if (cancelRequested) {
                            throw new Error('Scan cancelled by user');
                        }
                    }
                );
                
                progressModal.close();
                
                const successCount = results.filter(r => r.success).length;
                uiComponents.showMessage(`Scan completed: ${successCount} of ${results.length} pages scanned successfully`, 'success');
                
                // Reload results
                await loadScanResults();
                
            } catch (error) {
                document.querySelector('.scan-progress-modal')?.remove();
                uiComponents.showMessage(`Error during bulk scan: ${error.message}`, 'error');
                console.error('Error during bulk scan:', error);
            } finally {
                isScanning = false;
                updateScanButtonState();
            }
        }
        
        // Filter and display scan results
        function filterAndDisplayResults() {
            filteredResults = scanResults.filter(result => {
                // Search filter
                if (searchTerm) {
                    const searchableText = `${result.pageTitle} ${result.pagePath} ${JSON.stringify(result.violations || [])}`.toLowerCase();
                    if (!searchableText.includes(searchTerm)) {
                        return false;
                    }
                }
                
                // Page path filter
                if (currentPagePath && result.pagePath !== currentPagePath) {
                    return false;
                }
                
                // Severity filter
                if (severityFilter) {
                    // Check if the result has violations of the specified severity
                    const hasSeverity = 
                        (severityFilter === 'critical' && result.criticalCount > 0) ||
                        (severityFilter === 'serious' && result.seriousCount > 0) ||
                        (severityFilter === 'moderate' && result.moderateCount > 0) ||
                        (severityFilter === 'minor' && result.minorCount > 0);
                    if (!hasSeverity) {
                        return false;
                    }
                }
                
                // WCAG filter
                if (wcagFilter && result.wcagLevel !== wcagFilter) {
                    return false;
                }
                
                return true;
            });
            
            sortAndDisplayResults();
        }
        
        // Sort and display scan results
        function sortAndDisplayResults() {
            // Sort results
            filteredResults.sort((a, b) => {
                switch (sortOrder) {
                    case 'score-asc':
                        return (a.score || 0) - (b.score || 0);
                    case 'score-desc':
                        return (b.score || 0) - (a.score || 0);
                    case 'date-desc':
                        return new Date(b.scanDate || 0) - new Date(a.scanDate || 0);
                    case 'violations-desc':
                        return (b.violationCount || 0) - (a.violationCount || 0);
                    default:
                        return 0;
                }
            });
            
            currentPage = 1;
            displayResults();
            updateStats();
        }
        
        // Display scan results with pagination
        function displayResults() {
            if (!filteredResults || filteredResults.length === 0) {
                submissionsContainer.innerHTML = `
                    <div class=\"empty-state\">
                        <svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\">
                            <circle cx=\"12\" cy=\"12\" r=\"10\"></circle>
                            <path d=\"M12 16v-4\"></path>
                            <path d=\"M12 8h.01\"></path>
                        </svg>
                        <p>${searchTerm || severityFilter || wcagFilter ? 'No scan results match your filters' : 'No scan results found. Select a page and click \"Scan Page\" to start.'}</p>
                    </div>
                `;
                pagination.style.display = 'none';
                return;
            }
            
            const totalPagesCount = Math.ceil(filteredResults.length / itemsPerPage);
            const startIndex = (currentPage - 1) * itemsPerPage;
            const endIndex = startIndex + itemsPerPage;
            const pageResults = filteredResults.slice(startIndex, endIndex);
            
            submissionsContainer.innerHTML = pageResults.map(result => {
                const scoreClass = result.score >= 90 ? 'score-good' : result.score >= 70 ? 'score-warning' : 'score-bad';
                const criticalCount = result.criticalCount || result.violations_critical || 0;
                const seriousCount = result.seriousCount || result.violations_serious || 0;
                const moderateCount = result.moderateCount || result.violations_moderate || 0;
                const minorCount = result.minorCount || result.violations_minor || 0;
                
                return `
                    <div class=\"submission scan-result ${scoreClass}\" onclick=\"window.toggleResult(this)\" data-page-path=\"${result.pagePath}\">
                        <div class=\"submission-header\">
                            <div class=\"submission-meta\">
                                <div class=\"page-info\">
                                    <strong>${result.pageTitle || result.pagePath}</strong>
                                    <span class=\"page-path\">${result.pagePath}</span>
                                </div>
                                <div class=\"scan-info\">
                                    <span class=\"submission-date\">Scanned ${utils.formatDate(result.scanDate)}</span>
                                    <span class=\"wcag-level\">WCAG ${result.wcagLevel}</span>
                                    <span class=\"score-badge ${scoreClass}\">Score: ${result.score ? result.score.toFixed(1) : '0.0'}</span>
                                </div>
                            </div>
                            <div class=\"submission-expand\">⌄</div>
                        </div>
                        <div class=\"submission-preview\">
                            <div class=\"violation-summary\">
                                ${criticalCount > 0 ? `<span class=\"violation-count critical\">Critical: ${criticalCount}</span>` : ''}
                                ${seriousCount > 0 ? `<span class=\"violation-count serious\">Serious: ${seriousCount}</span>` : ''}
                                ${moderateCount > 0 ? `<span class=\"violation-count moderate\">Moderate: ${moderateCount}</span>` : ''}
                                ${minorCount > 0 ? `<span class=\"violation-count minor\">Minor: ${minorCount}</span>` : ''}
                                ${result.violationCount === 0 ? '<span class=\"violation-count success\">✓ No violations found</span>' : ''}
                            </div>
                        </div>
                        <div class=\"submission-fields scan-details\" data-loaded=\"false\">
                            <div class=\"loading\">Loading detailed results...</div>
                        </div>
                    </div>
                `;
            }).join('');
            
            // Update pagination
            if (totalPagesCount > 1) {
                pagination.style.display = 'flex';
                document.getElementById('currentPage').textContent = currentPage;
                document.getElementById('totalPages').textContent = totalPagesCount;
                document.getElementById('prevPage').disabled = currentPage === 1;
                document.getElementById('nextPage').disabled = currentPage === totalPagesCount;
            } else {
                pagination.style.display = 'none';
            }
        }
        
        // Toggle result expansion and load details if needed
        window.toggleResult = async function(element) {
            element.classList.toggle('expanded');
            
            const detailsContainer = element.querySelector('.scan-details');
            if (element.classList.contains('expanded') && detailsContainer.dataset.loaded === 'false') {
                const pagePath = element.dataset.pagePath;
                await loadDetailedResults(pagePath, detailsContainer);
            }
        };
        
        // Load detailed scan results for a specific page
        async function loadDetailedResults(pagePath, container) {
            try {
                const response = await fetch(`${apiBase}/easya11y/results/detail?pagePath=${encodeURIComponent(pagePath)}`);
                
                if (!response.ok) {
                    throw new Error(`Failed to load detailed results: ${response.status}`);
                }
                
                const data = await response.json();
                container.dataset.loaded = 'true';
                
                // Handle different response structures
                const result = data.result || data;
                const violations = result.violations || result.fullResults?.violations || [];
                
                if (!violations || violations.length === 0) {
                    // Check if there are no violations
                    if (result.violationCount === 0 || result.score === 100) {
                        container.innerHTML = '<p class=\"violation-count success\">✓ No accessibility violations found!</p>';
                    } else {
                        container.innerHTML = '<p>No violation details available.</p>';
                    }
                    return;
                }
                
                container.innerHTML = violations.map(violation => {
                    const card = uiComponents.createViolationCard(violation);
                    return card;
                }).join('');
                
                // Setup handlers for show more/less
                uiComponents.setupViolationCardHandlers(container);
                
            } catch (error) {
                container.innerHTML = `<p class=\"error\">Error loading details: ${error.message}</p>`;
                console.error('Error loading detailed results:', error);
            }
        }
        
        // Update statistics
        function updateStats() {
            if (scanResults && scanResults.length > 0) {
                statsContainer.style.display = 'grid';
                
                // Total pages
                document.getElementById('totalPages').textContent = allPages.length;
                
                // Scanned pages
                document.getElementById('scannedPages').textContent = scanResults.length;
                
                // Average score
                const avgScore = scanResults.reduce((sum, r) => sum + (r.score || 0), 0) / scanResults.length;
                document.getElementById('averageScore').textContent = avgScore.toFixed(1);
                
                // Critical issues
                const criticalCount = scanResults.reduce((sum, r) => sum + (r.criticalCount || r.violations_critical || 0), 0);
                document.getElementById('criticalIssues').textContent = criticalCount;
                
                // Update header stats
                const scannedPercent = Math.round((scanResults.length / allPages.length) * 100) || 0;
                headerStats.textContent = `${scanResults.length} of ${allPages.length} pages scanned (${scannedPercent}%)`;
            } else {
                statsContainer.style.display = 'none';
                headerStats.textContent = 'No pages scanned yet';
            }
        }
        
        // Export report as CSV
        function exportReport() {
            let exportUrl = `${apiBase}/easya11y/results/export/csv`;
            if (currentPagePath) {
                exportUrl += `?pagePath=${encodeURIComponent(currentPagePath)}`;
            }
            window.open(exportUrl, '_blank');
        }
        
        // Update scan button state
        function updateScanButtonState() {
            const selectedPath = pageSearchInput.dataset.selectedPath || '';
            scanBtn.disabled = isScanning || !selectedPath || !currentPagePath;
            scanAllBtn.disabled = isScanning;
            
            if (isScanning) {
                scanBtn.innerHTML = '<span>⟳</span> Scanning...';
                scanAllBtn.innerHTML = '<span>⟳</span> Scanning...';
            } else {
                scanBtn.innerHTML = '<span>▶</span> Scan Page';
                scanAllBtn.innerHTML = '<span>⊞</span> Scan All';
            }
        }
        
        // Show scan results modal
        function showScanResultsModal(scanResult) {
            try {
                const modal = document.getElementById('scanResultsModal');
                const body = document.getElementById('scanResultsBody');
                
                if (!modal || !body) {
                    console.error('Modal elements not found');
                    return;
                }
                
                // Calculate stats
                const violationCount = scanResult.violations?.length || 0;
                const passCount = scanResult.passes?.length || 0;
                const totalIssues = scanResult.violations?.reduce((sum, v) => sum + (v.nodes?.length || 0), 0) || 0;
                
                // Build modal content using similar structure as scan dialog
                let html = `
                <div class=\"scan-summary\">
                    <div class=\"scan-score\">
                        <div class=\"score-circle-container\">
                            <div class=\"score-circle\">
                                <svg width=\"80\" height=\"80\">
                                    <circle cx=\"40\" cy=\"40\" r=\"32\" class=\"score-circle-bg\"></circle>
                                    <circle cx=\"40\" cy=\"40\" r=\"32\" class=\"score-circle-progress\" 
                                            style=\"stroke: ${utils.getScoreColor(scanResult.score)}; 
                                                   stroke-dasharray: ${2 * Math.PI * 32}; 
                                                   stroke-dashoffset: ${2 * Math.PI * 32 * (1 - scanResult.score / 100)}\"></circle>
                                </svg>
                                <div class=\"score-text\" style=\"color: ${utils.getScoreColor(scanResult.score)}\">${scanResult.score}</div>
                            </div>
                        </div>
                        <div class=\"score-details\">
                            <div class=\"score-label\">Page: ${utils.escapeHtml(scanResult.pageTitle)}</div>
                            <div class=\"stats-row\">
                                <span>Violations: <strong>${violationCount}</strong></span>
                                <span>Passed Rules: <strong>${passCount}</strong></span>
                                <span>Total Issues: <strong>${totalIssues}</strong></span>
                            </div>
                        </div>
                    </div>
                    <div class=\"violation-summary\">
                        ${scanResult.violations_critical > 0 ? `<span class=\"violation-count critical\">Critical: ${scanResult.violations_critical}</span>` : ''}
                        ${scanResult.violations_serious > 0 ? `<span class=\"violation-count serious\">Serious: ${scanResult.violations_serious}</span>` : ''}
                        ${scanResult.violations_moderate > 0 ? `<span class=\"violation-count moderate\">Moderate: ${scanResult.violations_moderate}</span>` : ''}
                        ${scanResult.violations_minor > 0 ? `<span class=\"violation-count minor\">Minor: ${scanResult.violations_minor}</span>` : ''}
                        ${scanResult.violationCount === 0 ? `<span class=\"violation-count success\">No violations found!</span>` : ''}
                    </div>
                </div>
            `;
            
                // Add violations details if any
                if (scanResult.violations && scanResult.violations.length > 0) {
                    html += '<div class=\"scan-violations\"><h4>Accessibility Issues</h4><div class=\"violations-list\">';
                    
                    // Group violations by impact
                    const violationsByImpact = {
                        critical: [],
                        serious: [],
                        moderate: [],
                        minor: []
                    };
                    
                    scanResult.violations.forEach(v => {
                        if (violationsByImpact[v.impact]) {
                            violationsByImpact[v.impact].push(v);
                        }
                    });
                    
                    // Display violations by impact level
                    ['critical', 'serious', 'moderate', 'minor'].forEach(impact => {
                        violationsByImpact[impact].forEach(violation => {
                            html += uiComponents.createViolationCard(violation);
                        });
                    });
                    
                    html += '</div></div>';
                } else {
                    html += `
                        <div class=\"success-message\">
                            <strong>Great job!</strong> No accessibility violations found.
                        </div>
                    `;
                }
                
                body.innerHTML = html;
                modal.classList.add('active');
                
                // Setup handlers for show more/less
                uiComponents.setupViolationCardHandlers(body);
                
            } catch (error) {
                console.error('Error showing scan results modal:', error);
            }
        }
        
        window.closeScanResultsModal = function() {
            const modal = document.getElementById('scanResultsModal');
            modal.classList.remove('active');
            
            // Clear the page filter to show all results
            currentPagePath = '';
            pageSearchInput.value = '';
            pageSearchInput.dataset.selectedPath = '';
            
            // Refresh the scan results list with a small delay to ensure the backend has indexed the new result
            setTimeout(() => {
                loadScanResults();
            }, 500);
        };
        
        // Setup event listeners
        function setupEventListeners() {
            // Searchable dropdown events
            pageSearchInput.addEventListener('focus', () => {
                showPageDropdown();
            });
            
            pageSearchInput.addEventListener('input', utils.debounce((e) => {
                filterPageDropdown(e.target.value);
            }, 200));
            
            pageSearchInput.addEventListener('keydown', (e) => {
                handleDropdownKeyNavigation(e);
            });
            
            // Close dropdown when clicking outside
            document.addEventListener('click', (e) => {
                if (!e.target.closest('.searchable-dropdown')) {
                    hidePageDropdown();
                }
            });
            
            sortSelect.addEventListener('change', (e) => {
                sortOrder = e.target.value;
                sortAndDisplayResults();
            });
            
            searchInput.addEventListener('input', utils.debounce((e) => {
                searchTerm = e.target.value.toLowerCase();
                filterAndDisplayResults();
            }, 300));
            
            scanBtn.addEventListener('click', () => {
                if (currentPagePath && scanner) {
                    scanPage(currentPagePath);
                }
            });
            
            scanAllBtn.addEventListener('click', () => {
                if (confirm('This will scan all pages. This may take a while. Continue?')) {
                    scanAllPages();
                }
            });
            
            exportBtn.addEventListener('click', () => {
                exportReport();
            });
            
            filterBtn.addEventListener('click', () => {
                filters.classList.toggle('active');
            });
            
            applyFiltersBtn.addEventListener('click', () => {
                severityFilter = severityFilterSelect.value;
                wcagFilter = wcagFilterSelect.value;
                filterAndDisplayResults();
            });
            
            document.getElementById('prevPage').addEventListener('click', () => {
                if (currentPage > 1) {
                    currentPage--;
                    displayResults();
                }
            });
            
            document.getElementById('nextPage').addEventListener('click', () => {
                const totalPagesCount = Math.ceil(filteredResults.length / itemsPerPage);
                if (currentPage < totalPagesCount) {
                    currentPage++;
                    displayResults();
                }
            });
        }
        
        // Initialize
        console.log('Accessibility Checker initialized');
        console.log('Context path:', contextPath);
        console.log('API base:', apiBase);
        
        loadPages();
        loadScanResults();
        setupEventListeners();
        
        // Check if autoRun parameter is present
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('autoRun') === 'true') {
            console.log('Auto-run mode detected, starting scan automatically...');
            // Wait a bit for everything to load, then start scan
            setTimeout(() => {
                if (currentPagePath) {
                    startScan();
                }
            }, 1000);
        }
    },
    
    // Initialize the scan dialog page
    initScanDialog: () => {
        // Parse URL parameters
        const urlParams = new URLSearchParams(window.location.search);
        const pageUrl = urlParams.get('pageUrl');
        const scanId = urlParams.get('scanId');
        const pagePath = urlParams.get('pagePath');
        const pageTitle = urlParams.get('pageTitle') || 'Unknown Page';
        const wcagLevel = urlParams.get('wcagLevel') || 'AA';
        
        // Get context path
        const baseUrl = window.location.origin;
        const pathParts = window.location.pathname.split('/');
        const contextPath = pathParts[1] === 'magnoliaAuthor' ? '/magnoliaAuthor' : '';
        const apiBase = `${baseUrl}${contextPath}/.rest`;
        
        // Initialize scanner
        let scanner = null;
        
        // DOM elements
        const scanStatusEl = document.getElementById('scanStatus');
        const resultsContainerEl = document.getElementById('resultsContainer');
        const errorMessageEl = document.getElementById('errorMessage');
        
        // Display results function
        function displayResults(results) {
            // Hide scanning status completely
            scanStatusEl.style.display = 'none';
            scanStatusEl.classList.add('hidden');
            resultsContainerEl.classList.remove('hidden');
            
            // Calculate score using the same method as main scanner
            const score = utils.calculateScore(results);
            
            const scoreCircle = document.getElementById('scoreCircle');
            const scoreText = document.getElementById('scoreText');
            const scoreValue = document.getElementById('scoreValue');
            
            // Set score color based on value
            let scoreColor = utils.getScoreColor(score);
            
            scoreCircle.style.stroke = scoreColor;
            scoreText.style.color = scoreColor;
            scoreText.textContent = score;
            
            // Update score value display
            if (scoreValue) {
                scoreValue.textContent = `${score}/100`;
            }
            
            // Animate score circle (radius is now 32)
            const circumference = 2 * Math.PI * 32;
            scoreCircle.style.strokeDasharray = circumference;
            scoreCircle.style.strokeDashoffset = circumference - (score / 100) * circumference;
            
            // Display stats
            const totalIssues = results.violations?.reduce((sum, v) => sum + (v.nodes?.length || 0), 0) || 0;
            const violationCount = results.violations?.length || 0;
            const passCount = results.passes?.length || 0;
            
            // Count violations by impact
            const criticalCount = results.violations?.filter(v => v.impact === 'critical').length || 0;
            const seriousCount = results.violations?.filter(v => v.impact === 'serious').length || 0;
            const moderateCount = results.violations?.filter(v => v.impact === 'moderate').length || 0;
            const minorCount = results.violations?.filter(v => v.impact === 'minor').length || 0;
            
            document.getElementById('violationCount').textContent = violationCount;
            document.getElementById('passCount').textContent = passCount;
            document.getElementById('issueCount').textContent = totalIssues;
            
            // Display violation summary
            const violationSummaryEl = document.getElementById('violationSummary');
            let summaryHtml = '';
            if (criticalCount > 0) summaryHtml += `<span class="violation-count critical">Critical: ${criticalCount}</span>`;
            if (seriousCount > 0) summaryHtml += `<span class="violation-count serious">Serious: ${seriousCount}</span>`;
            if (moderateCount > 0) summaryHtml += `<span class="violation-count moderate">Moderate: ${moderateCount}</span>`;
            if (minorCount > 0) summaryHtml += `<span class="violation-count minor">Minor: ${minorCount}</span>`;
            if (violationCount === 0) summaryHtml = `<span class="violation-count success">✓ No violations found</span>`;
            violationSummaryEl.innerHTML = summaryHtml;
            
            // Display violations
            if (results.violations && results.violations.length > 0) {
                displayViolations(results.violations);
            } else {
                document.getElementById('violationsSection').innerHTML = `
                    <h4>Accessibility Issues</h4>
                    <div class=\"success-message\">
                        <strong>Great job!</strong> No accessibility violations found.
                    </div>
                `;
            }
        }
        
        // Display violations function
        function displayViolations(violations) {
            const violationsList = document.getElementById('violationsList');
            
            // Group by impact
            const byImpact = {
                critical: violations.filter(v => v.impact === 'critical'),
                serious: violations.filter(v => v.impact === 'serious'),
                moderate: violations.filter(v => v.impact === 'moderate'),
                minor: violations.filter(v => v.impact === 'minor')
            };
            
            // Clear and populate violations
            violationsList.innerHTML = '';
            
            // Display violations by impact level
            ['critical', 'serious', 'moderate', 'minor'].forEach(impact => {
                byImpact[impact].forEach(violation => {
                    const cardHtml = uiComponents.createViolationCard(violation);
                    violationsList.insertAdjacentHTML('beforeend', cardHtml);
                });
            });
            
            // Setup handlers for show more/less
            uiComponents.setupViolationCardHandlers(violationsList);
        }
        
        // Show error function
        function showError(message) {
            scanStatusEl.style.display = 'none';
            scanStatusEl.classList.add('hidden');
            errorMessageEl.textContent = message;
            errorMessageEl.classList.remove('hidden');
        }
        
        // Start scan function
        async function startScan() {
            try {
                const results = await scanner.scanPage({
                    scanId: scanId,
                    pagePath: pagePath,
                    pageUrl: pageUrl,
                    pageTitle: pageTitle,
                    wcagLevel: wcagLevel
                });
                displayResults(results);
            } catch (error) {
                console.error('Scan error:', error);
                showError('Scan failed: ' + error.message);
            }
        }
        
        // Initialize scanner and start scan
        if (window.AccessibilityScanner) {
            scanner = new window.AccessibilityScanner(apiBase);
            startScan();
        } else {
            showError('Scanner initialization failed');
        }
    }
};

// Also expose to window for HTML usage
if (typeof window !== 'undefined') {
    window.AccessibilityScanner = AccessibilityScanner;
    window.createProgressModal = createProgressModal;
    window.utils = utils;
    window.uiComponents = uiComponents;
    window.pageInitializers = pageInitializers;
}