import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { Button } from '@components/ui/button'
import { Checkbox } from '@components/ui/checkbox'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@components/ui/select'
import { PageSelector } from '@components/PageSelector'
import { Play, PlayCircle, Zap, Settings2 } from 'lucide-react'
import type { Page, WCAGLevel } from '@types'

interface QuickAuditPanelProps {
  pages: Page[]
  selectedPage: Page | null
  wcagLevel: WCAGLevel
  isLoading: boolean
  isScanning: boolean
  onPageSelect: (page: Page | null) => void
  onWcagLevelChange: (level: WCAGLevel) => void
  onScan: () => void
  onBulkScan: () => void
  scanSubpages: boolean
  onScanSubpagesChange: (enabled: boolean) => void
}

export function QuickAuditPanel({
  pages,
  selectedPage,
  wcagLevel,
  isLoading,
  isScanning,
  onPageSelect,
  onWcagLevelChange,
  onScan,
  onBulkScan,
  scanSubpages,
  onScanSubpagesChange,
}: QuickAuditPanelProps) {
  const canScan = selectedPage && !isScanning
  const canBulkScan = pages.length > 0 && !isScanning

  return (
    <Card className="h-fit">
      <CardHeader>
        <div className="flex items-center gap-2">
          <Zap className="h-5 w-5 text-blue-600" />
          <CardTitle>Quick Audit</CardTitle>
        </div>
        <CardDescription>
          Perform instant accessibility scans to identify compliance issues
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Page Selection */}
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <label className="text-sm font-medium">Select Page</label>
            <span className="text-xs text-muted-foreground">
              {pages.length} pages available
            </span>
          </div>
          <PageSelector
            pages={pages}
            value={selectedPage}
            onSelect={onPageSelect}
            disabled={isLoading || isScanning}
            placeholder={isLoading ? 'Loading pages...' : 'Choose a page to scan...'}
          />
          
          {/* Scan Subpages Toggle */}
          <div className="flex items-center space-x-2 pt-2">
            <Checkbox
              id="scan-subpages"
              checked={scanSubpages}
              onCheckedChange={onScanSubpagesChange}
              disabled={isScanning}
            />
            <label
              htmlFor="scan-subpages"
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
            >
              Include subpages in scan
            </label>
          </div>
        </div>

        {/* WCAG Level Selection */}
        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <Settings2 className="h-4 w-4 text-muted-foreground" />
            <label className="text-sm font-medium">WCAG Compliance Level</label>
          </div>
          <Select
            value={wcagLevel}
            onValueChange={(value) => onWcagLevelChange(value as WCAGLevel)}
            disabled={isScanning}
          >
            <SelectTrigger className="text-left">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="A" className="text-left">
                <div className="flex flex-col items-start">
                  <span className="font-medium">Level A</span>
                  <span className="text-xs text-muted-foreground">Basic compliance</span>
                </div>
              </SelectItem>
              <SelectItem value="AA" className="text-left">
                <div className="flex flex-col items-start">
                  <span className="font-medium">Level AA</span>
                  <span className="text-xs text-muted-foreground">Recommended standard</span>
                </div>
              </SelectItem>
              <SelectItem value="AAA" className="text-left">
                <div className="flex flex-col items-start">
                  <span className="font-medium">Level AAA</span>
                  <span className="text-xs text-muted-foreground">Highest compliance</span>
                </div>
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Scan Actions */}
        <div className="space-y-2">
          <Button
            onClick={onScan}
            disabled={!canScan}
            className="w-full"
            size="lg"
          >
            <Play className="mr-2 h-4 w-4" />
            {isScanning ? 'Scanning...' : 'Scan Selected Page'}
          </Button>

          <Button
            variant="outline"
            onClick={onBulkScan}
            disabled={!canBulkScan}
            className="w-full"
            size="lg"
          >
            <PlayCircle className="mr-2 h-4 w-4" />
            {isScanning ? 'Scanning...' : `Scan All Pages (${pages.length})`}
          </Button>
        </div>

        {/* Quick Info */}
        {selectedPage && (
          <div className="rounded-lg bg-blue-50 p-4 border-l-4 border-blue-400">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 bg-blue-500 rounded-full" />
              <p className="text-sm font-medium text-blue-900">Ready to scan</p>
            </div>
            <p className="text-xs text-blue-700 mt-1">
              {selectedPage.title || selectedPage.name}
            </p>
            {scanSubpages && (
              <p className="text-xs text-blue-600 mt-1">
                ✓ Subpages will be included
              </p>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  )
}