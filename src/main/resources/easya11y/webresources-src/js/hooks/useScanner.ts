import { useState, useCallback } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { scannerService } from '@services/scanner.service'
import { accessibilityService } from '@services/accessibility.service'
import type { Page, WCAGLevel, ScanProgress } from '@types/index'

export function useScanner() {
  const queryClient = useQueryClient()
  const [isScanning, setIsScanning] = useState(false)
  const [scanProgress, setScanProgress] = useState<ScanProgress | null>(null)

  const scanPageMutation = useMutation({
    mutationFn: async ({ pagePath, wcagLevel }: { pagePath: string; wcagLevel: WCAGLevel }) => {
      setIsScanning(true)
      const pageData = await accessibilityService.initiateScan(pagePath, wcagLevel)
      const result = await scannerService.scanPage({ ...pageData, wcagLevel })
      return result
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
    mutationFn: async ({ pages, wcagLevel }: { pages: Page[]; wcagLevel: WCAGLevel }) => {
      setIsScanning(true)
      setScanProgress(null)
      
      const result = await scannerService.scanMultiplePages(
        pages,
        wcagLevel,
        (progress) => setScanProgress(progress)
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
    (pages: Page[], wcagLevel: WCAGLevel = 'AA') => {
      return scanMultiplePagesMutation.mutateAsync({ pages, wcagLevel })
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