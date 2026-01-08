import { useState, useMemo } from 'react'
import { StatsOverview } from '@components/StatsOverview'
import { QuickAuditPanel } from '@components/QuickAuditPanel'
import { RecentScansPanel } from '@components/RecentScansPanel'
import { ScanProgressDialog } from '@components/ScanProgressDialog'
import { ScanResultsModal } from '@components/ScanResultsModal'
import { usePages } from '@hooks/usePages'
import { useScanResults } from '@hooks/useScanResults'
import { useScanner } from '@hooks/useScanner'
import { accessibilityService } from '@services/accessibility.service'
import { calculateScore } from '@lib/utils'
import type { Page, ScanResult, FilterState, SortOrder, WCAGLevel } from '@types'

export function AccessibilityChecker() {
  // State
  const [selectedPage, setSelectedPage] = useState<Page | null>(null)
  const [wcagLevel, setWcagLevel] = useState<WCAGLevel>('AA')
  const [searchTerm, setSearchTerm] = useState('')
  const [filters, setFilters] = useState<FilterState>({})
  const [sortOrder] = useState<SortOrder>('date-desc')
  const [showFilters, setShowFilters] = useState(false)
  const [showProgressDialog, setShowProgressDialog] = useState(false)
  const [showResultsModal, setShowResultsModal] = useState(false)
  const [modalResult, setModalResult] = useState<ScanResult | null>(null)
  const [scanSubpages, setScanSubpages] = useState<boolean>(false)

  // Queries
  const { data: pages = [], isLoading: pagesLoading } = usePages()
  const { data: scanResults = [], isLoading: resultsLoading, refetch: refetchResults } = useScanResults()
  
  // Scanner
  const { scanPage, scanAllPages, isScanning, scanProgress } = useScanner()

  // Calculate stats
  const stats = useMemo(() => {
    const scannedPages = scanResults.length
    const averageScore = scannedPages > 0
      ? scanResults.reduce((sum, r) => sum + (r.score || 0), 0) / scannedPages
      : 0
    const criticalIssues = scanResults.reduce((sum, r) => 
      sum + (r.criticalCount || r.violations_critical || 0), 0
    )

    return {
      totalPages: pages.length,
      scannedPages,
      averageScore,
      criticalIssues,
    }
  }, [pages, scanResults])

  // Filter and sort results
  const filteredResults = useMemo(() => {
    let results = [...scanResults]

    // Apply search filter
    if (searchTerm) {
      results = results.filter(result => {
        const searchableText = `${result.pageTitle} ${result.pagePath}`.toLowerCase()
        return searchableText.includes(searchTerm.toLowerCase())
      })
    }

    // Apply severity filter
    if (filters.severity) {
      results = results.filter(result => {
        const counts = {
          critical: result.criticalCount || result.violations_critical || 0,
          serious: result.seriousCount || result.violations_serious || 0,
          moderate: result.moderateCount || result.violations_moderate || 0,
          minor: result.minorCount || result.violations_minor || 0,
        }
        return counts[filters.severity as keyof typeof counts] > 0
      })
    }

    // Apply WCAG filter
    if (filters.wcagLevel) {
      results = results.filter(result => result.wcagLevel === filters.wcagLevel)
    }

    // Sort results
    results.sort((a, b) => {
      switch (sortOrder) {
        case 'score-asc':
          return (a.score || 0) - (b.score || 0)
        case 'score-desc':
          return (b.score || 0) - (a.score || 0)
        case 'date-desc':
          return new Date(b.scanDate || 0).getTime() - new Date(a.scanDate || 0).getTime()
        case 'violations-desc':
          return (b.violationCount || 0) - (a.violationCount || 0)
        default:
          return 0
      }
    })

    return results
  }, [scanResults, searchTerm, filters, sortOrder])

  // Handlers
  const handleScan = async () => {
    if (!selectedPage) return

    try {
      const result = await scanPage(selectedPage.path, wcagLevel)
      await refetchResults()
      
      // Show results modal if scan succeeded
      if (result && !result.errorMessage) {
        const score = result.score ?? calculateScore(result)
        const newResult: ScanResult = {
          scanId: Date.now().toString(),
          pagePath: selectedPage.path,
          pageUrl: selectedPage.url || '',
          pageTitle: selectedPage.title || selectedPage.name || selectedPage.path,
          scanDate: new Date().toISOString(),
          wcagLevel,
          score: score,
          violationCount: result.violations?.length || 0,
          violations: result.violations,
          passes: result.passes,
          criticalCount: result.violations?.filter((v: any) => v.impact === 'critical').length || 0,
          seriousCount: result.violations?.filter((v: any) => v.impact === 'serious').length || 0,
          moderateCount: result.violations?.filter((v: any) => v.impact === 'moderate').length || 0,
          minorCount: result.violations?.filter((v: any) => v.impact === 'minor').length || 0,
        }
        setModalResult(newResult)
        setShowResultsModal(true)
      }
    } catch (error) {
      console.error('Scan failed:', error)
    }
  }

  const handleBulkScan = async () => {
    setShowProgressDialog(true)
    try {
      await scanAllPages(pages, wcagLevel)
      await refetchResults()
    } finally {
      setShowProgressDialog(false)
    }
  }

  const handleExport = async () => {
    const exportUrl = await accessibilityService.exportResults('csv')
    window.open(exportUrl, '_blank')
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-2">
        <div>
          <p className="text-lg text-muted-foreground">
            Scan and monitor accessibility compliance across your website
          </p>
        </div>
      </div>

      {/* Stats Overview */}
      <StatsOverview stats={stats} />

      {/* Top Section - Quick Audit */}
      <div className="grid grid-cols-1 gap-6">
        <QuickAuditPanel
          pages={pages}
          selectedPage={selectedPage}
          wcagLevel={wcagLevel}
          isLoading={pagesLoading}
          isScanning={isScanning}
          onPageSelect={setSelectedPage}
          onWcagLevelChange={setWcagLevel}
          onScan={handleScan}
          onBulkScan={handleBulkScan}
          scanSubpages={scanSubpages}
          onScanSubpagesChange={setScanSubpages}
        />
      </div>

      {/* Bottom Section - Recent Scans (Full Width) */}
      <RecentScansPanel
        results={scanResults}
        filteredResults={filteredResults}
        searchTerm={searchTerm}
        filters={filters}
        showFilters={showFilters}
        onSearchChange={setSearchTerm}
        onFiltersChange={setFilters}
        onToggleFilters={() => setShowFilters(!showFilters)}
        onResultClick={(result) => {
          setModalResult(result)
          setShowResultsModal(true)
        }}
        onExport={handleExport}
        isLoading={resultsLoading}
      />

      {/* Modals */}
      <ScanProgressDialog
        isOpen={showProgressDialog}
        progress={scanProgress}
        onCancel={() => setShowProgressDialog(false)}
      />
      
      <ScanResultsModal
        isOpen={showResultsModal}
        result={modalResult}
        onClose={() => {
          setShowResultsModal(false)
          setModalResult(null)
        }}
      />
    </div>
  )
}
