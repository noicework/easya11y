import { useState, useCallback } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scannerService } from '@services/scanner.service'
import { accessibilityService } from '@services/accessibility.service'
import type { Page, WCAGLevel, ScanProgress } from '@types'

export function useScanner() {
  const queryClient = useQueryClient()
  const [isScanning, setIsScanning] = useState(false)
  const [scanProgress, setScanProgress] = useState<ScanProgress | null>(null)

  const scanPageMutation = useMutation({
    mutationFn: async ({ pagePath, wcagLevel }: { pagePath: string; wcagLevel: WCAGLevel }) => {
      setIsScanning(true)
      
      // Check if we should use server-side scanning
      const useServerSide = await accessibilityService.shouldUseServerSideScan()
      
      if (useServerSide) {
        // For server-side scan, we directly call the server
        const serverResult = await accessibilityService.serverSideScan(pagePath, wcagLevel)
        const detailedResult = await accessibilityService.getDetailedResult(pagePath)
        
        // The detailedResult contains the full scan result data
        return {
          violations: detailedResult.violations || [],
          passes: detailedResult.passes || [],
          incomplete: detailedResult.incomplete || [],
          inapplicable: detailedResult.inapplicable || [],
          score: detailedResult.score || serverResult.score
        }
      } else {
        // For client-side scan, use the normal flow
        const pageData = await accessibilityService.initiateScan(pagePath, wcagLevel)
        const result = await scannerService.scanPage({ ...pageData, wcagLevel })
        return result
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['scanResults'] })
      queryClient.invalidateQueries({ queryKey: ['pages'] })
    },
    onSettled: () => {
      setIsScanning(false)
    },
  })

  const scanMultiplePagesMutation = useMutation({
    mutationFn: async ({ pages, wcagLevel, concurrentBatchSize = 3 }: { pages: Page[]; wcagLevel: WCAGLevel; concurrentBatchSize?: number }) => {
      setIsScanning(true)
      setScanProgress(null)
      
      const result = await scannerService.scanMultiplePages(
        pages,
        wcagLevel,
        (progress) => setScanProgress(progress),
        concurrentBatchSize
      )
      
      return result
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['scanResults'] })
      queryClient.invalidateQueries({ queryKey: ['pages'] })
    },
    onSettled: () => {
      setIsScanning(false)
      setScanProgress(null)
    },
  })

  const scanPage = useCallback(
    (pagePath: string, wcagLevel: WCAGLevel = 'AA') => {
      return scanPageMutation.mutateAsync({ pagePath, wcagLevel })
    },
    [scanPageMutation]
  )

  const scanAllPages = useCallback(
    (pages: Page[], wcagLevel: WCAGLevel = 'AA', concurrentBatchSize?: number) => {
      return scanMultiplePagesMutation.mutateAsync({ pages, wcagLevel, concurrentBatchSize })
    },
    [scanMultiplePagesMutation]
  )

  return {
    scanPage,
    scanAllPages,
    isScanning,
    scanProgress,
    error: scanPageMutation.error || scanMultiplePagesMutation.error,
  }
}