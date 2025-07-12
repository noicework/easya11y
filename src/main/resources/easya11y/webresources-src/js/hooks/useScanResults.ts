import { useQuery } from '@tanstack/react-query'
import { accessibilityService } from '@services/accessibility.service'
import type { ScanResult } from '@types/index'

export function useScanResults() {
  return useQuery<ScanResult[], Error>({
    queryKey: ['scanResults'],
    queryFn: () => accessibilityService.getScanResults(),
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useDetailedResult(pagePath: string, enabled = true) {
  return useQuery({
    queryKey: ['scanResults', 'detail', pagePath],
    queryFn: () => accessibilityService.getDetailedResult(pagePath),
    enabled: enabled && !!pagePath,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}