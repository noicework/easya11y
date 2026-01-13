import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { Button } from '@components/ui/button'
import { Badge } from '@components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@components/ui/table'
import { ScoreIndicator } from '@components/ScoreIndicator'
import { SearchBar } from '@components/SearchBar'
import { FilterPanel } from '@components/FilterPanel'
import {
  Clock,
  TrendingUp,
  Filter,
  Download,
  ExternalLink,
  AlertTriangle,
  CheckCircle2,
  Activity,
  Zap,
  History
} from 'lucide-react'
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  ResponsiveContainer
} from 'recharts'
import type { ScanResult, FilterState } from '@types'

interface RecentScansPanelProps {
  results: ScanResult[]
  filteredResults: ScanResult[]
  searchTerm: string
  filters: FilterState
  showFilters: boolean
  totalPages: number
  onSearchChange: (term: string) => void
  onFiltersChange: (filters: FilterState) => void
  onToggleFilters: () => void
  onResultClick: (result: ScanResult) => void
  onExport: () => void
  isLoading: boolean
  onQuickAudit: () => void
  onViewHistory: () => void
  historyAvailable?: boolean
}

export function RecentScansPanel({
  results,
  filteredResults,
  searchTerm,
  filters,
  showFilters,
  totalPages,
  onSearchChange,
  onFiltersChange,
  onToggleFilters,
  onResultClick,
  onExport,
  isLoading,
  onQuickAudit,
  onViewHistory,
  historyAvailable = false
}: RecentScansPanelProps) {

  // Calculate trend data for mini charts
  const trendData = results.slice(-7).map((result, index) => ({
    index,
    score: result.score || 0,
    violations: result.violationCount || 0,
    date: new Date(result.scanDate).toLocaleDateString()
  }))

  const averageScore = results.length > 0 
    ? results.reduce((sum, r) => sum + (r.score || 0), 0) / results.length 
    : 0

  const totalViolations = results.reduce((sum, r) => sum + (r.violationCount || 0), 0)

  const getSeverityBadge = (result: ScanResult) => {
    const critical = result.criticalCount || result.violations_critical || 0
    const serious = result.seriousCount || result.violations_serious || 0
    const moderate = result.moderateCount || result.violations_moderate || 0
    const minor = result.minorCount || result.violations_minor || 0

    if (critical > 0) {
      return <Badge variant="destructive" className="text-xs">Critical</Badge>
    }
    if (serious > 0) {
      return <Badge variant="destructive" className="text-xs">Serious</Badge>
    }
    if (moderate > 0) {
      return <Badge variant="secondary" className="text-xs">Moderate</Badge>
    }
    if (minor > 0) {
      return <Badge variant="outline" className="text-xs">Minor</Badge>
    }
    return <Badge variant="outline" className="text-xs">✓ Clean</Badge>
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date)
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Activity className="h-5 w-5 text-green-600" />
            <CardTitle>Recent Scans</CardTitle>
          </div>
          <div className="flex gap-2">
            <Button onClick={onQuickAudit} size="sm">
              <Zap className="h-4 w-4 mr-2" />
              Quick Audit
            </Button>
            <Button
              variant="outline"
              onClick={onViewHistory}
              size="sm"
              disabled={!historyAvailable}
              title={!historyAvailable ? 'Historical data requires database storage configuration' : 'View historical scan data'}
            >
              <History className="h-4 w-4 mr-2" />
              View History
            </Button>
          </div>
        </div>
        <CardDescription>
          Monitor your accessibility compliance progress and trends
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Mini Stats & Trend Charts */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {/* Average Score Trend */}
          <div className="bg-gradient-to-r from-blue-50 to-blue-100 rounded-lg p-4 border">
            <div className="flex items-center justify-between mb-2">
              <div>
                <p className="text-sm font-medium text-blue-900">Average Score</p>
                <p className="text-2xl font-bold text-blue-600">{averageScore.toFixed(1)}</p>
              </div>
              <div className="flex items-center text-blue-600">
                <TrendingUp className="h-4 w-4" />
              </div>
            </div>
            <div className="h-12">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trendData}>
                  <Line 
                    type="monotone" 
                    dataKey="score" 
                    stroke="#2563eb" 
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Total Violations */}
          <div className="bg-gradient-to-r from-red-50 to-red-100 rounded-lg p-4 border">
            <div className="flex items-center justify-between mb-2">
              <div>
                <p className="text-sm font-medium text-red-900">Total Issues</p>
                <p className="text-2xl font-bold text-red-600">{totalViolations}</p>
              </div>
              <div className="flex items-center text-red-600">
                <AlertTriangle className="h-4 w-4" />
              </div>
            </div>
            <div className="h-12">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trendData}>
                  <Area 
                    type="monotone" 
                    dataKey="violations" 
                    stroke="#dc2626" 
                    fill="#dc2626"
                    fillOpacity={0.3}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Scans This Week */}
          <div className="bg-gradient-to-r from-green-50 to-green-100 rounded-lg p-4 border">
            <div className="flex items-center justify-between mb-2">
              <div>
                <p className="text-sm font-medium text-green-900">Recent Scans</p>
                <p className="text-2xl font-bold text-green-600">{results.length}</p>
              </div>
              <div className="flex items-center text-green-600">
                <CheckCircle2 className="h-4 w-4" />
              </div>
            </div>
            <div className="text-xs text-green-700 mt-2">
              {results.filter(r => {
                const scanDate = new Date(r.scanDate)
                const weekAgo = new Date()
                weekAgo.setDate(weekAgo.getDate() - 7)
                return scanDate > weekAgo
              }).length} scans this week
            </div>
          </div>
        </div>

        {/* Controls */}
        <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center">
          <div className="flex-1 w-full">
            <SearchBar 
              value={searchTerm}
              onChange={onSearchChange}
              placeholder="Search pages or issues..."
            />
          </div>
          <div className="flex gap-2 w-full sm:w-auto justify-end">
            <Button
              variant="outline"
              size="sm"
              onClick={onToggleFilters}
            >
              <Filter className="h-4 w-4 mr-2" />
              Filter
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={onExport}
              disabled={filteredResults.length === 0}
            >
              <Download className="h-4 w-4 mr-2" />
              Export
            </Button>
          </div>
        </div>

        {/* Filters */}
        {showFilters && (
          <FilterPanel
            filters={filters}
            onFiltersChange={onFiltersChange}
          />
        )}

        {/* Results Table - Condensed */}
        <div className="space-y-4">
          {isLoading ? (
            <div className="flex justify-center py-8">
              <div className="text-muted-foreground">Loading results...</div>
            </div>
          ) : results.length === 0 ? (
            <div className="text-center py-12">
              <div className="mx-auto w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4">
                <Zap className="h-8 w-8 text-blue-600" />
              </div>
              <h3 className="text-lg font-semibold mb-2">No scans yet</h3>
              <p className="text-muted-foreground mb-6 max-w-md mx-auto">
                Start scanning your {totalPages > 0 ? `${totalPages} pages` : 'website'} to identify accessibility issues and ensure WCAG compliance.
              </p>
              <Button onClick={onQuickAudit} size="lg">
                <Zap className="mr-2 h-5 w-5" />
                Run Your First Scan
              </Button>
            </div>
          ) : filteredResults.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Clock className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>No scans match your current filters.</p>
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[200px]">Page</TableHead>
                    <TableHead>Score</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Issues</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredResults.slice(0, 8).map((result) => (
                    <TableRow
                      key={result.scanId}
                      className="cursor-pointer hover:bg-muted/50"
                      onClick={() => onResultClick(result)}
                    >
                      <TableCell>
                        <div className="space-y-1">
                          <div className="font-medium text-sm truncate max-w-[180px]">
                            {result.pageTitle}
                          </div>
                          <div className="text-xs text-muted-foreground">
                            {formatDate(result.scanDate)}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <ScoreIndicator score={result.score || 0} size="sm" />
                      </TableCell>
                      <TableCell>
                        {getSeverityBadge(result)}
                      </TableCell>
                      <TableCell>
                        <span className="text-sm tabular-nums">
                          {result.violationCount || 0}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation()
                            window.open(result.pageUrl, '_blank')
                          }}
                          title="Open page"
                        >
                          <ExternalLink className="h-3 w-3" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              
              {filteredResults.length > 8 && (
                <div className="px-4 py-3 border-t bg-muted/20">
                  <p className="text-sm text-muted-foreground text-center">
                    Showing 8 of {filteredResults.length} results
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}