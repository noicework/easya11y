import { useEffect, useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@components/ui/tabs'
import { ScrollArea } from '@components/ui/scroll-area'
import { Badge } from '@components/ui/badge'
import { Button } from '@components/ui/button'
import { ScoreIndicator } from '@components/ScoreIndicator'
import { ViolationCard } from '@components/ViolationCard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { ExternalLink, CheckCircle2, Loader2 } from 'lucide-react'
import { accessibilityService } from '@services/accessibility.service'
import type { ScanResult } from '@types'

interface ScanResultsModalProps {
  isOpen: boolean
  result: ScanResult | null
  onClose: () => void
}

export function ScanResultsModal({ isOpen, result, onClose }: ScanResultsModalProps) {
  const [detailedResult, setDetailedResult] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (isOpen && result?.pagePath) {
      loadDetailedResult()
    }
  }, [isOpen, result?.pagePath])

  const loadDetailedResult = async () => {
    if (!result?.pagePath) return
    
    setLoading(true)
    setError(null)
    
    try {
      const details = await accessibilityService.getDetailedResult(result.pagePath)
      setDetailedResult(details)
    } catch (err) {
      setError('Failed to load scan details')
      console.error('Error loading scan details:', err)
    } finally {
      setLoading(false)
    }
  }

  if (!result) return null
  
  // Use detailed result if available, otherwise fall back to summary
  const displayResult = detailedResult || result

  // Group violations by impact
  const groupedViolations = {
    critical: displayResult.violations?.filter((v: any) => v.impact === 'critical') || [],
    serious: displayResult.violations?.filter((v: any) => v.impact === 'serious') || [],
    moderate: displayResult.violations?.filter((v: any) => v.impact === 'moderate') || [],
    minor: displayResult.violations?.filter((v: any) => v.impact === 'minor') || [],
  }

  // Calculate counts from the grouped violations
  const critical = groupedViolations.critical.length
  const serious = groupedViolations.serious.length
  const moderate = groupedViolations.moderate.length
  const minor = groupedViolations.minor.length

  const totalPassed = displayResult.passes?.length || displayResult.passCount || 0

  const handleClose = () => {
    setDetailedResult(null)
    setError(null)
    onClose()
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent className="max-w-4xl max-h-[90vh] flex flex-col">
        <DialogHeader>
          <div className="flex items-start justify-between">
            <div className="space-y-1.5">
              <DialogTitle>{result.pageTitle}</DialogTitle>
              <DialogDescription className="flex items-center gap-2">
                <span className="truncate max-w-md">{result.pagePath}</span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => window.open(result.pageUrl, '_blank')}
                >
                  <ExternalLink className="h-3 w-3" />
                </Button>
              </DialogDescription>
            </div>
            <ScoreIndicator score={result.score || 0} size="lg" />
          </div>
        </DialogHeader>

        <div className="flex-1 overflow-hidden">
          <Tabs defaultValue="violations" className="h-full flex flex-col">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="violations">
                Violations ({result.violationCount || 0})
              </TabsTrigger>
              <TabsTrigger value="passes">
                Passes ({totalPassed})
              </TabsTrigger>
              <TabsTrigger value="summary">Summary</TabsTrigger>
            </TabsList>

            <TabsContent value="violations" className="flex-1 overflow-hidden mt-4">
              {loading ? (
                <div className="flex items-center justify-center h-[400px]">
                  <div className="flex flex-col items-center gap-2">
                    <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                    <p className="text-sm text-muted-foreground">Loading scan details...</p>
                  </div>
                </div>
              ) : error ? (
                <div className="flex items-center justify-center h-[400px]">
                  <p className="text-sm text-muted-foreground">{error}</p>
                </div>
              ) : (
                <ScrollArea className="h-[400px] pr-4">
                  <div className="space-y-6">
                  {critical > 0 && (
                    <div className="space-y-3">
                      <h3 className="font-medium flex items-center gap-2">
                        <span className="text-red-600">●</span>
                        Critical Issues ({critical})
                      </h3>
                      <div className="space-y-2">
                        {groupedViolations.critical.map((violation: any) => (
                          <ViolationCard key={violation.id} violation={violation} />
                        ))}
                      </div>
                    </div>
                  )}

                  {serious > 0 && (
                    <div className="space-y-3">
                      <h3 className="font-medium flex items-center gap-2">
                        <span className="text-orange-600">●</span>
                        Serious Issues ({serious})
                      </h3>
                      <div className="space-y-2">
                        {groupedViolations.serious.map((violation: any) => (
                          <ViolationCard key={violation.id} violation={violation} />
                        ))}
                      </div>
                    </div>
                  )}

                  {moderate > 0 && (
                    <div className="space-y-3">
                      <h3 className="font-medium flex items-center gap-2">
                        <span className="text-yellow-600">●</span>
                        Moderate Issues ({moderate})
                      </h3>
                      <div className="space-y-2">
                        {groupedViolations.moderate.map((violation: any) => (
                          <ViolationCard key={violation.id} violation={violation} />
                        ))}
                      </div>
                    </div>
                  )}

                  {minor > 0 && (
                    <div className="space-y-3">
                      <h3 className="font-medium flex items-center gap-2">
                        <span className="text-blue-600">●</span>
                        Minor Issues ({minor})
                      </h3>
                      <div className="space-y-2">
                        {groupedViolations.minor.map((violation: any) => (
                          <ViolationCard key={violation.id} violation={violation} />
                        ))}
                      </div>
                    </div>
                  )}

                  {result.violationCount === 0 && (
                    <Card>
                      <CardContent className="pt-6">
                        <div className="text-center space-y-2">
                          <CheckCircle2 className="h-12 w-12 text-green-600 mx-auto" />
                          <p className="font-medium">No accessibility violations found!</p>
                          <p className="text-sm text-muted-foreground">
                            This page passes all accessibility checks.
                          </p>
                        </div>
                      </CardContent>
                    </Card>
                  )}
                </div>
                </ScrollArea>
              )}
            </TabsContent>

            <TabsContent value="passes" className="flex-1 overflow-hidden mt-4">
              {loading ? (
                <div className="flex items-center justify-center h-[400px]">
                  <div className="flex flex-col items-center gap-2">
                    <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                    <p className="text-sm text-muted-foreground">Loading scan details...</p>
                  </div>
                </div>
              ) : (
                <ScrollArea className="h-[400px] pr-4">
                  <div className="space-y-2">
                    {displayResult.passes?.map((pass: any) => (
                    <Card key={pass.id}>
                      <CardHeader>
                        <div className="flex items-start justify-between">
                          <div className="space-y-1">
                            <CardTitle className="text-base">{pass.help}</CardTitle>
                            <CardDescription>{pass.description}</CardDescription>
                          </div>
                          <CheckCircle2 className="h-5 w-5 text-green-600" />
                        </div>
                      </CardHeader>
                    </Card>
                  ))}
                </div>
                </ScrollArea>
              )}
            </TabsContent>

            <TabsContent value="summary" className="mt-4">
              <div className="space-y-4">
                <Card>
                  <CardHeader>
                    <CardTitle>Scan Details</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium">Scan Date</p>
                        <p className="text-sm text-muted-foreground">
                          {new Date(result.scanDate).toLocaleString()}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm font-medium">WCAG Level</p>
                        <Badge variant="outline">{result.wcagLevel}</Badge>
                      </div>
                      <div>
                        <p className="text-sm font-medium">Total Violations</p>
                        <p className="text-2xl font-bold">{result.violationCount || 0}</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium">Accessibility Score</p>
                        <p className="text-2xl font-bold">{Math.round(result.score || 0)}/100</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Violation Breakdown</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="flex items-center gap-2">
                          <span className="text-red-600">●</span> Critical
                        </span>
                        <span className="font-medium">{critical}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="flex items-center gap-2">
                          <span className="text-orange-600">●</span> Serious
                        </span>
                        <span className="font-medium">{serious}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="flex items-center gap-2">
                          <span className="text-yellow-600">●</span> Moderate
                        </span>
                        <span className="font-medium">{moderate}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="flex items-center gap-2">
                          <span className="text-blue-600">●</span> Minor
                        </span>
                        <span className="font-medium">{minor}</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </DialogContent>
    </Dialog>
  )
}