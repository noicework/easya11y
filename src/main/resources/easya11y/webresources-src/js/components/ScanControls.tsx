import { PageSelector } from '@components/PageSelector'
import { Button } from '@components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@components/ui/select'
import { Play, PlayCircle, AlertCircle } from 'lucide-react'
import { Alert, AlertDescription } from '@components/ui/alert'
import type { Page, WCAGLevel } from '@types'

interface ScanControlsProps {
  pages: Page[]
  selectedPage: Page | null
  wcagLevel: WCAGLevel
  isLoading: boolean
  isScanning: boolean
  onPageSelect: (page: Page | null) => void
  onWcagLevelChange: (level: WCAGLevel) => void
  onScan: () => void
  onBulkScan: () => void
}

export function ScanControls({
  pages,
  selectedPage,
  wcagLevel,
  isLoading,
  isScanning,
  onPageSelect,
  onWcagLevelChange,
  onScan,
  onBulkScan,
}: ScanControlsProps) {
  const canScan = selectedPage && !isScanning
  const canBulkScan = pages.length > 0 && !isScanning

  return (
    <div className="space-y-4">
      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <label className="text-sm font-medium leading-none">
            Page to Scan
          </label>
          <PageSelector
            pages={pages}
            value={selectedPage}
            onSelect={onPageSelect}
            disabled={isLoading || isScanning}
            placeholder={isLoading ? 'Loading pages...' : 'Select a page...'}
          />
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium leading-none">
            WCAG Level
          </label>
          <Select
            value={wcagLevel}
            onValueChange={(value) => onWcagLevelChange(value as WCAGLevel)}
            disabled={isScanning}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="A">Level A</SelectItem>
              <SelectItem value="AA">Level AA (Recommended)</SelectItem>
              <SelectItem value="AAA">Level AAA</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <Button
          onClick={onScan}
          disabled={!canScan}
          className="flex-1 sm:flex-initial"
        >
          <Play className="mr-2 h-4 w-4" />
          {isScanning ? 'Scanning...' : 'Scan Selected Page'}
        </Button>

        <Button
          variant="outline"
          onClick={onBulkScan}
          disabled={!canBulkScan}
          className="flex-1 sm:flex-initial"
        >
          <PlayCircle className="mr-2 h-4 w-4" />
          {isScanning ? 'Scanning...' : `Scan All Pages (${pages.length})`}
        </Button>
      </div>

      {!selectedPage && !isLoading && (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Select a page from the dropdown to begin accessibility scanning.
          </AlertDescription>
        </Alert>
      )}
    </div>
  )
}