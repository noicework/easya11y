import { useEffect, useState } from 'react'
import { Card, CardContent } from '@components/ui/card'
import { ScoreIndicator } from '@components/ScoreIndicator'
import { ViolationCard } from '@components/ViolationCard'
import { Alert, AlertDescription } from '@components/ui/alert'
import { Loader2, AlertCircle, CheckCircle2 } from 'lucide-react'
import { scannerService } from '@services/scanner.service'
import { calculateScore } from '@lib/utils'

export function AccessibilityScanDialog() {
  const [isScanning, setIsScanning] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [scanResult, setScanResult] = useState<any>(null)

  useEffect(() => {
    performScan()
  }, [])

  const performScan = async () => {
    try {
      // Parse URL parameters
      const urlParams = new URLSearchParams(window.location.search)
      const pageUrl = urlParams.get('pageUrl')
      const scanId = urlParams.get('scanId')
      const pagePath = urlParams.get('pagePath')
      const pageTitle = urlParams.get('pageTitle') || 'Unknown Page'
      const wcagLevel = (urlParams.get('wcagLevel') || 'AA') as 'A' | 'AA' | 'AAA'

      if (!pageUrl || !scanId || !pagePath) {
        throw new Error('Missing required parameters')
      }

      const result = await scannerService.scanPage({
        scanId,
        pagePath,
        pageUrl,
        pageTitle,
        wcagLevel,
      })

      setScanResult(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Scan failed')
    } finally {
      setIsScanning(false)
    }
  }

  if (isScanning) {
    return (
      <div className="flex h-screen items-center justify-center bg-background p-4">
        <Card className="w-full max-w-md">
          <CardContent className="flex flex-col items-center py-12">
            <Loader2 className="mb-4 h-12 w-12 animate-spin text-primary" />
            <p className="text-lg font-medium">Running accessibility scan...</p>
            <p className="mt-2 text-sm text-muted-foreground">This may take a moment</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center bg-background p-4">
        <Alert variant="destructive" className="max-w-md">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            <strong>Scan failed:</strong> {error}
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  if (!scanResult) return null

  const score = scanResult.score ?? calculateScore(scanResult)
  const violations = scanResult.violations || []
  const violationCount = violations.length
  const passCount = scanResult.passes?.length || 0
  const totalIssues = violations.reduce((sum: number, v: any) => 
    sum + (v.nodes?.length || 0), 0
  )

  // Count violations by impact
  const impactCounts = {
    critical: violations.filter((v: any) => v.impact === 'critical').length,
    serious: violations.filter((v: any) => v.impact === 'serious').length,
    moderate: violations.filter((v: any) => v.impact === 'moderate').length,
    minor: violations.filter((v: any) => v.impact === 'minor').length,
  }

  return (
    <div className="min-h-screen bg-background p-6">
      <Card className="mx-auto max-w-4xl">
        <CardContent className="p-6">
          {/* Score Summary */}
          <div className="mb-8 flex items-center gap-8">
            <ScoreIndicator score={score} size="lg" />
            <div className="flex-1">
              <h2 className="text-2xl font-semibold">Accessibility Score</h2>
              <div className="mt-2 space-y-1 text-sm text-muted-foreground">
                <p>Violations: <strong className="text-foreground">{violationCount}</strong></p>
                <p>Passed Rules: <strong className="text-foreground">{passCount}</strong></p>
                <p>Total Issues: <strong className="text-foreground">{totalIssues}</strong></p>
              </div>
            </div>
          </div>

          {/* Impact Summary */}
          <div className="mb-8 flex flex-wrap gap-2">
            {impactCounts.critical > 0 && (
              <span className="inline-flex items-center rounded-full bg-a11y-critical px-3 py-1 text-xs font-medium text-white">
                Critical: {impactCounts.critical}
              </span>
            )}
            {impactCounts.serious > 0 && (
              <span className="inline-flex items-center rounded-full bg-a11y-serious px-3 py-1 text-xs font-medium text-white">
                Serious: {impactCounts.serious}
              </span>
            )}
            {impactCounts.moderate > 0 && (
              <span className="inline-flex items-center rounded-full bg-a11y-moderate px-3 py-1 text-xs font-medium text-white">
                Moderate: {impactCounts.moderate}
              </span>
            )}
            {impactCounts.minor > 0 && (
              <span className="inline-flex items-center rounded-full bg-a11y-minor px-3 py-1 text-xs font-medium text-gray-900">
                Minor: {impactCounts.minor}
              </span>
            )}
            {violationCount === 0 && (
              <div className="flex items-center gap-2 text-a11y-success">
                <CheckCircle2 className="h-5 w-5" />
                <span className="font-medium">No violations found!</span>
              </div>
            )}
          </div>

          {/* Violations List */}
          {violations.length > 0 && (
            <div>
              <h3 className="mb-4 text-lg font-semibold">Accessibility Issues</h3>
              <div className="space-y-4">
                {['critical', 'serious', 'moderate', 'minor'].map((impact) => {
                  const impactViolations = violations.filter((v: any) => v.impact === impact)
                  return impactViolations.map((violation: any) => (
                    <ViolationCard key={violation.id} violation={violation} />
                  ))
                })}
              </div>
            </div>
          )}

          {violations.length === 0 && (
            <Alert className="border-a11y-success bg-a11y-success/10">
              <CheckCircle2 className="h-4 w-4 text-a11y-success" />
              <AlertDescription className="text-a11y-success">
                <strong>Great job!</strong> No accessibility violations were found on this page.
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
      </Card>
    </div>
  )
}