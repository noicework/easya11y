import React, { useState } from 'react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@components/ui/table'
import { Button } from '@components/ui/button'
import { Badge } from '@components/ui/badge'
import { ScoreIndicator } from '@components/ScoreIndicator'
import { ArrowUpDown, ChevronLeft, ChevronRight, ExternalLink } from 'lucide-react'
import type { ScanResult, SortOrder } from '@types'

interface ResultsTableProps {
  results: ScanResult[]
  sortOrder: SortOrder
  onSortChange: (order: SortOrder) => void
  onResultClick: (result: ScanResult) => void
}

const ITEMS_PER_PAGE = 10

export function ResultsTable({
  results,
  sortOrder,
  onSortChange,
  onResultClick,
}: ResultsTableProps) {
  const [currentPage, setCurrentPage] = useState(1)

  const totalPages = Math.ceil(results.length / ITEMS_PER_PAGE)
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE
  const endIndex = startIndex + ITEMS_PER_PAGE
  const paginatedResults = results.slice(startIndex, endIndex)

  const handleSort = (newOrder: SortOrder) => {
    onSortChange(newOrder)
    setCurrentPage(1)
  }

  const getSeverityBadge = (result: ScanResult) => {
    const critical = result.criticalCount || result.violations_critical || 0
    const serious = result.seriousCount || result.violations_serious || 0
    const moderate = result.moderateCount || result.violations_moderate || 0
    const minor = result.minorCount || result.violations_minor || 0

    if (critical > 0) {
      return <Badge variant="destructive">Critical ({critical})</Badge>
    }
    if (serious > 0) {
      return <Badge variant="destructive">Serious ({serious})</Badge>
    }
    if (moderate > 0) {
      return <Badge variant="secondary">Moderate ({moderate})</Badge>
    }
    if (minor > 0) {
      return <Badge variant="outline">Minor ({minor})</Badge>
    }
    return <Badge variant="outline">No Issues</Badge>
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date)
  }

  return (
    <div className="space-y-4">
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[300px]">Page</TableHead>
              <TableHead>
                <Button
                  variant="ghost"
                  size="sm"
                  className="-ml-3 h-8"
                  onClick={() => handleSort(sortOrder === 'score-asc' ? 'score-desc' : 'score-asc')}
                >
                  Score
                  <ArrowUpDown className="ml-2 h-4 w-4" />
                </Button>
              </TableHead>
              <TableHead>Severity</TableHead>
              <TableHead>
                <Button
                  variant="ghost"
                  size="sm"
                  className="-ml-3 h-8"
                  onClick={() => handleSort('violations-desc')}
                >
                  Violations
                  <ArrowUpDown className="ml-2 h-4 w-4" />
                </Button>
              </TableHead>
              <TableHead>WCAG Level</TableHead>
              <TableHead>
                <Button
                  variant="ghost"
                  size="sm"
                  className="-ml-3 h-8"
                  onClick={() => handleSort('date-desc')}
                >
                  Last Scanned
                  <ArrowUpDown className="ml-2 h-4 w-4" />
                </Button>
              </TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {paginatedResults.map((result) => (
              <TableRow
                key={result.scanId}
                className="cursor-pointer hover:bg-muted/50"
                onClick={() => onResultClick(result)}
              >
                <TableCell>
                  <div className="space-y-1">
                    <div className="font-medium">{result.pageTitle}</div>
                    <div className="text-xs text-muted-foreground truncate max-w-[250px]">
                      {result.pagePath}
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <ScoreIndicator score={result.score || 0} size="sm" />
                </TableCell>
                <TableCell>{getSeverityBadge(result)}</TableCell>
                <TableCell>
                  <span className="tabular-nums">{result.violationCount || 0}</span>
                </TableCell>
                <TableCell>
                  <Badge variant="outline">{result.wcagLevel}</Badge>
                </TableCell>
                <TableCell>
                  <span className="text-sm text-muted-foreground">
                    {formatDate(result.scanDate)}
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
                  >
                    <ExternalLink className="h-4 w-4" />
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Showing {startIndex + 1} to {Math.min(endIndex, results.length)} of{' '}
            {results.length} results
          </div>
          <div className="flex items-center space-x-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage(currentPage - 1)}
              disabled={currentPage === 1}
            >
              <ChevronLeft className="h-4 w-4" />
              Previous
            </Button>
            <div className="flex items-center gap-1">
              {Array.from({ length: totalPages }, (_, i) => i + 1)
                .filter((page) => {
                  return (
                    page === 1 ||
                    page === totalPages ||
                    Math.abs(page - currentPage) <= 1
                  )
                })
                .map((page, index, array) => (
                  <React.Fragment key={page}>
                    {index > 0 && array[index - 1] !== page - 1 && (
                      <span className="px-1">...</span>
                    )}
                    <Button
                      variant={currentPage === page ? 'default' : 'outline'}
                      size="sm"
                      onClick={() => setCurrentPage(page)}
                      className="h-8 w-8 p-0"
                    >
                      {page}
                    </Button>
                  </React.Fragment>
                ))}
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              Next
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}