import React, { useState, useMemo } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { StatsOverview } from '@components/StatsOverview'
import { ScanControls } from '@components/ScanControls'
import { ResultsTable } from '@components/ResultsTable'
import { SearchBar } from '@components/SearchBar'
import { FilterPanel } from '@components/FilterPanel'
import { ScanProgressDialog } from '@components/ScanProgressDialog'
import { ScanResultsModal } from '@components/ScanResultsModal'
import { NoResultsEmptyState } from '@components/EmptyState'
import { Button } from '@components/ui/button'
import { Download, Filter } from 'lucide-react'
import { usePages } from '@hooks/usePages'
import { useScanResults } from '@hooks/useScanResults'
import { useScanner } from '@hooks/useScanner'
import { accessibilityService } from '@services/accessibility.service'
import type { Page, ScanResult, FilterState, SortOrder, WCAGLevel } from '@types/index'

export function AccessibilityChecker() {
  // State
  const [selectedPage, setSelectedPage] = useState<Page | null>(null)
  const [wcagLevel, setWcagLevel] = useState<WCAGLevel>('AA')
  const [searchTerm, setSearchTerm] = useState('')
  const [filters, setFilters] = useState<FilterState>({})
  const [sortOrder, setSortOrder] = useState<SortOrder>('date-desc')
  const [showFilters, setShowFilters] = useState(false)
  const [showProgressDialog, setShowProgressDialog] = useState(false)
  const [showResultsModal, setShowResultsModal] = useState(false)
  const [modalResult, setModalResult] = useState<ScanResult | null>(null)

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
        const newResult: ScanResult = {
          scanId: Date.now().toString(),
          pagePath: selectedPage.path,
          pageUrl: selectedPage.url || '',
          pageTitle: selectedPage.title || selectedPage.name || selectedPage.path,
          scanDate: new Date().toISOString(),
          wcagLevel,
          score: result.score || 0,
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Accessibility Checker</h1>
          <p className="text-muted-foreground">
            Scan and monitor accessibility compliance across your website
          </p>
        </div>
      </div>

      {/* Stats Overview */}
      <StatsOverview stats={stats} />

      {/* Scan Controls */}
      <Card>
        <CardHeader>
          <CardTitle>Accessibility Audit</CardTitle>
          <CardDescription>
            {stats.scannedPages} of {stats.totalPages} pages scanned ({Math.round((stats.scannedPages / stats.totalPages) * 100) || 0}%)
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ScanControls
            pages={pages}
            selectedPage={selectedPage}
            wcagLevel={wcagLevel}
            isLoading={pagesLoading}
            isScanning={isScanning}
            onPageSelect={setSelectedPage}
            onWcagLevelChange={setWcagLevel}
            onScan={handleScan}
            onBulkScan={handleBulkScan}
          />
        </CardContent>
      </Card>

      {/* Results Section */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Scan Results</CardTitle>
            <div className="flex gap-2">
              <SearchBar 
                value={searchTerm}
                onChange={setSearchTerm}
                placeholder="Search pages or issues..."
              />
              <Button
                variant="outline"
                size="icon"
                onClick={() => setShowFilters(!showFilters)}
              >
                <Filter className="h-4 w-4" />
              </Button>
              <Button
                variant="outline"
                onClick={handleExport}
                disabled={filteredResults.length === 0}
              >
                <Download className="mr-2 h-4 w-4" />
                Export
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {showFilters && (
            <FilterPanel
              filters={filters}
              onFiltersChange={setFilters}
            />
          )}
          
          {resultsLoading ? (
            <div className="flex justify-center py-8">
              <div className="text-muted-foreground">Loading results...</div>
            </div>
          ) : filteredResults.length === 0 ? (
            <NoResultsEmptyState />
          ) : (
            <ResultsTable
              results={filteredResults}
              sortOrder={sortOrder}
              onSortChange={setSortOrder}
              onResultClick={(result) => {
                setModalResult(result)
                setShowResultsModal(true)
              }}
            />
          )}
        </CardContent>
      </Card>

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